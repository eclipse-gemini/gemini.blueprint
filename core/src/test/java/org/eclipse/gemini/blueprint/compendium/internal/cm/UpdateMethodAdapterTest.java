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

package org.eclipse.gemini.blueprint.compendium.internal.cm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.eclipse.gemini.blueprint.util.internal.MapBasedDictionary;

/**
 * @author Costin Leau
 * 
 */
public class UpdateMethodAdapterTest {

	public class NoMethod {

	}

	public static class OneMapMethod {

		public static int INVOCATIONS = 0;


		public void update(Map properties) {
			INVOCATIONS++;
		}
	}

	public static class OneDictionaryMethod {

		public static int INVOCATIONS = 0;


		public void dictMethod(Dictionary properties) {
			INVOCATIONS++;
		}
	}

	public static class BothMethods {

		public static int INVOCATIONS = 0;


		public void update(Dictionary prop) {
			INVOCATIONS++;
		}

		public void update(Map properties) {
			INVOCATIONS++;
		}
	}

	public class NonPublicMethod {

		void update(Map props) {
		}
	}

	@Test
	public void testDetermineUpdateMethodWNoMethod() {
		assertTrue(UpdateMethodAdapter.determineUpdateMethod(NoMethod.class, "update").isEmpty());
	}

	@Test
	public void testDetermineUpdateMethodWMapMethod() {
		assertEquals(1, UpdateMethodAdapter.determineUpdateMethod(OneMapMethod.class, "update").size());
	}

	@Test
	public void testDetermineUpdateMethodWDictMethod() {
		assertEquals(1, UpdateMethodAdapter.determineUpdateMethod(OneDictionaryMethod.class, "dictMethod").size());
	}

	@Test
	public void testDetermineUpdateMethodWBothMethod() {
		assertEquals(2, UpdateMethodAdapter.determineUpdateMethod(BothMethods.class, "update").size());
	}

	@Test
	public void testDetermineUpdateMethodWNonPublicMethod() {
		assertTrue(UpdateMethodAdapter.determineUpdateMethod(NonPublicMethod.class, "update").isEmpty());
	}

	@Test
	public void testInvokeCustomMethodsOnMapMethod() {
		OneMapMethod.INVOCATIONS = 0;
		Map methods = UpdateMethodAdapter.determineUpdateMethod(OneMapMethod.class, "update");
		UpdateMethodAdapter.invokeCustomMethods(new OneMapMethod(), methods, new HashMap());
		assertEquals(1, OneMapMethod.INVOCATIONS);
	}

	@Test
	public void testInvokeCustomMethodsOnDictMethod() {
		OneDictionaryMethod.INVOCATIONS = 0;
		Map methods = UpdateMethodAdapter.determineUpdateMethod(OneDictionaryMethod.class, "dictMethod");
		UpdateMethodAdapter.invokeCustomMethods(new OneDictionaryMethod(), methods, new MapBasedDictionary());
		assertEquals(1, OneDictionaryMethod.INVOCATIONS);
	}

	@Test
	public void testInvokeCustomMethodsOnBothMethod() {
		BothMethods.INVOCATIONS = 0;
		Map methods = UpdateMethodAdapter.determineUpdateMethod(BothMethods.class, "update");
		UpdateMethodAdapter.invokeCustomMethods(new BothMethods(), methods, new MapBasedDictionary());
		assertEquals(2, BothMethods.INVOCATIONS);
	}
}
