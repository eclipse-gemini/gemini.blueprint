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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.DynamicSortedSet;

/**
 * 
 * @author Costin Leau
 * 
 */
public class DynamicSortedSetIteratorTest extends TestCase {

	private SortedSet dynamicSortedSet;

	private List list;

	private Integer one;

	private Integer two;

	private Integer three;

	private Iterator iter;

	protected void setUp() throws Exception {
		dynamicSortedSet = new DynamicSortedSet();

		one = new Integer(1);
		two = new Integer(2);
		three = new Integer(3);

		list = new ArrayList();
		list.add(one);
		list.add(two);
		list.add(three);
		list.add(two);

		iter = dynamicSortedSet.iterator();
	}

	protected void tearDown() throws Exception {
		dynamicSortedSet = null;
		list = null;
		one = null;
		two = null;
		three = null;
		iter = null;
	}

	public void testIteratingWhileAdding() {
		assertFalse(iter.hasNext());

		assertTrue(dynamicSortedSet.add(two));

		assertTrue(iter.hasNext());
		assertSame(two, iter.next());

		// added before two
		dynamicSortedSet.add(one);
		assertFalse(iter.hasNext());
	}

	public void testIteratingWhileAddingSeveralElements() {
		assertFalse(iter.hasNext());

		dynamicSortedSet.add(two);
		dynamicSortedSet.add(three);

		assertTrue(iter.hasNext());
		assertSame(two, iter.next());

		// added before two
		dynamicSortedSet.add(one);
		assertTrue(iter.hasNext());
		assertSame(three, iter.next());
	}

	public void testDoubleIteratingWithAdding() {
		assertFalse(iter.hasNext());
		Iterator it = dynamicSortedSet.iterator();

		dynamicSortedSet.add(two);

		assertTrue(iter.hasNext());
		assertSame(two, iter.next());
		dynamicSortedSet.add(one);

		assertFalse(iter.hasNext());
		assertSame(one, it.next());
		assertSame(two, it.next());
	}

	public void testIteratingWhileRemoving() {
		dynamicSortedSet.add(two);
		dynamicSortedSet.add(three);

		assertSame(two, iter.next());
		dynamicSortedSet.remove(two);
		assertSame(three, iter.next());
	}

	public void testDoubleIteratingWhileRemoving() {
		Iterator it = dynamicSortedSet.iterator();
		dynamicSortedSet.add(one);
		dynamicSortedSet.add(three);
		dynamicSortedSet.add(two);

		assertSame(one, iter.next());
		assertSame(two, iter.next());
		dynamicSortedSet.remove(two);
		assertSame(one, it.next());

		dynamicSortedSet.remove(three);
		assertFalse(iter.hasNext());
		assertFalse(it.hasNext());
	}

}
