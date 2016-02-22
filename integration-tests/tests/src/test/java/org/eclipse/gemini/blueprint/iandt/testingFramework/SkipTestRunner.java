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

import junit.framework.TestCase;
import junit.framework.TestResult;
import org.eclipse.gemini.blueprint.test.legacyspringsupport.ConditionalTestCase;

/**
 * @author Costin Leau
 * 
 */
public class SkipTestRunner extends TestCase {

	private TestCase test;

	private TestResult result;


	protected void setUp() throws Exception {
		test = new SkipTestsTst();
		result = new TestResult();
	}

	public void testSkippedTestProperlyRecorded() throws Exception {
		executeTest(SkipTestsTst.TEST_SKIPPED_1);
		executeTest(SkipTestsTst.TEST_RAN);
		executeTest(SkipTestsTst.TEST_SKIPPED_2);

		// tests are being ran as far as JUnit is concerned
		assertEquals("tests are being not being ran by JUnit", 3, result.runCount());
		assertEquals("skipped tests not properly recorded", 2, ConditionalTestCase.getDisabledTestCount());
	}

	private void executeTest(String testMethod) {
		test.setName(testMethod);
		test.run(result);
	}
}
