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

package org.eclipse.gemini.blueprint.test.internal.support;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.test.internal.OsgiJUnitTest;
import org.eclipse.gemini.blueprint.test.internal.TestRunnerService;
import org.eclipse.gemini.blueprint.test.internal.holder.HolderLoader;
import org.eclipse.gemini.blueprint.test.internal.holder.OsgiTestInfoHolder;
import org.eclipse.gemini.blueprint.test.internal.util.TestUtils;

/**
 * OSGi service for executing JUnit tests.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiJUnitService implements TestRunnerService {

	private static final Log log = LogFactory.getLog(OsgiJUnitService.class);


	public void runTest(OsgiJUnitTest test) {
		try {
			executeTest(test);
		}
		catch (Exception ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			throw new RuntimeException("cannot execute test:" + ex, ex);
		}
	}

	/**
	 * Execute the JUnit test and publish results to the outside-OSGi world.
	 * 
	 * @param test
	 * @throws Exception
	 */
	protected void executeTest(OsgiJUnitTest test) throws Exception {
		// create holder
		// since we're inside OSGi, we have to use the special loading procedure
		OsgiTestInfoHolder holder = HolderLoader.INSTANCE.getHolder();

		// read the test to be executed
		String testName = holder.getTestMethodName();
		if (log.isDebugEnabled())
			log.debug("Reading test [" + testName + "] for execution inside OSGi");
		// execute the test
		TestResult result = runTest(test, testName);

		if (log.isDebugEnabled())
			log.debug("Sending test results from OSGi");
		// write result back to the outside world
		TestUtils.unpackProblems(result, holder);
	}

	/**
	 * Run fixture setup, test from the given test case and fixture teardown.
	 * 
	 * @param osgiTestExtensions
	 * @param testName
	 */
	protected TestResult runTest(final OsgiJUnitTest osgiTestExtensions, String testName) {
		if (log.isDebugEnabled())
			log.debug("Running test [" + testName + "] on testCase " + osgiTestExtensions);
		final TestResult result = new TestResult();
		TestCase rawTest = osgiTestExtensions.getTestCase();

		rawTest.setName(testName);

		try {
			osgiTestExtensions.osgiSetUp();

			try {
				// use TestResult method to bypass the setUp/tearDown methods
				result.runProtected(rawTest, new Protectable() {

					public void protect() throws Throwable {
						osgiTestExtensions.osgiRunTest();
					}

				});
			}
			finally {
				osgiTestExtensions.osgiTearDown();
			}

		}
		// exceptions thrown by osgiSetUp/osgiTearDown
		catch (Exception ex) {
			log.error("test exception threw exception ", ex);
			result.addError((Test) rawTest, ex);
		}
		return result;
	}
}