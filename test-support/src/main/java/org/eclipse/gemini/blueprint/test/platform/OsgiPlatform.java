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

package org.eclipse.gemini.blueprint.test.platform;

import java.util.Properties;

import org.osgi.framework.BundleContext;

/**
 * Lifecycle contract for the OSGi platform.
 * 
 * @author Costin Leau
 * 
 */
public interface OsgiPlatform {

	/**
	 * Starts the OSGi platform.
	 * 
	 * @throws Exception if starting the platform fails
	 */
	void start() throws Exception;

	/**
	 * Stops the OSGi platform.
	 * 
	 * @throws Exception if stopping the platform fails.
	 */
	void stop() throws Exception;

	/**
	 * Returns the {@link java.util.Properties} object used for configuring the
	 * underlying OSGi implementation before starting it.
	 * 
	 * @return platform implementation specific properties
	 */
	Properties getConfigurationProperties();

	/**
	 * Returns the bundle context of the returned platform. Useful during
	 * startup for installing bundles and interacting with the OSGi instance.
	 * 
	 * @return platform bundle context
	 */
	BundleContext getBundleContext();
}
