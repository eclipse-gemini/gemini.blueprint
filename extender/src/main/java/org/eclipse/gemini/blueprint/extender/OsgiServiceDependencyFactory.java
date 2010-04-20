/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.extender;

import java.util.Collection;

import org.eclipse.gemini.blueprint.service.importer.OsgiServiceDependency;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Interface to be implemented by beans wishing to provide OSGi service
 * dependencies required by the
 * {@link org.springframework.context.ApplicationContext}. By default, the
 * extender will postpone the context initialization until the dependencies (to
 * OSGi services) are all satisfied at the same time.
 * 
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor *
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
public interface OsgiServiceDependencyFactory {

	/**
	 * Returns the OSGi service dependencies applying for the given bean factory
	 * running inside the given bundle context. The returned collection should
	 * contain only {@link OsgiServiceDependency} objects.
	 * 
	 * @param bundleContext bundle
	 * @param beanFactory the bean factory used by the application context
	 * @return collection of service dependencies
	 * @throws BeansException in case of factory errors
	 * @throws InvalidSyntaxException in case of OSGi filters errors
	 * @throws BundleException in case of OSGi bundle errors
	 */
	Collection<OsgiServiceDependency> getServiceDependencies(BundleContext bundleContext,
			ConfigurableListableBeanFactory beanFactory) throws BeansException, InvalidSyntaxException, BundleException;
}
