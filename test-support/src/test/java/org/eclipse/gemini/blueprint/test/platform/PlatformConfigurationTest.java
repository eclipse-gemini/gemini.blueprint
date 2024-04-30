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

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

import org.osgi.framework.BundleContext;

/**
 * Unit test for the platform configuration properties.
 * 
 * @author Costin Leau
 * 
 */
public class PlatformConfigurationTest {

	private OsgiPlatform platform;

	@Test
	public void testOverridenConfiguration() throws Exception {
		platform = new AbstractOsgiPlatform() {

			Properties getPlatformProperties() {
				Properties props = new Properties();
				props.put("foo", "bar");
				return props;
			}

			public BundleContext getBundleContext() {
				return null;
			}

			public void start() throws Exception {
			}

			public void stop() throws Exception {
			}

		};

		Properties props = platform.getConfigurationProperties();
		props.setProperty("some.prop", "valueA");
		props.setProperty("other.prop", "valueB");
		// override default property
		props.setProperty("foo", "extra-bar");

		Properties test = platform.getConfigurationProperties();
		assertEquals("valueA", test.getProperty("some.prop"));
		assertEquals("valueB", test.getProperty("other.prop"));
		assertEquals("extra-bar", test.getProperty("foo"));
	}
}
