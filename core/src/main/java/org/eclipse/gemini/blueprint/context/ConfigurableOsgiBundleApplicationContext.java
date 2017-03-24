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

package org.eclipse.gemini.blueprint.context;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Interface that extends <code>ConfigurableApplicationContext</code> to
 * provides OSGi specific functionality.
 * 
 * <p>
 * <strong>Note:</strong> Just like its ancestors,the setters of this interface
 * should be called before <code>refresh</code>ing the
 * <code>ApplicationContext</code>
 * 
 * @author Costin Leau
 */
public interface ConfigurableOsgiBundleApplicationContext extends ConfigurableApplicationContext {

	/**
	 * Service entry used for specifying the application context name when
	 * published as an OSGi service
	 */
	static final String APPLICATION_CONTEXT_SERVICE_PROPERTY_NAME = "org.eclipse.gemini.blueprint.context.service.name";
	
	/**
	 * Compatibility (with Spring DM) service entry used for specifying the application context name when
	 * published as an OSGi service
	 */
	static final String SPRING_DM_APPLICATION_CONTEXT_SERVICE_PROPERTY_NAME = "org.springframework.context.service.name";

	/**
	 * Name of the bundle context bean
	 */
	static final String BUNDLE_CONTEXT_BEAN_NAME = "bundleContext";

	/**
	 * Name of the bundle bean
	 */
	static final String BUNDLE_BEAN_NAME = "bundle";


	/**
	 * Sets the config locations for this OSGi bundle application context. If
	 * not set, the implementation is supposed to use a default for the given
	 * bundle.
	 * 
	 * @param configLocations array of configuration locations
	 */
	void setConfigLocations(String... configLocations);

	/**
	 * Sets the <code>BundleContext</code> used by this OSGi bundle
	 * application context. Normally it's the <code>BundleContext</code> in
	 * which the context runs.
	 * 
	 * <p>
	 * Does not cause an initialization of the context: {@link #refresh()} needs
	 * to be called after the setting of all configuration properties.
	 * 
	 * @param bundleContext the <code>BundleContext</code> used by this
	 * application context.
	 * @see #refresh()
	 */
	void setBundleContext(BundleContext bundleContext);

	/**
	 * Return the <code>BundleContext</code> for this application context.
	 * This method is offered as a helper since as of OSGi 4.1, the bundle
	 * context can be discovered directly from the given bundle.
	 * 
	 * @return the <code>BundleContext</code> in which this application
	 * context runs
	 * 
	 * @see #getBundle()
	 */
	BundleContext getBundleContext();

	/**
	 * Returns the OSGi <code>Bundle</code> for this application context.
	 * 
	 * @return the <code>Bundle</code> for this OSGi bundle application
	 * context.
	 */
	Bundle getBundle();

	/**
	 * Indicates whether this application context should be publish as an OSGi
	 * service if successfully started. By default, this is set to
	 * <code>true</code>.
	 * 
	 * @param publishContextAsService true if the application context should be
	 * published as a service, false otherwise
	 */
	void setPublishContextAsService(boolean publishContextAsService);
}
