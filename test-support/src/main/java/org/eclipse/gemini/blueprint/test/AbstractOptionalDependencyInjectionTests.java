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

package org.eclipse.gemini.blueprint.test;

import java.io.IOException;

import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.support.AbstractDelegatedExecutionApplicationContext;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.util.ObjectUtils;

/**
 * JUnit superclass, which creates an empty OSGi bundle appCtx when no
 * configuration file is specified. Required for mixing Spring existing testing
 * hierarchy with the OSGi testing framework functionality.
 * 
 * @author Costin Leau
 * 
 */
public abstract class AbstractOptionalDependencyInjectionTests extends AbstractDependencyInjectionSpringContextTests {

	// The OSGi BundleContext (when executing the test as a bundle inside OSGi)
	protected BundleContext bundleContext;


	/**
	 * Empty OSGi application context that doesn't require any files to be
	 * specified.
	 * 
	 * Useful to still get injection of bundleContext and OSGi specific resource
	 * loading.
	 * 
	 * @author Costin Leau
	 * 
	 */
	// the disposable interface is added just so that byte code detect the org.springframework.beans.factory package
	private static class EmptyOsgiApplicationContext extends AbstractDelegatedExecutionApplicationContext implements DisposableBean {
		protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {}
	}


	/**
	 * Default constructor. Constructs a new
	 * <code>AbstractOptionalDependencyInjectionTests</code> instance.
	 * 
	 */
	public AbstractOptionalDependencyInjectionTests() {
		super();
	}

	/**
	 * 
	 * Constructs a new <code>AbstractOptionalDependencyInjectionTests</code>
	 * instance.
	 * 
	 * @param name test name
	 */
	public AbstractOptionalDependencyInjectionTests(String name) {
		super(name);
	}

	protected boolean isContextKeyEmpty(Object key) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p/>This implementation will create an empty bundle context in case no
	 * locations are specified.
	 */
	protected ConfigurableApplicationContext createApplicationContext(String[] locations) {

		ConfigurableOsgiBundleApplicationContext context = null;

		if (ObjectUtils.isEmpty(locations)) {
			context = new EmptyOsgiApplicationContext();
        } else {
			context = new OsgiBundleXmlApplicationContext(locations);
        }

		context.setBundleContext(bundleContext);
		context.refresh();
		return context;
	}

}
