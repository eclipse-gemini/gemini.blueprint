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

import java.lang.reflect.Method;

import org.eclipse.gemini.blueprint.test.junit4.ConditionalTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

/**
 * @author Michelle Cross
 */
public class SkipTestRunner extends Runner {

	private Class<? extends ConditionalTestCase> test;

	public SkipTestRunner(Class<? extends ConditionalTestCase> test) {
		super();
		this.test = test;
	}

	@Override
	public Description getDescription() {
		return Description.createTestDescription(test, "SkipTestRunner");
	}

	@Override
	public void run(RunNotifier notifier) {
		System.out.println("Running the tests from SkipTestRunner: " + test);
		try {
			ConditionalTestCase testObject = test.newInstance();
			for (Method method : test.getMethods()) {
				if (method.isAnnotationPresent(Test.class) && !method.isAnnotationPresent(Ignore.class)
						&& !testObject.isDisabledInThisEnvironment(method.getName())) {
					notifier.fireTestStarted(Description.createTestDescription(test, method.getName()));
					method.invoke(testObject);
					notifier.fireTestFinished(Description.createTestDescription(test, method.getName()));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
