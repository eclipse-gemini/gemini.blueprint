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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.DynamicSet;

/**
 * @author Costin Leau
 * 
 */
public class DynamicSetTest {
	private Set dynamicSet;

	private Object obj;

	private List list;

	@Before
	public void setup() throws Exception {
		dynamicSet = new DynamicSet();
		obj = new Object();
		list = new ArrayList();
		list.add(obj);
		list.add(obj);

	}

	@After
	public void tearDown() throws Exception {
		dynamicSet = null;
		list = null;
		obj = null;
	}

	@Test
	public void testNoDuplicatesWhileAdding() {
		Object obj = new Object();
		assertTrue(dynamicSet.add(obj));
		assertFalse(dynamicSet.add(obj));
	}

	@Test
	public void testNoDuplicatesAfterRemovalAndAdding() {
		Object obj = new Object();
		assertTrue(dynamicSet.add(obj));
		assertTrue(dynamicSet.remove(obj));
		assertTrue(dynamicSet.add(obj));
	}

	@Test
	public void testNullAllowed() {
		assertTrue(dynamicSet.add(null));
		assertFalse(dynamicSet.add(null));
	}

	@Test
	public void testAddAllWithCollectionThatContainsDuplicates() {
		dynamicSet.addAll(list);
		assertTrue(dynamicSet.contains(obj));
		assertFalse(dynamicSet.add(obj));
	}

	@Test
	public void testContainsAllWithCollectionsThatContainsDuplicates() {
		assertFalse(dynamicSet.containsAll(list));
		dynamicSet.add(obj);
		assertTrue(dynamicSet.containsAll(list));
	}

	@Test
	public void testRetainAllWithCollectionThatContainsDuplicates() {
		assertFalse(dynamicSet.retainAll(list));
		dynamicSet.add(obj);
		assertFalse(dynamicSet.retainAll(list));
		dynamicSet.add(new Object());
		assertTrue(dynamicSet.retainAll(list));
	}

	@Test
	public void testRemoveAllWithCollectionThatContainsDuplicates() {
		assertFalse(dynamicSet.removeAll(list));
		dynamicSet.add(obj);
		assertTrue(dynamicSet.removeAll(list));
		assertTrue(dynamicSet.isEmpty());
	}

}
