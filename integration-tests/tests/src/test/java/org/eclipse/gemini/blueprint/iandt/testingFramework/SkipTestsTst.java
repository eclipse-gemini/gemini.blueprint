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

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;

/**
 * 
 * @author Costin Leau
 * 
 */
public class SkipTestsTst extends BaseIntegrationTest {

	static final String TEST_SKIPPED_1 = "testFirstSkipped";
	static final String TEST_SKIPPED_2 = "testSecondSkipped";
	static final String TEST_RAN = "testActuallyRan";


	public void testFirstSkipped() throws Exception {
		fail("test should be skipped");
	}

	public void testActuallyRan() throws Exception {
	}

	public void testSecondSkipped() throws Exception {
		fail("test should be skipped");
	}

	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return testMethodName.endsWith("Skipped");
	}

}
