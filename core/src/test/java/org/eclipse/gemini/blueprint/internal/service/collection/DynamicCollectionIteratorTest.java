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
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.DynamicCollection;

/**
 * Tests related to the dynamic collection iterator (consistency and dynamic
 * nature).
 * 
 * @author Costin Leau
 * 
 */
public class DynamicCollectionIteratorTest {

	private Collection dynamicCollection;

	private Iterator iter;

	@Before
	public void setup() throws Exception {
		dynamicCollection = new DynamicCollection();
		iter = dynamicCollection.iterator();
	}

	@After
	public void tearDown() throws Exception {
		dynamicCollection = null;
		iter = null;
	}

	// iterating tests
	@Test
	public void testAddWhileIterating() throws Exception {
		assertTrue(dynamicCollection.isEmpty());
		assertFalse(iter.hasNext());
		Object a = new Object();
		dynamicCollection.add(a);

		assertTrue(iter.hasNext());
		assertSame(a, iter.next());
		assertFalse(iter.hasNext());
	}

	@Test
	public void testRemoveWhileIterating() throws Exception {
		assertTrue(dynamicCollection.isEmpty());
		assertFalse(iter.hasNext());
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();
		dynamicCollection.add(a);
		dynamicCollection.add(b);
		dynamicCollection.add(c);

		assertTrue(iter.hasNext());
		assertSame(a, iter.next());
		dynamicCollection.remove(b);
		assertTrue(iter.hasNext());
		assertSame(c, iter.next());
	}

	@Test
	public void testRemovePreviouslyIteratedWhileIterating() throws Exception {
		assertTrue(dynamicCollection.isEmpty());
		assertFalse(iter.hasNext());

		Object a = new Object();
		Object b = new Object();
		dynamicCollection.add(a);
		dynamicCollection.add(b);

		assertTrue(iter.hasNext());
		assertSame(a, iter.next());
		assertTrue(iter.hasNext());
		dynamicCollection.remove(a);
		// still have b
		assertTrue(iter.hasNext());
		assertSame(b, iter.next());
	}

	@Test
	public void testRemoveUniteratedWhileIterating() throws Exception {
		assertTrue(dynamicCollection.isEmpty());
		assertFalse(iter.hasNext());

		Object a = new Object();
		Object b = new Object();
		Object c = new Object();
		dynamicCollection.add(a);
		dynamicCollection.add(b);
		dynamicCollection.add(c);

		assertTrue(iter.hasNext());
		assertSame(a, iter.next());
		assertTrue(iter.hasNext());
		dynamicCollection.remove(a);
		// still have b
		assertTrue(iter.hasNext());
		dynamicCollection.remove(b);
		// still have c
		assertTrue(iter.hasNext());
		assertSame(c, iter.next());
	}

	@Test
	public void testIteratorRemove() throws Exception {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();

		dynamicCollection.add(a);
		dynamicCollection.add(b);
		dynamicCollection.add(c);

		assertTrue(iter.hasNext());
		try {
			iter.remove();
			fail("remove() can be called only after next()");
		}
		catch (IllegalStateException ex) {
			// expected
		}

		assertSame(a, iter.next());
		assertSame(b, iter.next());
		// remove b
		iter.remove();

		assertEquals(2, dynamicCollection.size());
		assertSame(c, iter.next());
		// remove c
		iter.remove();
		assertEquals(1, dynamicCollection.size());

		try {
			iter.remove();
			fail("remove() can be called only once for each next()");
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}

	@Test
	public void testRemoveAllWhileIterating() throws Exception {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();

		dynamicCollection.add(a);
		dynamicCollection.add(b);
		dynamicCollection.add(c);

		Collection col = new ArrayList();
		col.add(a);
		col.add(c);

		assertSame(a, iter.next());
		// remove a and c
		dynamicCollection.removeAll(col);
		assertSame(b, iter.next());
		assertFalse(iter.hasNext());
	}

	@Test
	public void testAddAllWhileIterating() throws Exception {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();

		dynamicCollection.add(a);

		Collection col = new ArrayList();
		col.add(b);
		col.add(c);

		assertSame(a, iter.next());
		assertFalse(iter.hasNext());
		dynamicCollection.addAll(col);
		assertTrue(iter.hasNext());
		assertSame(b, iter.next());
		assertSame(c, iter.next());
	}

	@Test
	public void testRemoveObjectWhenTheCollectionContainsDuplicates() throws Exception {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();

		// create a|b|a|c|a|a
		dynamicCollection.add(a);
		dynamicCollection.add(b);
		dynamicCollection.add(a);
		dynamicCollection.add(c);
		dynamicCollection.add(a);
		dynamicCollection.add(a);

		Iterator i1 = dynamicCollection.iterator();

		assertSame(a, iter.next());
		assertSame(b, iter.next());
		assertSame(a, iter.next());
		iter.remove();
		assertSame(a, i1.next());
		assertSame(b, i1.next());
		assertSame(c, i1.next());
		assertSame(a, i1.next());

		assertSame(c, iter.next());
		assertSame(a, iter.next());
		assertSame(a, iter.next());
		iter.remove();

		assertFalse(i1.hasNext());
		assertFalse(iter.hasNext());
	}

	@Test
	public void testRemoveUnexistingObj() throws Exception {
		Object a = new Object();
		Object b = new Object();

		dynamicCollection.add(a);

		assertFalse(dynamicCollection.remove(b));
		assertTrue(dynamicCollection.remove(a));
		dynamicCollection.add(b);
		assertFalse(dynamicCollection.remove(a));
		assertTrue(dynamicCollection.remove(b));
		assertFalse(dynamicCollection.remove(b));
	}

	@Test
	public void testCorrectExceptionThrownByIteratorWhenStructureChanges() {
		Object a = new Object();

		dynamicCollection.add(a);
		dynamicCollection.add(a);

		Iterator i1 = dynamicCollection.iterator();

		iter.next();
		iter.next();

		i1.next();
		i1.remove();
		i1.next();
		i1.remove();

		assertFalse(i1.hasNext());
		assertFalse(iter.hasNext());

		try {
			iter.remove();
			fail("should have thrown exception");
		}
		catch (IndexOutOfBoundsException ioobe) {
			// expected
		}
	}

	// consistency tests

	// 1. hasNext() reflects the latest collection updates (adding stuff)
	@Test
	public void testConsistentIteratorWhileAdding() throws Exception {
		assertTrue(dynamicCollection.isEmpty());
		assertFalse(iter.hasNext());
		Object a = new Object();
		dynamicCollection.add(a);

		assertTrue(iter.hasNext());
		assertSame(a, iter.next());
		assertFalse(iter.hasNext());
	}

	// 1. hasNext() reflect the changes when removing things
	@Test
	public void testConsistentIteratorWhileRemoving() throws Exception {
		assertTrue(dynamicCollection.isEmpty());
		assertFalse(iter.hasNext());
		Object a = new Object();
		dynamicCollection.add(a);

		assertTrue(iter.hasNext());
		dynamicCollection.remove(a);
		assertFalse(iter.hasNext());
	}

	// 2. hasNext() returns false -> next() throws Exception
	@Test
	public void testConsistentIteratorWithAddition() throws Exception {
		assertTrue(dynamicCollection.isEmpty());
		assertFalse(iter.hasNext());
		Object a = new Object();
		dynamicCollection.add(a);

		try {
			iter.next();
			fail("the iterator is inconsistent - since hasNext() returned false, next() should fail");
		}
		catch (NoSuchElementException e) {
			// expected
		}
	}

	// 3. hasNext() = true -> next() will NOT throw an exception no matter the
	// collection changes
	@Test
	public void testConsistentIterator() throws Exception {
		assertTrue(dynamicCollection.isEmpty());
		assertFalse(iter.hasNext());
		Object a = new Object();
		dynamicCollection.add(a);

		assertTrue(iter.hasNext());
		dynamicCollection.remove(a);

		assertSame(a, iter.next()); // should successed
	}

	// 4. double check hasNext() true -> next() will return the last object for each
	// iterator
	@Test
	public void testMultiIteratorHasNextConsistency() throws Exception {
		assertTrue(dynamicCollection.isEmpty());
		assertFalse(iter.hasNext());

		Iterator iter1 = dynamicCollection.iterator();

		Object a = new Object();
		Object b = new Object();
		Object c = new Object();
		dynamicCollection.add(a);
		dynamicCollection.add(b);

		Iterator iter2 = dynamicCollection.iterator();

		dynamicCollection.add(c);

		Iterator iter3 = dynamicCollection.iterator();

		// iter2 goes in the middle
		assertSame(a, iter2.next());
		// iter3 approaches the end
		assertSame(a, iter3.next());
		assertSame(b, iter3.next());

		// check hasNext() and force next()
		assertTrue(iter3.hasNext());
		assertTrue(iter1.hasNext());
		assertTrue(iter2.hasNext());

		dynamicCollection.remove(c);
		dynamicCollection.remove(b);
		dynamicCollection.remove(a);

		assertSame(a, iter1.next());
		assertSame(b, iter2.next());
		assertSame(c, iter3.next());
	}

	// similar test to the one above but the removal order is different
	@Test
	public void testMultiIteratorHasNextConsistencyGhostUpdate() throws Exception {
		assertTrue(dynamicCollection.isEmpty());
		assertFalse(iter.hasNext());

		Iterator iter1 = dynamicCollection.iterator();

		Object a = new Object();
		Object b = new Object();
		Object c = new Object();
		dynamicCollection.add(a);
		dynamicCollection.add(b);

		Iterator iter2 = dynamicCollection.iterator();

		dynamicCollection.add(c);

		Iterator iter3 = dynamicCollection.iterator();

		// iter2 goes in the middle
		assertSame(a, iter2.next());
		// iter3 approaches the end
		assertSame(a, iter3.next());
		assertSame(b, iter3.next());

		// check hasNext() and force next()
		assertTrue(iter3.hasNext());
		assertTrue(iter1.hasNext());
		assertTrue(iter2.hasNext());

		dynamicCollection.remove(a);
		dynamicCollection.remove(b);
		dynamicCollection.remove(c);

		assertSame(c, iter1.next());
		assertSame(c, iter2.next());
		assertSame(c, iter3.next());
	}

	@Test
	public void testMultipleIteratorsPositionAfterCompleteRemoval() throws Exception {
		Iterator iter1 = dynamicCollection.iterator();
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();
		dynamicCollection.add(a);
		dynamicCollection.add(b);
		dynamicCollection.add(c);
		Iterator iter2 = dynamicCollection.iterator();

		// iter1 goes in the middle
		assertSame(a, iter1.next());
		// iter2 approaches the end
		assertSame(a, iter2.next());
		assertSame(b, iter2.next());

		// check hasNext() and force next()
		assertTrue(iter1.hasNext());
		assertTrue(iter2.hasNext());

		dynamicCollection.remove(c);
		dynamicCollection.remove(b);
		dynamicCollection.remove(a);

		assertSame(b, iter1.next());
		assertSame(c, iter2.next());

		dynamicCollection.add(a);

		// note: the iterator will return the previous elements if they've just been added to the collection  
		assertSame(a, iter1.next());
		assertSame(a, iter2.next());
	}
}