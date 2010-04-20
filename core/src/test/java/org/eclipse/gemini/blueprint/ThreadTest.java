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

package org.eclipse.gemini.blueprint;

import junit.framework.TestCase;

public class ThreadTest extends TestCase {

	public static class TestThread implements Runnable {
		private int counter;

		public transient static boolean alive = true;

		public void run() {
			while (true) {
				// do nothing
				counter++;
			}
		}
	}

	public void testThreadInterrupt() throws Exception {
		Thread th = new Thread(new TestThread());
		th.start();

		assertTrue(th.isAlive());
		th.interrupt();
		assertTrue(th.isAlive());
		Thread.sleep(1000);
		assertTrue(th.isAlive());
	}
}
