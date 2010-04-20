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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.extender.OsgiBeanFactoryPostProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Simple adapter for wrapping OsgiBeanPostProcessors to normal Spring post
 * processors.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiBeanFactoryPostProcessorAdapter implements BeanFactoryPostProcessor {

	/** logger */
	private static final Log log = LogFactory.getLog(OsgiBeanFactoryPostProcessorAdapter.class);

	private final BundleContext bundleContext;

	private List<OsgiBeanFactoryPostProcessor> osgiPostProcessors;


	public OsgiBeanFactoryPostProcessorAdapter(BundleContext bundleContext,
			List<OsgiBeanFactoryPostProcessor> postProcessors) {
		this.bundleContext = bundleContext;
		this.osgiPostProcessors = postProcessors;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		boolean trace = log.isTraceEnabled();

		Exception processingException = null;

		for (Iterator<OsgiBeanFactoryPostProcessor> iterator = osgiPostProcessors.iterator(); iterator.hasNext();) {
			OsgiBeanFactoryPostProcessor osgiPostProcessor = iterator.next();
			if (trace)
				log.trace("Calling OsgiBeanFactoryPostProcessor " + osgiPostProcessor + " for bean factory "
						+ beanFactory);

			try {
				osgiPostProcessor.postProcessBeanFactory(bundleContext, beanFactory);
			}
			catch (InvalidSyntaxException ex) {
				processingException = ex;
			}
			catch (BundleException ex) {
				processingException = ex;
			}

			if (processingException != null) {
				if (log.isDebugEnabled())
					log.debug("PostProcessor " + osgiPostProcessor + " threw exception", processingException);
				throw new FatalBeanException("Error encountered while executing OSGi post processing",
					processingException);
			}
		}
	}
}