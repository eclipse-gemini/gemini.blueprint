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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.notification.RunNotifier;

/**
 * Start executing the {@link RunBundleCreationTest} (which is an integration
 * test) and tests failures and errors.
 * 
 * @author Hal Hildebrand
 * @author Costin Leau
 * @author Michelle Cross
 */
public class RunBundleCreationTest {

	@Test
	public void test() {
		BundleCreationTstListener listener = new BundleCreationTstListener();
		BundleCreationTstRunner runner = new BundleCreationTstRunner(listener);
		RunNotifier notifier = new RunNotifier();
		runner.run(notifier);

		assertEquals(6, listener.getStarted());
		assertEquals(0, listener.getFailed());
		assertEquals(1, listener.getFinished());
	}
}