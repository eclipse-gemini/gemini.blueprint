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

package org.eclipse.gemini.blueprint.util.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.osgi.framework.ServiceReference;

public class ServiceReferenceBasedMapTest {

	private ServiceReference reference;
	private Map map;

	@Before
	public void setup() throws Exception {
		reference = new MockServiceReference();
		createMap();
	}

	@After
	public void tearDown() throws Exception {
		reference = null;
		map = null;
	}

	private void createMap() {
		map = new ServiceReferenceBasedMap(reference);
	}

	@Test
	public void testClear() {
		try {
			map.clear();
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}

	@Test
	public void testContainsKeyObject() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop, null);
		createMap();
		assertTrue(map.containsKey("joe"));
	}

	@Test
	public void testContainsValueObject() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop, null);
		createMap();
		assertTrue(map.containsValue("satriani"));
	}

	@Test
	public void testEntrySet() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop, null);
		createMap();
		Set entries = map.entrySet();
		assertNotNull(entries);

		for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			assertTrue(map.containsKey(entry.getKey()));
			assertEquals(entry.getValue(), map.get(entry.getKey()));
		}
	}

	@Test
	public void testGetObject() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop, null);
		createMap();
		assertEquals("satriani", map.get("joe"));
	}

	@Test
	public void testPutObjectObject() {
		try {
			map.put(new Object(), new Object());
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}

	@Test
	public void testPutAllMap() {
		try {
			map.putAll(new HashMap());
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}

	@Test
	public void testRemoveObject() {
		try {
			map.remove(new Object());
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}
}
