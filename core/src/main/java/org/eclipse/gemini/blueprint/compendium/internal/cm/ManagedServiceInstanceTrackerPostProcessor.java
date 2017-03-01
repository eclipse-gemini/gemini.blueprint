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

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * Post processor tracking the creation and destruction of managed service instances. The instances tracked are subject
 * to Configuration Admin based injection.
 * 
 * @author Costin Leau
 * 
 */
public class ManagedServiceInstanceTrackerPostProcessor implements BeanFactoryAware, BundleContextAware,
		InitializingBean, BeanPostProcessor, DestructionAwareBeanPostProcessor, DisposableBean {

	private final String trackedBean;
	private DefaultManagedServiceBeanManager managedServiceManager;
	private String pid;
	private String updateMethod;
	private boolean autowireOnUpdate = false;
	private BundleContext bundleContext;
	private BeanFactory beanFactory;

	public ManagedServiceInstanceTrackerPostProcessor(String beanNameToTrack) {
		this.trackedBean = beanNameToTrack;
	}

	public void afterPropertiesSet() throws Exception {
		ConfigurationAdminManager cam = new ConfigurationAdminManager(pid, bundleContext);
		managedServiceManager = new DefaultManagedServiceBeanManager(autowireOnUpdate, updateMethod, cam, beanFactory);
	}

	public void destroy() throws Exception {
		managedServiceManager.destroy();
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		// register the instance (if needed)
		if (trackedBean.equals(beanName)) {
			return managedServiceManager.register(bean);
		}
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		if (trackedBean.equals(beanName)) {
			managedServiceManager.unregister(bean);
		}
	}

	/**
	 * @param o is ignored.
	 * @return always <code>true</code> as this post processor is unaware of bean instances and
	 *          will determine whether a bean requires destruction using the bean name in {@link #postProcessBeforeDestruction(Object, String)}.
	 */
	@Override
	public boolean requiresDestruction(Object o) {
		return true;
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * Sets the pid for the configuration manager.
	 * 
	 * @param pid The pid to set.
	 */
	public void setPersistentId(String pid) {
		this.pid = pid;
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
	 * Sets the method name, for bean-managed update strategy.
	 * 
	 * @param updateMethod The updateMethod to set.
	 */
	public void setUpdateMethod(String methodName) {
		this.updateMethod = methodName;
	}
}