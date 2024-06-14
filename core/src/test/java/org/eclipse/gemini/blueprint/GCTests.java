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

import static org.junit.Assert.fail;

import java.lang.ref.Reference;

/**
 * Utility class providing methods related to 'Garbage Collector' and
 * WeakReferences.
 * Normally used inside JUnit test cases.
 * 
 * @author Costin Leau
 * 
 */
public abstract class GCTests {

	/**
	 * Number of iterators while calling the GC.
	 */
	public static int GC_ITERATIONS = 30;

	public static void assertGCed(Reference reference) {
		assertGCed("given object was not reclaimed", reference);
	}

	/**
	 * Assert that the given object reference has been reclaimed. This assertion
	 * is useful for determing if there are hard references to the given object.
	 * 
	 * @param message
	 * @param reference
	 */
	public static void assertGCed(String message, Reference reference) {
		int garbageSize = 300;

		for (int i = 0; i < GC_ITERATIONS; i++) {
			if (reference.get() == null) {
				return;
			}
			else {
				// add garbage
				byte[] garbage = new byte[garbageSize];
				garbageSize = garbageSize << 1;

				// trigger the GC
				System.gc();
				System.runFinalization();
			}
		}

		fail(message);
	}
}
