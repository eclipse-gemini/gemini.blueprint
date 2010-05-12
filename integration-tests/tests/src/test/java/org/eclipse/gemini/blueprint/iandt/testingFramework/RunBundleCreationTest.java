/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.iandt.testingFramework;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/**
 * Start executing the {@link RunBundleCreationTest} (which is an integration
 * test) and tests failures and errors.
 * 
 * @author Hal Hildebrand
 * @author Costin Leau
 */
public class RunBundleCreationTest extends TestCase {

	private TestCase test;

	private TestResult result;


	protected void setUp() throws Exception {
		test = new BundleCreationTst();
		result = new TestResult();
	}

	public void testAssertionPass() {
		executeTest("testAssertionPass");
		assertEquals(0, result.errorCount());
		assertEquals(0, result.failureCount());
	}

	public void testAssertionFailure() {
		executeTest("testAssertionFailure");
		assertEquals("failure counted as error", 0, result.errorCount());
		assertEquals("failure ignored", 1, result.failureCount());

	}

	public void testFailure() {
		executeTest("testFailure");
		assertEquals("failure counted as error", 0, result.errorCount());
		assertEquals("failure ignored", 1, result.failureCount());
	}

	public void testException() {
		executeTest("testException");
		assertEquals("error not considered", 1, result.errorCount());
		assertEquals("error considered failure", 0, result.failureCount());
	}

	public void testExceptionClass() throws Exception {
		executeTest("testException");
		TestFailure failure = (TestFailure) result.errors().nextElement();
		assertTrue(failure.thrownException() instanceof RuntimeException);
	}

	public void testError() {
		executeTest("testError");
		assertEquals("error not considered", 1, result.errorCount());
		assertEquals("error considered failure", 0, result.failureCount());
	}

	public void testErrorClass() throws Exception {
		executeTest("testError");
		TestFailure failure = (TestFailure) result.errors().nextElement();
		assertTrue(failure.thrownException() instanceof Error);
	}

	private void executeTest(String testMethod) {
		test.setName(testMethod);
		test.run(result);
		assertEquals(1, result.runCount());
	}
}