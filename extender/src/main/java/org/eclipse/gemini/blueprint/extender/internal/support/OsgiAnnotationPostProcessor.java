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

package org.eclipse.gemini.blueprint.extender.internal.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.OsgiException;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.extender.OsgiBeanFactoryPostProcessor;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Post processor used for processing Spring-DM annotations.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiAnnotationPostProcessor implements OsgiBeanFactoryPostProcessor {

	/** logger */
	private static final Log log = LogFactory.getLog(OsgiAnnotationPostProcessor.class);

	/** service reference bpp */
	private static final String ANNOTATION_BPP_CLASS = "org.eclipse.gemini.blueprint.extensions.annotation.ServiceReferenceInjectionBeanPostProcessor";


	public void postProcessBeanFactory(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory)
			throws BeansException, OsgiException {

		Bundle bundle = bundleContext.getBundle();
		try {
			// Try and load the annotation code using the bundle classloader
			Class<?> annotationBppClass = bundle.loadClass(ANNOTATION_BPP_CLASS);
			// instantiate the class
			final BeanPostProcessor annotationBeanPostProcessor = (BeanPostProcessor) BeanUtils.instantiateClass(annotationBppClass);

			// everything went okay so configure the BPP and add it to the BF
			((BeanFactoryAware) annotationBeanPostProcessor).setBeanFactory(beanFactory);
			((BeanClassLoaderAware) annotationBeanPostProcessor).setBeanClassLoader(beanFactory.getBeanClassLoader());
			((BundleContextAware) annotationBeanPostProcessor).setBundleContext(bundleContext);
			beanFactory.addBeanPostProcessor(annotationBeanPostProcessor);
		}
		catch (ClassNotFoundException exception) {
			log.info("Spring-DM annotation package could not be loaded from bundle ["
					+ OsgiStringUtils.nullSafeNameAndSymName(bundle) + "]; annotation processing disabled...");
			if (log.isDebugEnabled())
				log.debug("Cannot load annotation injection processor", exception);
		}
	}
}
