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
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.ListIterator;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceCollection;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceSortedList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Costin Leau
 * 
 */
public class OsgiServiceSortedListTest extends AbstractOsgiCollectionTest {

	private OsgiServiceSortedList col;

	private Iterator iter;

	@Before
	public void setup() throws Exception {
		super.setup();
		col = (OsgiServiceSortedList) super.col;
		iter = col.iterator();
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		col = null;
		iter = null;
	}

	OsgiServiceCollection createCollection() {
		return new OsgiServiceSortedList(null, context, getClass().getClassLoader(), createProxyCreator(new Class<?>[] {
				Wrapper.class, Comparable.class }), false);
	}

	@Test
	public void testOrderingWhileAdding() {
		Wrapper date1 = new DateWrapper(1);
		Wrapper date2 = new DateWrapper(2);
		Wrapper date3 = new DateWrapper(3);

		addService(date2);
		assertEquals(1, col.size());

		addService(date2);
		// duplicates accepted
		assertEquals("collection should accept duplicates", 2, col.size());
		assertEquals(date2.toString(), col.get(0).toString());

		addService(date1);
		assertEquals("collection should accept duplicates", 3, col.size());
		assertEquals(date1.toString(), col.get(0).toString());

		addService(date3);

		assertEquals("collection should accept duplicates", 4, col.size());
		assertEquals(date1.toString(), col.get(0).toString());
		assertEquals(date3.toString(), col.get(col.size() - 1).toString());
	}

	@Test
	public void testOrderingWhileRemoving() {
		Wrapper date1 = new DateWrapper(1);
		Wrapper date2 = new DateWrapper(2);
		Wrapper date3 = new DateWrapper(3);

		addService(date1);
		addService(date2);
		addService(date3);

		removeService(date2);
		assertEquals(2, col.size());

		assertEquals(date1.toString(), col.get(0).toString());
		assertEquals(date3.toString(), col.get(col.size() - 1).toString());

		removeService(date1);

		assertEquals(1, col.size());
		assertEquals(date3.toString(), col.get(0).toString());
		assertEquals(date3.toString(), col.get(col.size() - 1).toString());

	}

	@Test
	public void testOrderingWhileIterating() {
		Wrapper date1 = new DateWrapper(1);
		Wrapper date2 = new DateWrapper(2);
		Wrapper date3 = new DateWrapper(3);

		addService(date2);

		assertTrue(iter.hasNext());
		assertEquals(date2.toString(), iter.next().toString());

		addService(date1);
		assertFalse(iter.hasNext());

		addService(date3);
		assertTrue(iter.hasNext());
		assertEquals(date3.toString(), iter.next().toString());
	}

	@Test
	public void testOrderingAndDuplicatesWhileIterating() {
		Wrapper date1 = new DateWrapper(1);
		Wrapper date2 = new DateWrapper(2);
		Wrapper date3 = new DateWrapper(3);

		addService(date2);

		assertTrue(iter.hasNext());
		assertEquals(date2.toString(), iter.next().toString());

		assertEquals(1, col.size());

		// check next duplicate
		addService(date2);
		assertEquals(2, col.size());

		assertEquals(date2.toString(), iter.next().toString());

		addService(date1);
		assertFalse(iter.hasNext());
		assertEquals("collection should accept duplicates", 3, col.size());

		addService(date1);
		assertFalse(iter.hasNext());
		assertEquals("collection should accept duplicates", 4, col.size());

		addService(date3);
		assertTrue(iter.hasNext());
		assertEquals(date3.toString(), iter.next().toString());
	}

	@Test
	public void testRemovalWhileIterating() {
		Wrapper date1 = new DateWrapper(1);
		Wrapper date2 = new DateWrapper(2);
		Wrapper date3 = new DateWrapper(3);

		addService(date2);
		addService(date3);
		addService(date1);
		addService(date2);
		addService(date1);

		assertEquals(5, col.size());

		// date1
		assertEquals(date1.toString(), iter.next().toString());

		removeService(date1);

		// date2
		assertEquals(date2.toString(), iter.next().toString());

		removeService(date2);
		// date 3
		assertEquals(date3.toString(), iter.next().toString());
	}

	@Test
	public void testHeadDeadProxy() {
		ListIterator iterator = col.listIterator();

		Wrapper date1 = new DateWrapper(1);
		Wrapper date2 = new DateWrapper(2);
		Wrapper date3 = new DateWrapper(3);

		addService(date2);
		addService(date1);
		addService(date3);

		// date1
		assertEquals(date1.toString(), iterator.next().toString());

		assertTrue(iterator.hasPrevious());
		removeService(date1);
	}

}
