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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.osgi.framework.BundleContext;

/**
 * @author Costin Leau
 * 
 */
public class AbstractOsgiPlatformTest {

	private AbstractOsgiPlatform platform;
	private Properties prop = new Properties();

	@Before
	public void setup() throws Exception {

		final BundleContext ctx = new MockBundleContext();
		prop.setProperty("foo", "bar");

		platform = new AbstractOsgiPlatform() {

			Properties getPlatformProperties() {
				return prop;
			}

			public BundleContext getBundleContext() {
				return ctx;
			}

			public void start() throws Exception {
			}

			public void stop() throws Exception {
			}

		};
	}

	@After
	public void tearDown() throws Exception {
		prop = null;
		platform = null;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.test.platform.AbstractOsgiPlatform#getConfigurationProperties()}.
	 */
	@Test
	public void testGetConfigurationProperties() {
		Properties cfg = platform.getConfigurationProperties();
		assertNotNull(cfg);
		Properties sysCfg = System.getProperties();
		for (Iterator iterator = sysCfg.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			assertSame(entry.getValue(), cfg.get(entry.getKey()));
		}
		assertEquals("bar", cfg.getProperty("foo"));

		prop.setProperty("abc", "xyz");
		Properties otherCfg = platform.getConfigurationProperties();
		assertSame(cfg, otherCfg);
		assertFalse(cfg.contains("abc"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.test.platform.AbstractOsgiPlatform#getPlatformProperties()}.
	 */
	@Test
	public void testGetPlatformProperties() {
		assertSame(prop, platform.getPlatformProperties());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.test.platform.AbstractOsgiPlatform#createTempDir(java.lang.String)}.
	 */
	@Test
	public void testCreateTempDir() {
		File tmpDir = platform.createTempDir("bla");
		assertNotNull(tmpDir);
		assertTrue(tmpDir.exists());
		tmpDir.delete();
	}

}
