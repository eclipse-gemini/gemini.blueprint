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

package org.eclipse.gemini.blueprint.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.util.internal.MapBasedDictionary;

/**
 * Test for the Dictionary contract of {@link MapBasedDictionary}.
 * 
 * @author Costin Leau
 * 
 */
public class MapBasedDictionaryTest {

	private Dictionary dict;

	@Before
	public void setup() throws Exception {
		dict = new MapBasedDictionary();
	}

	@After
	public void tearDown() throws Exception {
		dict = null;
	}

	@Test
	public void testDictionaryWithNullMap() {
		dict = new MapBasedDictionary((Map) null);
		assertTrue(dict.isEmpty());
	}

	@Test
	public void testElements() {
		dict.put(new Object(), new Object());
		Object value = new Object();

		dict.put(new Object(), value);
		dict.put(new Object(), value);

		Enumeration enm = dict.elements();
		assertNotNull(enm);

		enm.nextElement();
		assertSame(value, enm.nextElement());
		assertSame(value, enm.nextElement());
		// no more elements
		assertFalse(enm.hasMoreElements());
	}

	@Test
	public void testKeys() {
		Object key = new Object();
		Object value = new Object();

		dict.put(key, new Object());
		dict.put(key, value);

		Enumeration enm = dict.keys();
		assertNotNull(enm);

		assertSame(key, enm.nextElement());
		assertFalse(enm.hasMoreElements());
	}

	@Test
	public void testGetObject() {
		Object key = new Object();
		Object value = new Object();

		dict.put(key, new Object());
		dict.put(key, value);

		assertNull(dict.get(new Object()));
		assertSame(value, dict.get(key));
		assertSame(value, dict.get(key));

	}

	@Test
	public void testGetNullObject() {
		try {
			dict.get(null);
			fail("should have thrown NPE");
		}
		catch (NullPointerException e) {
			// expected
		}
	}

	@Test
	public void testRemoveObject() {
		assertNull(dict.remove(new Object()));

		Object key = new Object();

		dict.put(key, key);

		assertSame(key, dict.remove(key));
		assertNull(dict.remove(key));
	}

	@Test
	public void testRemoveNullObject() {
		try {
			dict.remove(null);
			fail("should have thrown NPE");
		}
		catch (NullPointerException e) {
			// expected
		}

	}

	@Test
	public void testPutObjectObject() {
		assertNull(dict.put(new Object(), new Object()));

		Object key = new Object();
		Object value = new Object();

		assertNull(dict.put(key, value));
		assertSame(value, dict.put(key, value));
	}

	@Test
	public void testPutNullValue() {
		try {
			dict.put(new Object(), null);
			fail("should have thrown NPE");
		}
		catch (NullPointerException e) {
			// expected
		}
	}

	@Test
	public void testPutNullKey() {
		try {
			dict.put(null, new Object());
			fail("should have thrown NPE");
		}
		catch (NullPointerException e) {
			// expected
		}
	}

	@Test
	public void testSize() {
		assertEquals(0, dict.size());
		dict.put(new Object(), new Object());

		Object key = new Object();
		dict.put(key, key);
		assertEquals(2, dict.size());
		dict.put(key, key);
		assertEquals(2, dict.size());
	}

	@Test
	public void testDictionaryWithDictionary() {
		Dictionary dict = new Properties();
		dict.put("joe", "satriani");

		Dictionary wrapper = new MapBasedDictionary(dict);
		assertEquals(wrapper, dict);
	}

	@Test
	public void testPutAllDictionary() {
		Dictionary dict = new Properties();
		dict.put("joe", "satriani");

		MapBasedDictionary wrapper = new MapBasedDictionary();
		wrapper.putAll(dict);
		assertEquals(wrapper, dict);
	}

	@Test
	public void testValues() throws Exception {
		Dictionary dict = new Properties();
		dict.put("joe", "satriani");

		MapBasedDictionary wrapper = new MapBasedDictionary();
		wrapper.putAll(dict);

		Enumeration enm1 = dict.elements();
		Enumeration enm2 = wrapper.elements();

		while (enm1.hasMoreElements()) {
			assertTrue(enm2.hasMoreElements());
			assertEquals(enm1.nextElement(), enm2.nextElement());
		}
	}

	@Test
	public void testClear() throws Exception {
		Dictionary dict = new Properties();
		dict.put("joe", "satriani");

		MapBasedDictionary wrapper = new MapBasedDictionary();
		wrapper.putAll(dict);
		assertEquals(1, wrapper.size());
		wrapper.clear();
		assertEquals(0, wrapper.size());
	}

	@Test
	public void testContainsKey() throws Exception {
		Dictionary dict = new Properties();
		dict.put("joe", "satriani");

		MapBasedDictionary wrapper = new MapBasedDictionary();
		wrapper.putAll(dict);
		assertTrue(wrapper.containsKey("joe"));
	}

	@Test
	public void testValue() throws Exception {
		Dictionary dict = new Properties();
		dict.put("joe", "satriani");

		MapBasedDictionary wrapper = new MapBasedDictionary();
		wrapper.putAll(dict);
		assertTrue(wrapper.containsValue("satriani"));
	}

	@Test
	public void testHashCode() throws Exception {
		MapBasedDictionary wrapper1 = new MapBasedDictionary();
		MapBasedDictionary wrapper2 = new MapBasedDictionary();
		assertEquals(wrapper1.hashCode(), wrapper2.hashCode());
		wrapper1.put(new Object(), new Object());
		assertFalse(wrapper1.hashCode() == wrapper2.hashCode());
	}

}
