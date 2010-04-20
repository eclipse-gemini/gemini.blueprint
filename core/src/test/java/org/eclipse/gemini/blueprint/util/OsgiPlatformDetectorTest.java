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

package org.eclipse.gemini.blueprint.util;

import java.util.Properties;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.util.OsgiPlatformDetector;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * @author Adrian Colyer
 *
 */
public class OsgiPlatformDetectorTest extends TestCase {

	private Bundle mockBundle;
	private Properties props;
	
	protected void setUp() throws Exception {
		super.setUp();
		this.mockBundle = new MockBundle();
		this.props = new Properties();
	}
	
	public void testEquinoxDetection() {
		props.put(Constants.FRAMEWORK_VENDOR,"Eclipse");
		BundleContext bc = new MockBundleContext(mockBundle,props);
		assertTrue("Detected as Equinox",OsgiPlatformDetector.isEquinox(bc));
		assertFalse(OsgiPlatformDetector.isKnopflerfish(bc));
		assertFalse(OsgiPlatformDetector.isFelix(bc));
	}
	
	public void testKnopflerfishDetection() {
		props.put(Constants.FRAMEWORK_VENDOR,"Knopflerfish");
		BundleContext bc = new MockBundleContext(mockBundle,props);
		assertTrue("Detected as Knopflerfish",OsgiPlatformDetector.isKnopflerfish(bc));		
		assertFalse(OsgiPlatformDetector.isEquinox(bc));
		assertFalse(OsgiPlatformDetector.isFelix(bc));
	}
	
	public void testFelixDetection() {
		props.put(Constants.FRAMEWORK_VENDOR,"Apache Software Foundation");
		BundleContext bc = new MockBundleContext(mockBundle,props);
		assertTrue("Detected as Felix",OsgiPlatformDetector.isFelix(bc));		
		assertFalse(OsgiPlatformDetector.isEquinox(bc));
		assertFalse(OsgiPlatformDetector.isKnopflerfish(bc));
	}
	
}
