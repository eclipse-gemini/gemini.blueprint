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

import java.lang.reflect.Method;

import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * OSGi adapter for the information holder. Overrides the methods used inside
 * OSGi to use reflection and avoid CCE.
 * 
 * @author Costin Leau
 * 
 */
class ReflectionOsgiHolder extends OsgiTestInfoHolder {

	private final Object instance;

	private final Method GET_TEST_BUNDLE_ID, GET_TEST_CLASS_NAME, GET_TEST_METHOD_NAME, ADD_TEST_ERROR,
			ADD_TEST_FAILURE;


	/**
	 * Constructs a new <code>OsgiTestInfoHolder</code> instance wrapping the
	 * given object and accessing it through reflection. This constructor is
	 * used for accessing the instance loaded outside OSGi, from within OSGi.
	 * 
	 * @param twinInstance instance to wrap
	 */
	ReflectionOsgiHolder(Object twinInstance) {
		Assert.notNull(twinInstance, "twinInstance is required");
		this.instance = twinInstance;
		Class<?> clazz = instance.getClass();
		GET_TEST_BUNDLE_ID = ReflectionUtils.findMethod(clazz, "getTestBundleId");
		GET_TEST_CLASS_NAME = ReflectionUtils.findMethod(clazz, "getTestClassName");
		GET_TEST_METHOD_NAME = ReflectionUtils.findMethod(clazz, "getTestMethodName");

		ADD_TEST_ERROR = ReflectionUtils.findMethod(clazz, "addTestError", Throwable.class);
		ADD_TEST_FAILURE = ReflectionUtils.findMethod(clazz, "addTestFailure", Throwable.class);

	}

	public Long getTestBundleId() {
		return (Long) ReflectionUtils.invokeMethod(GET_TEST_BUNDLE_ID, instance);
	}

	public String getTestClassName() {
		return (String) ReflectionUtils.invokeMethod(GET_TEST_CLASS_NAME, instance);
	}

	public String getTestMethodName() {
		return (String) ReflectionUtils.invokeMethod(GET_TEST_METHOD_NAME, instance);
	}

	public void addTestError(Throwable testProblem) {
		ReflectionUtils.invokeMethod(ADD_TEST_ERROR, instance, testProblem);
	}

	public void addTestFailure(Throwable testProblem) {
		ReflectionUtils.invokeMethod(ADD_TEST_FAILURE, instance, testProblem);
	}

}
