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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.ListIterator;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceCollection;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OsgiServiceListTest extends AbstractOsgiCollectionTest {

	private OsgiServiceList col;

	private ListIterator iter;

	@Before
	public void setup() throws Exception {
		super.setup();

		col = (OsgiServiceList) super.col;
		iter = col.listIterator();
	}

	OsgiServiceCollection createCollection() {
		return new OsgiServiceList(null, context, getClass().getClassLoader(), createProxyCreator(new Class<?>[] {
				Wrapper.class, Comparable.class }), false);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		col = null;
		iter = null;
	}

	@Test
	public void testAddDuplicates() {
		long time1 = 123;
		Date date1 = new Date(time1);

		addService(date1);
		assertEquals(1, col.size());

		addService(date1);
		assertEquals("duplicate not added", 2, col.size());

		addService(date1);
		assertEquals("duplicate not added", 3, col.size());
	}

	@Test
	public void testRemoveDuplicate() {
		long time1 = 123;
		Date date1 = new Date(time1);

		addService(date1);
		addService(date1);
		addService(date1);

		assertEquals(3, col.size());

		removeService(date1);
		assertEquals(2, col.size());

		removeService(date1);
		assertEquals(1, col.size());
	}

	@Test
	public void testListIteratorWhileAdding() {
		long time1 = 123;
		Wrapper date = new DateWrapper(time1);

		addService(date);

		assertEquals(0, iter.nextIndex());
		assertEquals(new Long(time1), ((Wrapper) iter.next()).execute());
		addService(date);
		assertEquals(1, iter.nextIndex());
		assertEquals(new Long(time1), ((Wrapper) iter.next()).execute());
	}

	@Test
	public void testListIteratorWhileRemoving() {

		long time1 = 123;
		Wrapper date = new DateWrapper(time1);

		addService(date);
		addService(date);

		assertEquals(0, iter.nextIndex());
		Wrapper proxy1 = (Wrapper) iter.next();

		assertEquals(new Long(time1), proxy1.execute());
		removeService(date);

		assertEquals(1, iter.nextIndex());
		assertFalse(iter.hasNext());
		assertTrue(iter.hasPrevious());
		Wrapper proxy2 = ((Wrapper) iter.previous());
		assertEquals(new Long(time1), proxy2.execute());

		assertSame(proxy1, proxy2);

	}
}
