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

package org.eclipse.gemini.blueprint.test.internal.holder;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom class used for storing JUnit test results. To work, this class should
 * always be loaded through the same class loader, to <em>transport</em>
 * information from OSGi to the outside world.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiTestInfoHolder {

	/** JUnit test problems */
	private List testFailures = new ArrayList(4);
	private List testErrors = new ArrayList(4);

	/** test bundle id */
	private Long testBundleId;
	/** test class name */
	private String testClassName;
	/** test method name */
	private String testMethodName;

	/** static instance */
	public static final OsgiTestInfoHolder INSTANCE = new OsgiTestInfoHolder();


	/**
	 * 
	 * Constructs a new <code>OsgiTestInfoHolder</code> instance.
	 */
	public OsgiTestInfoHolder() {
	}

	/**
	 * Returns the testBundleId.
	 * 
	 * @return Returns the testBundleId
	 */
	public Long getTestBundleId() {
		return testBundleId;
	}

	/**
	 * @param testBundleId The testBundleId to set.
	 */
	public void setTestBundleId(Long testBundleId) {
		this.testBundleId = testBundleId;
	}

	/**
	 * Returns the testClassName.
	 * 
	 * @return Returns the testClassName
	 */
	public String getTestClassName() {
		return testClassName;
	}

	/**
	 * @param testClassName The testClassName to set.
	 */
	public void setTestClassName(String testClassName) {
		this.testClassName = testClassName;
	}

	/**
	 * @param testProblem The testResult to set.
	 */
	public void addTestFailure(Throwable testProblem) {
		testFailures.add(testProblem);
	}

	public void addTestError(Throwable testProblem) {
		testErrors.add(testProblem);
	}

	/**
	 * Returns the testMethodName.
	 * 
	 * @return Returns the testMethodName
	 */
	public String getTestMethodName() {
		return testMethodName;
	}

	/**
	 * @param testMethodName The testMethodName to set.
	 */
	public void setTestMethodName(String testMethodName) {
		this.testMethodName = testMethodName;
	}

	public List getTestFailures() {
		return testFailures;
	}

	public List getTestErrors() {
		return testErrors;
	}

	/**
	 * Clear all information. Used between test runs to clear results.
	 */
	public void clearResults() {
		testFailures.clear();
		testErrors.clear();
	}
}
