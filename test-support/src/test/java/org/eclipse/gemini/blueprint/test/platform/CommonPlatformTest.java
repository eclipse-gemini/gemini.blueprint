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

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;

/**
 * @author Costin Leau
 * 
 */
public abstract class CommonPlatformTest extends TestCase {

	private AbstractOsgiPlatform platform;
	private String systemPackages = "";

	protected void setUp() throws Exception {
		systemPackages = System.getProperty("org.osgi.framework.system.packages", "");
		platform = createPlatform();
	}

	protected void tearDown() throws Exception {
		platform.stop();
		platform = null;
		System.setProperty("org.osgi.framework.system.packages", systemPackages);
	}

	abstract AbstractOsgiPlatform createPlatform();

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.test.platform.FelixPlatform#getPlatformProperties()}.
	 */
	public void testGetPlatformProperties() {
		assertNotNull(platform.getPlatformProperties());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.test.platform.FelixPlatform#start()}.
	 */
	public void testStart() throws Exception {
		assertNull(platform.getBundleContext());
		platform.start();
		assertNotNull(platform.getBundleContext());
	}

	public void testMultipleStart() throws Exception {
		platform.start();
		BundleContext ctx = platform.getBundleContext();
		platform.start();
		assertSame(ctx, platform.getBundleContext());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.test.platform.FelixPlatform#stop()}.
	 */
	public void testStop() throws Exception {
		assertNull(platform.getBundleContext());
		platform.start();
		assertNotNull(platform.getBundleContext());
		platform.stop();
		assertNull(platform.getBundleContext());
	}

	public void testMultipleStop() throws Exception {
		platform.start();
		assertNotNull(platform.getBundleContext());
		platform.stop();
		assertNull(platform.getBundleContext());
		platform.stop();
		assertNull(platform.getBundleContext());
	}
}