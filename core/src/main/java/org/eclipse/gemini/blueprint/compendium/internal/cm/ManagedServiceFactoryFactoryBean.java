/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.compendium.internal.cm;

import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.compendium.internal.cm.ManagedFactoryDisposableInvoker.DestructionCodes;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.service.exporter.OsgiServiceRegistrationListener;
import org.eclipse.gemini.blueprint.service.exporter.support.DefaultInterfaceDetector;
import org.eclipse.gemini.blueprint.service.exporter.support.ExportContextClassLoaderEnum;
import org.eclipse.gemini.blueprint.service.exporter.support.InterfaceDetector;
import org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean;
import org.eclipse.gemini.blueprint.service.exporter.support.ServicePropertiesChangeListener;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.DynamicCollection;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;
import org.eclipse.gemini.blueprint.util.internal.MapBasedDictionary;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * {@link FactoryBean Factory} class that automatically manages instances based on the configuration available inside a
 * {@link ManagedServiceFactory}.
 * 
 * The factory returns a list of {@link ServiceRegistration} of all published instances.
 * 
 * @author Costin Leau
 */
public class ManagedServiceFactoryFactoryBean implements InitializingBean, BeanClassLoaderAware, BeanFactoryAware,
		BundleContextAware, DisposableBean, FactoryBean<Collection> {

	/**
	 * Configuration Admin whiteboard 'listener'.
	 * 
	 * @author Costin Leau
	 */
	private class ConfigurationWatcher implements ManagedServiceFactory {

		public void deleted(String pid) {
			if (log.isTraceEnabled())
				log.trace("Configuration [" + pid + "] has been deleted");
			destroyInstance(pid);
		}

		public String getName() {
			return "Spring DM managed-service-factory support";
		}

		public void updated(String pid, Dictionary props) throws ConfigurationException {
			if (log.isTraceEnabled())
				log.trace("Configuration [" + pid + "] has been updated with properties " + props);
			createOrUpdate(pid, new MapBasedDictionary(props));
		}
	}

	/**
	 * Simple processor that applies the ConfigurationAdmin configuration before the bean is initialized.
	 * 
	 * @author Costin Leau
	 */
	private class InitialInjectionProcessor implements BeanPostProcessor {

		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			return bean;
		}

		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			CMUtils.applyMapOntoInstance(bean, initialInjectionProperties, beanFactory);

			if (log.isTraceEnabled()) {
				log.trace("Applying initial injection for managed bean " + beanName);
			}

			return bean;
		}
	}

	/**
	 * Simple associating cache between types and disposable invoker.
	 * 
	 * @author Costin Leau
	 */
	private class DestructionInvokerCache {

		private final String methodName;
		final ConcurrentMap<Class<?>, ManagedFactoryDisposableInvoker> cache =
				new ConcurrentHashMap<Class<?>, ManagedFactoryDisposableInvoker>(4);

		DestructionInvokerCache(String methodName) {
			this.methodName = methodName;
		}

		ManagedFactoryDisposableInvoker getInvoker(Class<?> type) {
			ManagedFactoryDisposableInvoker invoker = cache.get(type);
			// non-atomic check for the destruction invoker (duplicate invokers are okay)
			if (invoker == null) {
				invoker = new ManagedFactoryDisposableInvoker(type, methodName);
				cache.put(type, invoker);
			}
			return invoker;
		}

	}

	/** logger */
	private static final Log log = LogFactory.getLog(ManagedServiceFactoryFactoryBean.class);

	/** visibility monitor */
	private final Object monitor = new Object();
	/** Configuration Admin fpid */
	private String factoryPid;
	/** bundle context */
	private BundleContext bundleContext;
	/** embedded bean factory for instance management */
	private DefaultListableBeanFactory beanFactory;
	/** bean definition template */
	private RootBeanDefinition templateDefinition;
	/** owning bean factory - can be null */
	private BeanFactory owningBeanFactory;
	/** configuration watcher registration */
	private ServiceRegistration configurationWatcher;
	/** inner bean service registrations */
	private final DynamicCollection serviceRegistrations = new DynamicCollection(8);
	/** read-only view of the registration */
	private final Collection<ServiceRegistration> userReturnedCollection =
			Collections.unmodifiableCollection(serviceRegistrations);
	/** lookup map between exporters and associated pids */
	private final Map<String, OsgiServiceFactoryBean> serviceExporters =
			new ConcurrentHashMap<String, OsgiServiceFactoryBean>(8);

	// exporting template
	/** listeners */
	private OsgiServiceRegistrationListener[] listeners = new OsgiServiceRegistrationListener[0];
	/** auto export */
	private InterfaceDetector detector = DefaultInterfaceDetector.DISABLED;
	/** ccl */
	private ExportContextClassLoaderEnum ccl = ExportContextClassLoaderEnum.UNMANAGED;
	/** interfaces */
	private Class<?>[] interfaces;
	/** class loader */
	private ClassLoader classLoader;
	private boolean autowireOnUpdate = false;
	private String updateMethod;
	/** update callback */
	private UpdateCallback updateCallback;

	// this fields gets set whenever a new CM entry is created
	// no synch is needed since the CF is guaranteed to be called sequentially and the callback doesn't return
	// until the bean is fully initialized and published
	public Map initialInjectionProperties;

	/**
	 * destroyed flag - used since some CM implementations still call the service even though it was unregistered
	 */
	private boolean destroyed = false;
	/** service properties */
	private volatile Map serviceProperties;
	/** special destruction invoker for managed-components/template beans */
	private volatile DestructionInvokerCache destructionInvokerFactory;

	public void afterPropertiesSet() throws Exception {

		synchronized (monitor) {
			Assert.notNull(factoryPid, "factoryPid required");
			Assert.notNull(bundleContext, "bundleContext is required");
			Assert.notNull(templateDefinition, "templateDefinition is required");

			Assert.isTrue(!DefaultInterfaceDetector.DISABLED.equals(detector) || !ObjectUtils.isEmpty(interfaces),
					"No service interface(s) specified and auto-export "
							+ "discovery disabled; change at least one of these properties");
		}

		processTemplateDefinition();
		createEmbeddedBeanFactory();

		updateCallback = CMUtils.createCallback(autowireOnUpdate, updateMethod, beanFactory);

		registerService();
	}

	private void processTemplateDefinition() {
		// make sure the scope is singleton
		templateDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		// let the special invoker handle the destroy method
		String destroyMethod = templateDefinition.getDestroyMethodName();
		templateDefinition.setDestroyMethodName(null);
		// prevent Spring for calling DiposableBean (it's already handled by the invoker)
		templateDefinition.registerExternallyManagedDestroyMethod("destroy");
		destructionInvokerFactory = new DestructionInvokerCache(destroyMethod);
	}

	public void destroy() throws Exception {

		synchronized (monitor) {
			destroyed = true;
			// remove factory service
			OsgiServiceUtils.unregisterService(configurationWatcher);
			configurationWatcher = null;
			// destroy instances
			destroyFactory();
		}

		destructionInvokerFactory.cache.clear();
		destructionInvokerFactory = null;

		synchronized (serviceRegistrations) {
			serviceRegistrations.clear();
		}
	}

	private void createEmbeddedBeanFactory() {
		synchronized (monitor) {
			DefaultListableBeanFactory bf = new DefaultListableBeanFactory(owningBeanFactory);
			if (owningBeanFactory instanceof ConfigurableBeanFactory) {
				bf.copyConfigurationFrom((ConfigurableBeanFactory) owningBeanFactory);
			}
			// just to be on the safe side
			bf.setBeanClassLoader(classLoader);
			// add autowiring processor
			bf.addBeanPostProcessor(new InitialInjectionProcessor());

			beanFactory = bf;
		}
	}

	private void registerService() {
		synchronized (monitor) {
			Dictionary props = new Hashtable(2);
			props.put(Constants.SERVICE_PID, factoryPid);

			configurationWatcher =
					bundleContext.registerService(ManagedServiceFactory.class.getName(), new ConfigurationWatcher(),
							props);
		}
	}

	// no monitor since the calling method already holds it
	private void destroyFactory() {
		if (beanFactory != null) {
			// destroy singletons manually to prevent multiple calls of the destruction contract (DisposableBean)
			// being called by both the factory and the special invoker
			String[] singletonBeans = beanFactory.getSingletonNames();
			for (String sigletonName : singletonBeans) {
				Object singleton = beanFactory.getBean(sigletonName);
				beanFactory.removeBeanDefinition(sigletonName);
				ManagedFactoryDisposableInvoker invoker = destructionInvokerFactory.getInvoker(singleton.getClass());
				invoker.destroy(sigletonName, singleton, DestructionCodes.BUNDLE_STOPPING);
			}
			beanFactory = null;
		}
	}

	private void createOrUpdate(String pid, Map props) {
		synchronized (monitor) {
			if (destroyed)
				return;

			if (beanFactory.containsBean(pid)) {
				updateInstance(pid, props);
			} else {
				createInstance(pid, props);
			}
		}
	}

	private void createInstance(String pid, Map props) {
		synchronized (monitor) {
			if (destroyed)
				return;

			beanFactory.registerBeanDefinition(pid, templateDefinition);
			initialInjectionProperties = props;
			// create instance (causing the injection BPP to be applied)
			Object bean = beanFactory.getBean(pid);
			registerService(pid, bean);
		}
	}

	private void registerService(String pid, Object bean) {
		OsgiServiceFactoryBean exporter = createExporter(pid, bean);
		serviceExporters.put(pid, exporter);
		try {
			serviceRegistrations.add(exporter.getObject());
		} catch (Exception ex) {
			throw new BeanCreationException("Cannot publish bean for pid " + pid, ex);
		}
	}

	private OsgiServiceFactoryBean createExporter(String beanName, Object bean) {
		OsgiServiceFactoryBean exporter = new OsgiServiceFactoryBean();
		exporter.setInterfaceDetector(detector);
		exporter.setBeanClassLoader(classLoader);
		exporter.setBeanName(beanName);
		exporter.setBundleContext(bundleContext);
		exporter.setExportContextClassLoader(ccl);
		exporter.setInterfaces(interfaces);
		exporter.setListeners(listeners);
		exporter.setTarget(bean);

		// add properties
		Properties props = new Properties();
		if (serviceProperties != null) {
			props.putAll(serviceProperties);
		}
		// add the service pid (to be able to identify the bean instance)
		props.put(Constants.SERVICE_PID, beanName);
		exporter.setServiceProperties(props);

		try {
			exporter.afterPropertiesSet();
		} catch (Exception ex) {
			throw new BeanCreationException("Cannot publish bean for pid " + beanName, ex);
		}
		return exporter;
	}

	private void updateInstance(String pid, Map props) {
		if (updateCallback != null) {
			Object instance = beanFactory.getBean(pid);
			updateCallback.update(instance, props);
		}
	}

	private void destroyInstance(String pid) {
		synchronized (monitor) {
			// bail out fast
			if (destroyed)
				return;

			if (beanFactory.containsBeanDefinition(pid)) {
				unregisterService(pid);
				Object singleton = beanFactory.getBean(pid);
				// remove definition and instance
				beanFactory.removeBeanDefinition(pid);
				ManagedFactoryDisposableInvoker invoker = destructionInvokerFactory.getInvoker(singleton.getClass());
				invoker.destroy(pid, singleton, DestructionCodes.CM_ENTRY_DELETED);
			}
		}
	}

	private void unregisterService(String pid) {
		OsgiServiceFactoryBean exporterFactory = serviceExporters.remove(pid);

		if (exporterFactory != null) {

			Object registration = null;
			try {
				registration = exporterFactory.getObject();
			} catch (Exception ex) {
				// log the exception and continue
				log.error("Could not retrieve registration for pid " + pid, ex);
			}

			if (log.isTraceEnabled()) {
				log.trace("Unpublishing bean for pid " + pid + " w/ registration " + registration);
			}
			// remove service registration
			serviceRegistrations.remove(registration);

			exporterFactory.destroy();
		}
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		synchronized (monitor) {
			this.owningBeanFactory = beanFactory;
		}
	}

	public void setBundleContext(BundleContext bundleContext) {
		synchronized (monitor) {
			this.bundleContext = bundleContext;
		}
	}

	public Collection getObject() throws Exception {
		return userReturnedCollection;
	}

	public Class<Collection> getObjectType() {
		return Collection.class;
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Sets the listeners interested in registration and unregistration events.
	 * 
	 * @param listeners registration/unregistration listeners.
	 */
	public void setListeners(OsgiServiceRegistrationListener[] listeners) {
		if (listeners != null)
			this.listeners = listeners;
	}

	/**
	 * @param factoryPid The factoryPid to set.
	 */
	public void setFactoryPid(String factoryPid) {
		synchronized (monitor) {
			this.factoryPid = factoryPid;
		}
	}

	/**
	 * @param templateDefinition The templateDefinition to set.
	 */
	public void setTemplateDefinition(BeanDefinition[] templateDefinition) {
		if (templateDefinition != null && templateDefinition.length > 0) {
			this.templateDefinition = new RootBeanDefinition();
			this.templateDefinition.overrideFrom(templateDefinition[0]);
		} else {
			this.templateDefinition = null;
		}
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void setInterfaceDetector(InterfaceDetector detector) {
		this.detector = detector;
	}

	/**
	 * @param ccl The ccl to set.
	 */
	public void setExportContextClassLoader(ExportContextClassLoaderEnum ccl) {
		this.ccl = ccl;
	}

	/**
	 * @param interfaces The interfaces to set.
	 */
	public void setInterfaces(Class<?>[] interfaces) {
		this.interfaces = interfaces;
	}

	/**
	 * Sets whether autowire on update should be performed automatically or not.
	 * 
	 * @param autowireOnUpdate
	 */
	public void setAutowireOnUpdate(boolean autowireOnUpdate) {
		this.autowireOnUpdate = autowireOnUpdate;
	}

	/**
	 * @param updateMethod The updateMethod to set.
	 */
	public void setUpdateMethod(String updateMethod) {
		this.updateMethod = updateMethod;
	}

	/**
	 * Sets the properties used when exposing the target as an OSGi service. If the given argument implements (
	 * {@link ServicePropertiesChangeListener}), any updates to the properties will be reflected by the service
	 * registration.
	 * 
	 * @param serviceProperties properties used for exporting the target as an OSGi service
	 */
	public void setServiceProperties(Map serviceProperties) {
		this.serviceProperties = serviceProperties;
	}
}