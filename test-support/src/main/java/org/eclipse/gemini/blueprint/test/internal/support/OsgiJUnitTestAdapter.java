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

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.test.AbstractOsgiTests;
import org.eclipse.gemini.blueprint.test.internal.OsgiJUnitTest;
import org.osgi.framework.BundleContext;
import org.springframework.util.Assert;

/**
 * Reflection-based adapter for {@link OsgiJUnitTest} used for wrapping
 * {@link AbstractOsgiTests} & co. with {@link OsgiJUnitTest} interface without
 * exposing the latter interface (which is internal and might be modified in the
 * future).
 * 
 * @author Costin Leau
 */
public class OsgiJUnitTestAdapter implements OsgiJUnitTest {

	private final TestCase target;

	private final Method injectBundleContext, runTest, setUp, tearDown;

	public OsgiJUnitTestAdapter(TestCase target) {
		Assert.notNull(target, "the adapter can be used only with a non-null test");

		this.target = target;

		try {

			// determine methods
			injectBundleContext = org.springframework.util.ReflectionUtils.findMethod(target.getClass(),
				"injectBundleContext", new Class<?>[] { BundleContext.class });
			org.springframework.util.ReflectionUtils.makeAccessible(injectBundleContext);

			runTest = org.springframework.util.ReflectionUtils.findMethod(target.getClass(), "osgiRunTest");
			org.springframework.util.ReflectionUtils.makeAccessible(runTest);

			setUp = org.springframework.util.ReflectionUtils.findMethod(target.getClass(), "osgiSetUp");
			org.springframework.util.ReflectionUtils.makeAccessible(setUp);

			tearDown = org.springframework.util.ReflectionUtils.findMethod(target.getClass(), "osgiTearDown");
			org.springframework.util.ReflectionUtils.makeAccessible(tearDown);

		}
		catch (Exception ex) {
			throw new RuntimeException(
					"cannot determine JUnit hooks; is this test extending Spring-DM test framework?", ex);
		}

	}

	public void injectBundleContext(BundleContext bundleContext) {
		org.springframework.util.ReflectionUtils.invokeMethod(injectBundleContext, target,
			new Object[] { bundleContext });
	}

	public void osgiRunTest() throws Throwable {
		org.springframework.util.ReflectionUtils.invokeMethod(runTest, target);
	}

	public void osgiSetUp() throws Exception {
		org.springframework.util.ReflectionUtils.invokeMethod(setUp, target);
	}

	public void osgiTearDown() throws Exception {
		org.springframework.util.ReflectionUtils.invokeMethod(tearDown, target);
	}

	public TestCase getTestCase() {
		return target;
	}

}
