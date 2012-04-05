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

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.osgi.framework.BundleContext;

/**
 * Equinox (3.2.x) OSGi platform.
 * 
 * @author Costin Leau
 * 
 */
public class EquinoxPlatform extends AbstractOsgiPlatform {

	private BundleContext context;


	public EquinoxPlatform() {
		toString = "Equinox OSGi Platform";
	}

	Properties getPlatformProperties() {
		// default properties
		Properties props = new Properties();
		props.setProperty("eclipse.ignoreApp", "true");
		props.setProperty("osgi.clean", "true");
		props.setProperty("osgi.noShutdown", "true");

		// local temporary folder for running tests
		// prevents accidental rewrites
		props.setProperty("osgi.configuration.area", "./target/eclipse_config");
		props.setProperty("osgi.instance.area", "./target/eclipse_config");
		props.setProperty("osgi.user.area", "./target/eclipse_config");

		// props.setProperty("eclipse.consoleLog", "true");
		// props.setProperty("osgi.debug", "");

		return props;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	public void start() throws Exception {

		if (context == null) {
			// copy configuration properties to sys properties
			System.getProperties().putAll(getConfigurationProperties());

			// Equinox 3.1.x returns void - use of reflection is required
			// use main since in 3.1.x it sets up some system properties
			EclipseStarter.main(new String[0]);

			final Field field = EclipseStarter.class.getDeclaredField("context");

			AccessController.doPrivileged(new PrivilegedAction<Object>() {
				public Object run() {
					field.setAccessible(true);
					return null;
				}
			});
			context = (BundleContext) field.get(null);
		}
	}

	public void stop() throws Exception {
		if (context != null) {
			context = null;
			EclipseStarter.shutdown();
		}
	}
}