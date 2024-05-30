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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * @author Costin Leau
 * 
 */
public class OsgiBundleUtilsTest {

	private Bundle bundle;

	private static int state;

	@Before
	public void setup() throws Exception {
		OsgiBundleUtilsTest.state = Bundle.UNINSTALLED;
		bundle = new MockBundle() {
			public int getState() {
				return state;
			}
		};
	}

	@After
	public void tearDown() throws Exception {
		bundle = null;
	}

	@Test
	public void testIsInActiveBundleState() throws Exception {
		OsgiBundleUtilsTest.state = Bundle.ACTIVE;
		assertTrue(OsgiBundleUtils.isBundleActive(bundle));

		OsgiBundleUtilsTest.state = Bundle.STARTING;
		assertFalse(OsgiBundleUtils.isBundleActive(bundle));

		OsgiBundleUtilsTest.state = Bundle.INSTALLED;
		assertFalse(OsgiBundleUtils.isBundleActive(bundle));
	}

	@Test
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
