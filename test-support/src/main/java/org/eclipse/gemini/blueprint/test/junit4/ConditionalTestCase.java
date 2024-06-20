/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.gemini.blueprint.test.junit4;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Superclass for JUnit 4.5+ based tests that allows conditional test execution
 * at the individual test method level. The
 * {@link #isDisabledInThisEnvironment(String) isDisabledInThisEnvironment()}
 * method is invoked before the execution of each test method. Subclasses can
 * override that method to return whether or not the given test should be
 * executed. Note that the tests will still appear to have executed and passed;
 * however, log output will show that the test was not executed.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see #isDisabledInThisEnvironment
 */
public abstract class ConditionalTestCase extends AbstractJUnit4SpringContextTests {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	
	private final String name;

	private static int disabledTestCount;

	/**
	 * Return the number of tests disabled in this environment.
	 */
	public static int getDisabledTestCount() {
		return disabledTestCount;
	}

	/**
	 * Default constructor for ConditionalTestCase.
	 */
	public ConditionalTestCase() {
		this.name = getClass().getName();
	}

	/**
	 * Constructor for ConditionalTestCase with a JUnit name.
	 */
	public ConditionalTestCase(String name) {
		this.name = name;
	}

	/**
	 * Should this test run?
	 * 
	 * @param testMethodName name of the test method
	 * @return whether the test should execute in the current environment
	 */
	public boolean isDisabledInThisEnvironment(String testMethodName) {
		return false;
	}

	/**
	 * Record a disabled test.
	 * 
	 * @return the current disabled test count
	 */
	protected int recordDisabled() {
		return ++disabledTestCount;
	}

	public String getName() {
		return this.name;
	}
}
