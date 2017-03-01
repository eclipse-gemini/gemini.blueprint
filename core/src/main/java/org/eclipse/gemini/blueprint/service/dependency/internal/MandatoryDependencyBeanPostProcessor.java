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

package org.eclipse.gemini.blueprint.service.dependency.internal;

import org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean;
import org.eclipse.gemini.blueprint.service.exporter.support.internal.controller.ExporterControllerUtils;
import org.eclipse.gemini.blueprint.service.exporter.support.internal.controller.ExporterInternalActions;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * BeanPostProcessor registered for detecting the dependency between service importer and service exporters. Besides
 * bean detection, this component also listens to specific importer events to determine whether a potential associated
 * exporter needs to be disabled temporarily.
 * 
 * @author Costin Leau
 * 
 */
public class MandatoryDependencyBeanPostProcessor implements BeanFactoryAware, BeanPostProcessor,
		DestructionAwareBeanPostProcessor {

	private MandatoryServiceDependencyManager manager;
	private ConfigurableBeanFactory beanFactory;

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof OsgiServiceFactoryBean && beanFactory.containsLocalBean(beanName)) {
			manager.addServiceExporter(bean, beanName);
		}
		return bean;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		// disable publication until all the dependencies have been fulfilled
		
		// ignore inner beans
		if (bean instanceof OsgiServiceFactoryBean && beanFactory.containsLocalBean(beanName)) {
			String exporterName = beanName;
			if (beanFactory.isFactoryBean(beanName)) {
				exporterName = BeanFactory.FACTORY_BEAN_PREFIX + beanName;
			}
			// if it's a singleton, then disable publication, otherwise ignore it
			if (beanFactory.isSingleton(exporterName)) {
				// get controller
				ExporterInternalActions controller = ExporterControllerUtils.getControllerFor(bean);
				controller.registerServiceAtStartup(false);
			}
		}
		return bean;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		DefaultMandatoryDependencyManager manager = new DefaultMandatoryDependencyManager();
		manager.setBeanFactory(beanFactory);
		this.manager = manager;
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		if (bean instanceof OsgiServiceFactoryBean && beanFactory.containsLocalBean(beanName)) {
			manager.removeServiceExporter(bean, beanName);
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
}