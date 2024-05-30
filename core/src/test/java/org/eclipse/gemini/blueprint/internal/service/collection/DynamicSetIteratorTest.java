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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.DynamicSet;

public class DynamicSetIteratorTest {

	private Set dynamicSet;

	private Iterator iter;

	private Integer one;

	private Integer two;

	private Integer three;

	@Before
	public void setup() throws Exception {
		dynamicSet = new DynamicSet();
		iter = dynamicSet.iterator();

		one = Integer.valueOf(1);
		two = Integer.valueOf(2);
		three = Integer.valueOf(3);

	}

	@After
	public void tearDown() throws Exception {
		dynamicSet = null;
		iter = null;
		one = null;
		two = null;
		three = null;
	}

	@Test
	public void testIteratingWhileAdding() {
		assertFalse(iter.hasNext());
		dynamicSet.add(one);
		assertTrue(iter.hasNext());
		assertSame(one, iter.next());

		dynamicSet.add(two);
		assertTrue(iter.hasNext());
		dynamicSet.add(three);
		assertSame(two, iter.next());
		assertSame(three, iter.next());
	}

	@Test
	public void testIteratingWhileAddingDuplicate() {
		assertFalse(iter.hasNext());
		dynamicSet.add(one);
		assertTrue(iter.hasNext());
		assertSame(one, iter.next());
		dynamicSet.add(one);
		assertFalse(iter.hasNext());
	}

	@Test
	public void testIteratingWhileRemovingValidItem() {
		assertFalse(iter.hasNext());
		dynamicSet.add(one);
		dynamicSet.add(two);
		dynamicSet.add(three);
		assertTrue(iter.hasNext());
		dynamicSet.remove(one);
		assertSame(two, iter.next());
		dynamicSet.add(one);
		assertTrue(iter.hasNext());
		assertSame(three, iter.next());
	}

	@Test
	public void testIteratingWhileAddingCollection() {
		assertFalse(iter.hasNext());
		List list = new ArrayList();
		list.add(two);
		list.add(two);
		list.add(three);

		dynamicSet.add(three);
		assertTrue(iter.hasNext());

		dynamicSet.addAll(list);
		assertSame(three, iter.next());
		assertSame(two, iter.next());
		assertFalse(iter.hasNext());
	}

}
