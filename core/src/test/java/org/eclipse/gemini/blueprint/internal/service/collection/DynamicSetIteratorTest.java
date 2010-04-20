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
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.DynamicSet;

public class DynamicSetIteratorTest extends TestCase {

	private Set dynamicSet;

	private Iterator iter;

	private Integer one;

	private Integer two;

	private Integer three;

	protected void setUp() throws Exception {
		dynamicSet = new DynamicSet();
		iter = dynamicSet.iterator();

		one = new Integer(1);
		two = new Integer(2);
		three = new Integer(3);

	}

	protected void tearDown() throws Exception {
		dynamicSet = null;
		iter = null;
		one = null;
		two = null;
		three = null;
	}

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

	public void testIteratingWhileAddingDuplicate() {
		assertFalse(iter.hasNext());
		dynamicSet.add(one);
		assertTrue(iter.hasNext());
		assertSame(one, iter.next());
		dynamicSet.add(one);
		assertFalse(iter.hasNext());
	}

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
