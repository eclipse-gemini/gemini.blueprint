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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.DynamicSortedSet;

/**
 * @author Costin Leau
 * 
 */
public class DynamicSortedSetTest {

	private SortedSet dynamicSortedSet;

	private List list;

	private Integer one;

	private Integer two;

	private Integer three;

	@Before
	public void setup() throws Exception {
		dynamicSortedSet = new DynamicSortedSet();

		one = Integer.valueOf(1);
		two = Integer.valueOf(2);
		three = Integer.valueOf(3);

		list = new ArrayList();
		list.add(one);
		list.add(two);
		list.add(three);
		list.add(two);

	}

	@After
	public void tearDown() throws Exception {
		dynamicSortedSet = null;
		list = null;
		one = null;
		two = null;
		three = null;
	}

	@Test
	public void testAdd() {
		assertTrue(dynamicSortedSet.add(one));
		assertFalse(dynamicSortedSet.add(one));
		assertTrue(dynamicSortedSet.add(three));
		assertFalse(dynamicSortedSet.add(three));
	}

	@Test
	public void testAddNullForbidden() {
		try {
			dynamicSortedSet.add(null);
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testAddAll() {
		assertTrue(dynamicSortedSet.isEmpty());
		assertTrue(dynamicSortedSet.addAll(list));
		assertEquals(3, dynamicSortedSet.size());
	}

	@Test
	public void testAddAllOnExistingSet() {
		dynamicSortedSet.add(two);
		assertTrue(dynamicSortedSet.addAll(list));
		assertEquals(3, dynamicSortedSet.size());
	}

	@Test
	public void testRemove() {
		dynamicSortedSet.add(one);
		dynamicSortedSet.add(two);
		dynamicSortedSet.add(three);

		assertEquals(3, dynamicSortedSet.size());

		assertTrue(dynamicSortedSet.remove(one));
		assertEquals(2, dynamicSortedSet.size());
		assertFalse(dynamicSortedSet.remove(one));

		assertTrue(dynamicSortedSet.remove(three));
		assertEquals(1, dynamicSortedSet.size());
	}

	@Test
	public void testRemoveNullForbidden() {
		try {
			dynamicSortedSet.remove(null);
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testRemoveAll() {
		dynamicSortedSet.add(two);
		dynamicSortedSet.add(Integer.valueOf(4));

		assertTrue(dynamicSortedSet.removeAll(list));
		assertEquals(1, dynamicSortedSet.size());
	}

	@Test
	public void testFirst() {
		dynamicSortedSet.add(three);
		assertSame(three, dynamicSortedSet.first());
		dynamicSortedSet.add(two);
		assertSame(two, dynamicSortedSet.first());
		dynamicSortedSet.add(one);
		assertSame(one, dynamicSortedSet.first());

		dynamicSortedSet.remove(two);
		dynamicSortedSet.add(two);
		assertSame(one, dynamicSortedSet.first());

	}

	@Test
	public void testFirstWithEmptySet() {
		assertTrue(dynamicSortedSet.isEmpty());
		try {
			dynamicSortedSet.first();
			fail("should have thrown NoSuchElementException");
		}
		catch (NoSuchElementException nsee) {
			// expected
		}
	}

	@Test
	public void testLast() {
		dynamicSortedSet.add(one);
		assertSame(one, dynamicSortedSet.last());
		dynamicSortedSet.add(two);
		assertSame(two, dynamicSortedSet.last());
		dynamicSortedSet.add(three);
		assertSame(three, dynamicSortedSet.last());

		dynamicSortedSet.remove(two);
		dynamicSortedSet.add(two);
		assertSame(three, dynamicSortedSet.last());
	}

	@Test
	public void testLastWithEmptySet() {
		assertTrue(dynamicSortedSet.isEmpty());
		try {
			dynamicSortedSet.last();
			fail("should have thrown NoSuchElementException");
		}
		catch (NoSuchElementException nsee) {
			// expected
		}

	}

}
