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

package org.eclipse.gemini.blueprint.internal.service.collection;

import java.util.Date;
import java.util.Iterator;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceCollection;

/**
 * Mock test for OsgiServiceCollection.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceCollectionTest extends AbstractOsgiCollectionTest {

	private Iterator iter;

	protected void setUp() throws Exception {
		super.setUp();
		iter = col.iterator();
	}

	OsgiServiceCollection createCollection() {
		return new OsgiServiceCollection(null, context, getClass().getClassLoader(), createProxyCreator(new Class<?>[] {
				Wrapper.class, Comparable.class }), false);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		col = null;
		iter = null;
	}

	public void testAddServiceBySize() throws Exception {
		assertEquals(0, col.size());

		Date service1 = new Date();
		addService(service1);

		assertEquals(1, col.size());

		Date service2 = new Date();
		addService(service2);

		assertEquals(2, col.size());
	}

	public void testAddServiceByIterating() throws Exception {
		DateWrapper service = new DateWrapper(123);
		addService(service);

		assertTrue(iter.hasNext());
		assertEquals(service.execute(), ((Wrapper) iter.next()).execute());

		assertFalse(iter.hasNext());

		DateWrapper date2 = new DateWrapper(321);
		addService(date2);
		assertTrue(iter.hasNext());
		assertEquals(date2.execute(), ((Wrapper) iter.next()).execute());

		assertFalse(iter.hasNext());
	}

	public void testRemoveService() {
		assertEquals(0, col.size());

		Date service1 = new Date();
		addService(service1);

		assertEquals(1, col.size());

		removeService(service1);
		assertEquals(0, col.size());

	}

	public void testRemoveServiceWhileIterating() throws Exception {
		long time1 = 123;
		DateWrapper date1 = new DateWrapper(time1);

		long time2 = 321;
		DateWrapper date2 = new DateWrapper(time2);
		addService(date1);
		addService(date2);

		assertTrue(iter.hasNext());
		assertEquals(new Long(time1), ((Wrapper) iter.next()).execute());

		assertTrue(iter.hasNext());
		removeService(date2);
		assertFalse(iter.hasNext());
	}
}
