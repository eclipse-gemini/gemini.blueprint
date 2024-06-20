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

import org.eclipse.gemini.blueprint.test.internal.OsgiJUnitTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.osgi.framework.BundleContext;

/**
 * OSGi service for executing JUnit tests.
 *
 * @author Costin Leau
 * @author Michelle Cross
 */
public class OsgiJUnitService extends Runner {

	private Class<?> test;

	private BundleContext bc;

	public OsgiJUnitService(Class<?> test) {
		super();
		this.test = test;
	}

	@Override
	public Description getDescription() {
		return Description.createTestDescription(test, "OsgiJUnitService");
	}

	@Override
	public void run(RunNotifier notifier) {
		System.out.println("Running the tests from OsgiJUnitService: " + test);
		try {
			OsgiJUnitTest testObject = (OsgiJUnitTest) test.newInstance();
			for (Method method : test.getMethods()) {
				if (method.isAnnotationPresent(Test.class) && !method.isAnnotationPresent(Ignore.class)) {
					notifier.fireTestStarted(Description.createTestDescription(test, method.getName()));
					try {
						testObject.injectBundleContext(bc);
						testObject.osgiSetUp();
						method.invoke(testObject);
					} finally {
						testObject.osgiTearDown();
					}
					notifier.fireTestFinished(Description.createTestDescription(test, method.getName()));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setBundleContext(BundleContext bc) {
		this.bc = bc;
	}
}