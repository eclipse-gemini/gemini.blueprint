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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.osgi.framework.ServiceReference;

public class ServiceReferenceBasedMapTest extends TestCase {

	private ServiceReference reference;
	private Map map;


	protected void setUp() throws Exception {
		reference = new MockServiceReference();
		createMap();
	}

	protected void tearDown() throws Exception {
		reference = null;
		map = null;
	}

	private void createMap() {
		map = new ServiceReferenceBasedMap(reference);
	}

	public void testClear() {
		try {
			map.clear();
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}

	public void testContainsKeyObject() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop);
		createMap();
		assertTrue(map.containsKey("joe"));
	}

	public void testContainsValueObject() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop);
		createMap();
		assertTrue(map.containsValue("satriani"));
	}

	public void testEntrySet() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop);
		createMap();
		Set entries = map.entrySet();
		assertNotNull(entries);

		for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			assertTrue(map.containsKey(entry.getKey()));
			assertEquals(entry.getValue(), map.get(entry.getKey()));
		}
	}

	public void testGetObject() {
		Properties prop = new Properties();
		prop.setProperty("joe", "satriani");
		reference = new MockServiceReference(new MockBundle(), prop);
		createMap();
		assertEquals("satriani", map.get("joe"));
	}

	public void testPutObjectObject() {
		try {
			map.put(new Object(), new Object());
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}

	public void testPutAllMap() {
		try {
			map.putAll(new HashMap());
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}

	public void testRemoveObject() {
		try {
			map.remove(new Object());
			fail("map is read-only; expected exception");
		}
		catch (Exception ex) {
		}
	}
}
