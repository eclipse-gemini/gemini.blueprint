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

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.osgi.framework.Bundle;
import org.eclipse.gemini.blueprint.mock.MockBundle;

/**
 * @author Costin Leau
 * 
 */
public class OsgiBundleUtilsTest extends TestCase {

	private Bundle bundle;

	private static int state;

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		OsgiBundleUtilsTest.state = Bundle.UNINSTALLED;
		bundle = new MockBundle() {
			public int getState() {
				return state;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		bundle = null;
	}

	public void testIsInActiveBundleState() throws Exception {
		OsgiBundleUtilsTest.state = Bundle.ACTIVE;
		assertTrue(OsgiBundleUtils.isBundleActive(bundle));

		OsgiBundleUtilsTest.state = Bundle.STARTING;
		assertFalse(OsgiBundleUtils.isBundleActive(bundle));

		OsgiBundleUtilsTest.state = Bundle.INSTALLED;
		assertFalse(OsgiBundleUtils.isBundleActive(bundle));
	}

	public void testIsBundleResolved() throws Exception {
		OsgiBundleUtilsTest.state = Bundle.UNINSTALLED;
		assertFalse(OsgiBundleUtils.isBundleResolved(bundle));

		OsgiBundleUtilsTest.state = Bundle.INSTALLED;
		assertFalse(OsgiBundleUtils.isBundleResolved(bundle));

		OsgiBundleUtilsTest.state = Bundle.ACTIVE;
		assertTrue(OsgiBundleUtils.isBundleResolved(bundle));

		OsgiBundleUtilsTest.state = Bundle.RESOLVED;
		assertTrue(OsgiBundleUtils.isBundleResolved(bundle));
		
		OsgiBundleUtilsTest.state = Bundle.STOPPING;
		assertTrue(OsgiBundleUtils.isBundleResolved(bundle));

		OsgiBundleUtilsTest.state = Bundle.STARTING;
		assertTrue(OsgiBundleUtils.isBundleResolved(bundle));

	}

}
