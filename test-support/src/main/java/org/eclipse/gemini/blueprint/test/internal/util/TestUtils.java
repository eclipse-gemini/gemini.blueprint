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

package org.eclipse.gemini.blueprint.test.internal.util;

import java.util.Enumeration;
import java.util.Iterator;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;

import org.eclipse.gemini.blueprint.test.internal.holder.OsgiTestInfoHolder;

/**
 * Utility class for running OSGi-JUnit tests.
 * 
 * @author Costin Leau
 * 
 */
public abstract class TestUtils {

	/**
	 * Clones the test result from a TestResult loaded through a different
	 * classloader.
	 * 
	 * @param source test result loaded through a different classloader
	 * @param destination test result reported to the outside framework
	 * @param test initial test used for bootstrapping the integration framework
	 * @return cloned test result
	 */
	public static TestResult cloneTestResults(OsgiTestInfoHolder source, TestResult destination, Test test) {
		// get errors
        for (Throwable throwable : source.getTestErrors()) {
            destination.addError(test, throwable);
        }

		// get failures
		// since failures are a special JUnit error, we have to clone the stack
        for (Throwable originalFailure : source.getTestFailures()) {
            AssertionFailedError clonedFailure = new AssertionFailedError(originalFailure.getMessage());
            clonedFailure.setStackTrace(originalFailure.getStackTrace());
            destination.addFailure(test, clonedFailure);
        }

		return destination;
	}

	/**
	 * Utility method which extracts the information from a TestResult and
	 * stores it as primordial classes. This avoids the use of reflection when
	 * reading the results outside OSGi.
	 * 
	 * @param result
	 * @param holder
	 */
	public static void unpackProblems(TestResult result, OsgiTestInfoHolder holder) {
		Enumeration<TestFailure> errors = result.errors();
		while (errors.hasMoreElements()) {
			TestFailure failure = errors.nextElement();
			holder.addTestError(failure.thrownException());
		}
		Enumeration<TestFailure> failures = result.failures();
		while (failures.hasMoreElements()) {
			TestFailure failure = failures.nextElement();
			holder.addTestFailure(failure.thrownException());
		}
	}
}
