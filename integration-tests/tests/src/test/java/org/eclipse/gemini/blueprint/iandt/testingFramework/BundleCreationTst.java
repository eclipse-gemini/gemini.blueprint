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

package org.eclipse.gemini.blueprint.iandt.testingFramework;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;

/**
 * Test to check if the testcase is properly packaged in a bundle jar and deploy
 * on the OSGi platform.
 * 
 * Note: this test case is not intended to be run (hence the Tst name).
 * 
 * @author Costin Leau
 * 
 */
public class BundleCreationTst extends BaseIntegrationTest {
	
	public BundleCreationTst() {

	}

	protected String[] getBundleLocations() {
		// no test bundle is included
		return new String[] {};
	}

	public void testAssertionPass() {
		assertTrue(true);
	}

	public void testAssertionFailure() {
		assertTrue(false);
	}

	public void testFailure() {
		fail("this is a failure");
	}

	public void testException() {
		throw new RuntimeException("this is an exception");
	}

	public void testError() {
		throw new Error("this is an error");
	}

}