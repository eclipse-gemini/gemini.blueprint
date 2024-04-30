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


package org.eclipse.gemini.blueprint.extensions.annotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.service.importer.support.*;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * <code>BeanPostProcessor</code> that processed annotation to inject
 * Spring-DM managed OSGi services.
 *
 * @author Andy Piper
 */
public class ServiceReferenceInjectionBeanPostProcessor implements
		BundleContextAware, BeanFactoryAware, BeanClassLoaderAware, InstantiationAwareBeanPostProcessor {

	private BundleContext bundleContext;

	private static Log logger = LogFactory.getLog(ServiceReferenceInjectionBeanPostProcessor.class);

	private BeanFactory beanFactory;

	private ClassLoader classLoader;


	private abstract static class ImporterCallAdapter {

		static void setInterfaces(Object importer, Class<?>[] classes) {
			if (importer instanceof OsgiServiceProxyFactoryBean)
				((OsgiServiceProxyFactoryBean) importer).setInterfaces(classes);
			else
				((OsgiServiceCollectionProxyFactoryBean) importer).setInterfaces(classes);
		}

		static void setBundleContext(Object importer, BundleContext context) {
			((BundleContextAware) importer).setBundleContext(context);
		}

		static void setBeanClassLoader(Object importer, ClassLoader cl) {
			((BeanClassLoaderAware) importer).setBeanClassLoader(cl);
		}

		static void setCardinality(Object importer, Availability cardinality) {
			if (importer instanceof OsgiServiceProxyFactoryBean)
				((OsgiServiceProxyFactoryBean) importer).setAvailability(cardinality);
			else
				((OsgiServiceCollectionProxyFactoryBean) importer).setAvailability(cardinality);
		}

        static void setGreedyProxying(Object importer, boolean greedy) {
            if (importer instanceof OsgiServiceCollectionProxyFactoryBean) {
                ((OsgiServiceCollectionProxyFactoryBean) importer).setGreedyProxying(greedy);
            }
        }

        static void setSticky(Object importer, boolean sticky) {
            if (importer instanceof OsgiServiceProxyFactoryBean) {
                ((OsgiServiceProxyFactoryBean) importer).setSticky(sticky);
            }
        }

		static void afterPropertiesSet(Object importer) throws Exception {
			((InitializingBean) importer).afterPropertiesSet();
		}

		static void setFilter(Object importer, String filter) throws Exception {
			if (importer instanceof OsgiServiceProxyFactoryBean)
				((OsgiServiceProxyFactoryBean) importer).setFilter(filter);
			else
				((OsgiServiceCollectionProxyFactoryBean) importer).setFilter(filter);
		}

		static void setContextClassLoader(Object importer, ImportContextClassLoaderEnum ccl) {
			if (importer instanceof OsgiServiceProxyFactoryBean)
				((OsgiServiceProxyFactoryBean) importer).setImportContextClassLoader(ccl);
			else
				((OsgiServiceCollectionProxyFactoryBean) importer).setImportContextClassLoader(ccl);
		}

		static void setServiceBean(Object importer, String name) {
			if (importer instanceof OsgiServiceProxyFactoryBean)
				((OsgiServiceProxyFactoryBean) importer).setServiceBeanName(name);
			else
				((OsgiServiceCollectionProxyFactoryBean) importer).setServiceBeanName(name);
		}
	}


	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Perform field-based service injection as fields are not part of the
	 * {@link #postProcessPropertyValues(PropertyValues, PropertyDescriptor[], Object, String) property-based intitialization}.
	 */
	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
		injectServicesViaAnnotatedFields(bean, beanName);
		return bean;
	}

	/**
	 * process FactoryBean created objects, since these will not have had services injected.
	 */
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
		if (logger.isDebugEnabled())
			logger.debug("processing [" + bean.getClass().getName() + ", " + beanName + "]");
		// Catch FactoryBean created instances.
		if (!(bean instanceof FactoryBean) && beanFactory.containsBean(BeanFactory.FACTORY_BEAN_PREFIX + beanName)) {
			injectServicesViaAnnotatedSetterMethods(bean, beanName);
			injectServicesViaAnnotatedFields(bean, beanName);
		}
		return bean;
	}

	/* private version of the injector can use */
	private void injectServicesViaAnnotatedSetterMethods(final Object bean, final String beanName) {
		ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {

			public void doWith(Method method) {
				ServiceReference s = AnnotationUtils.getAnnotation(method, ServiceReference.class);
				if (s != null && method.getParameterTypes().length == 1) {
					try {
						if (logger.isDebugEnabled())
							logger.debug("Processing annotation [" + s + "] for [" + bean.getClass().getName() + "."
									+ method.getName() + "()] on bean [" + beanName + "]");
						method.invoke(bean, getServiceImporter(s, method, beanName).getObject());
					}
					catch (Exception e) {
						throw new IllegalArgumentException("Error processing service annotation", e);
					}
				}
			}
		});
	}

	private void injectServicesViaAnnotatedFields(final Object bean, final String beanName) {
		ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
			public void doWith(Field field) {
				ServiceReference s = AnnotationUtils.getAnnotation(field, ServiceReference.class);
				if (s != null && !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
					try {
						if (logger.isDebugEnabled())
							logger.debug("Processing annotation [" + s + "] for [" + field + "] on bean [" + beanName + "]");
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						ReflectionUtils.setField(field, bean, getServiceImporter(s, field.getType(), beanName).getObject());
					}
					catch (Exception e) {
						throw new IllegalArgumentException("Error processing service annotation", e);
					}
				}
			}
		});
	}

	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean,
			String beanName) throws BeansException {

		MutablePropertyValues newprops = new MutablePropertyValues(pvs);
		for (PropertyDescriptor pd : pds) {
			ServiceReference s = hasServiceProperty(pd);
			if (s != null && !pvs.contains(pd.getName())) {
				try {
					if (logger.isDebugEnabled())
						logger.debug("Processing annotation [" + s + "] for [" + beanName + "." + pd.getName() + "]");
					FactoryBean importer = getServiceImporter(s, pd.getWriteMethod(), beanName);
					// BPPs are created in stageOne(), even though they are run in stageTwo(). This check means that
					// the call to getObject() will not fail with ServiceUnavailable. This is safe to do because
					// ServiceReferenceDependencyBeanFactoryPostProcessor will ensure that mandatory services are
					// satisfied before stageTwo() is run.
					if (bean instanceof BeanPostProcessor) {
						ImporterCallAdapter.setCardinality(importer, Availability.OPTIONAL);
					}
					newprops.addPropertyValue(pd.getName(), importer.getObject());
				}
				catch (Exception e) {
					throw new FatalBeanException("Could not create service reference", e);
				}
			}
		}
		return newprops;
	}

	private FactoryBean getServiceImporter(ServiceReference s, Method writeMethod, String beanName) throws Exception {
		Class<?>[] params = writeMethod.getParameterTypes();
		if (params.length != 1) {
			throw new IllegalArgumentException("Setter for [" + beanName + "] must have only one argument");
		}
		return getServiceImporter(s, params[0], beanName);
	}

	private FactoryBean getServiceImporter(ServiceReference s, Class<?> serviceType, String beanName) throws Exception {
		// Invocations will block here, so although the ApplicationContext is created. Nothing will proceed until all the dependencies are satisfied.
		if (Collection.class.isAssignableFrom(serviceType)) {
			return getServiceProperty(new OsgiServiceCollectionProxyFactoryBean(), s, serviceType, beanName);
		}
		else {
			return getServiceProperty(new OsgiServiceProxyFactoryBean(), s, serviceType, beanName);
		}
	}

	private boolean impliedServiceType(ServiceReference s) {
		return (s.serviceTypes() == null || s.serviceTypes().length == 0 || (s.serviceTypes().length == 1 && s.serviceTypes()[0].equals(ServiceReference.class)));
	}

	// Package protected for testing
	private FactoryBean getServicePropertyInternal(FactoryBean pfb, ServiceReference s, Class<?> serviceType,
			String beanName) throws Exception {
		if (s.filter().length() > 0) {
			ImporterCallAdapter.setFilter(pfb, s.filter());
		}
		if (impliedServiceType(s)) {
			if (Collection.class.isAssignableFrom(serviceType)) {
				throw new IllegalArgumentException("Cannot infer type for collection-based reference [" + beanName + "]");
			} else {
				ImporterCallAdapter.setInterfaces(pfb, new Class<?>[] { serviceType });
			}
		}
		else {
			ImporterCallAdapter.setInterfaces(pfb, s.serviceTypes());
		}
		ImporterCallAdapter.setCardinality(pfb, s.cardinality());
		ImporterCallAdapter.setSticky(pfb, s.sticky());
		ImporterCallAdapter.setContextClassLoader(pfb, s.contextClassLoader().toImportContextClassLoader());
		ImporterCallAdapter.setBundleContext(pfb, bundleContext);

		if (s.serviceBeanName().length() > 0) {
			ImporterCallAdapter.setServiceBean(pfb, s.serviceBeanName());
		}
		ImporterCallAdapter.setBeanClassLoader(pfb, classLoader);
		ImporterCallAdapter.afterPropertiesSet(pfb);
		return pfb;
	}

	FactoryBean getServiceProperty(OsgiServiceProxyFactoryBean pfb, ServiceReference s,
			Class<?> serviceType, String beanName) throws Exception {
		pfb.setTimeout(s.timeout());
		return getServicePropertyInternal(pfb, s, serviceType, beanName);
	}

	FactoryBean getServiceProperty(OsgiServiceCollectionProxyFactoryBean pfb, ServiceReference s,
			Class<?> serviceType, String beanName) throws Exception {
		if (SortedSet.class.isAssignableFrom(serviceType)) {
			pfb.setCollectionType(CollectionType.SORTED_SET);
		}
		else if (Set.class.isAssignableFrom(serviceType)) {
			pfb.setCollectionType(CollectionType.SET);
		}
		else if (List.class.isAssignableFrom(serviceType)) {
			pfb.setCollectionType(CollectionType.LIST);
		}
		else {
			throw new IllegalArgumentException("Setter for [" + beanName
					+ "] does not have a valid Collection type argument");
		}
		return getServicePropertyInternal(pfb, s, serviceType, beanName);
	}

	protected ServiceReference hasServiceProperty(PropertyDescriptor propertyDescriptor) {
		Method setter = propertyDescriptor.getWriteMethod();
		return setter != null ? AnnotationUtils.getAnnotation(setter, ServiceReference.class) : null;
	}

	public void setBundleContext(BundleContext context) {
		this.bundleContext = context;

	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}
