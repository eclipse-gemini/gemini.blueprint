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

package org.eclipse.gemini.blueprint.extender;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Extender hook that allows custom modification of an application context's
 * bean definitions. New beans can be created, removed or existing definitions
 * modified.
 * 
 * <p/> Similar in functionality with Spring's BeanFactoryPostProcessor, this
 * interface also considers the BundleContext in which the beanFactory runs.
 * 
 * <p/>Just like the BeanFactoryPostProcessor, the post processing happens
 * during the creation of the bean factory but before any beans (including
 * declared BeanFactoryPostProcessors) are initialized.
 * 
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
 * @see BundleContext
 * 
 * @author Costin Leau
 */
public interface OsgiBeanFactoryPostProcessor {

	/**
	 * 
	 * Modifies the application context's internal bean factory after its
	 * standard initialization. All bean definitions will have been loaded, but
	 * no beans will have been instantiated yet. This allows for overriding or
	 * adding properties even to eager-initializing beans.
	 * 
	 * @param bundleContext bundle
	 * @param beanFactory the bean factory used by the application context
	 * @throws BeansException in case of factory errors
	 * @throws InvalidSyntaxException in case of OSGi filters errors
	 * @throws BundleException in case of OSGi bundle errors
	 */
	void postProcessBeanFactory(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory)
			throws BeansException, InvalidSyntaxException, BundleException;
}
