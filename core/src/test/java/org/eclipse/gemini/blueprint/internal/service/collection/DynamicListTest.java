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

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.DynamicList;

/**
 * @author Costin Leau
 * 
 */
public class DynamicListTest extends TestCase {

	private List dynamicList;

	private ListIterator iter;


	protected void setUp() throws Exception {
		dynamicList = new DynamicList();
		iter = dynamicList.listIterator();
	}

	protected void tearDown() throws Exception {
		dynamicList = null;
		iter = null;
	}

	public void testAddWhileListIterating() throws Exception {
		assertTrue(dynamicList.isEmpty());
		assertFalse(iter.hasNext());
		Object a = new Object();
		dynamicList.add(a);
		assertTrue(iter.hasNext());
		assertFalse(iter.hasPrevious());
		assertSame(a, iter.next());
		assertFalse(iter.hasNext());
		assertTrue(iter.hasPrevious());
		assertSame(a, iter.previous());
	}

	public void testRemoveWhileListIterating() throws Exception {
		assertTrue(dynamicList.isEmpty());
		assertFalse(iter.hasNext());
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();
		dynamicList.add(a);
		dynamicList.add(b);
		dynamicList.add(c);

		assertTrue(iter.hasNext());
		assertSame(a, iter.next());
		// remove b
		dynamicList.remove(b);
		assertTrue(iter.hasNext());
		assertSame(c, iter.next());
		assertFalse(iter.hasNext());
		assertSame(c, iter.previous());
		// remove c
		dynamicList.remove(c);
		assertFalse(iter.hasNext());
		assertTrue(iter.hasPrevious());
		assertSame(a, iter.previous());
	}

	public void testRemovePreviouslyIteratedWhileIterating() throws Exception {
		assertTrue(dynamicList.isEmpty());
		assertFalse(iter.hasNext());

		Object a = new Object();
		Object b = new Object();
		dynamicList.add(a);
		dynamicList.add(b);

		assertTrue(iter.hasNext());
		assertSame(a, iter.next());
		assertTrue(iter.hasNext());
		dynamicList.remove(a);
		// still have b
		assertTrue(iter.hasNext());
		assertFalse(iter.hasPrevious());
	}

	public void testListIteratorIndexes() throws Exception {
		assertTrue(dynamicList.isEmpty());
		assertFalse(iter.hasNext());

		Object a = new Object();
		Object b = new Object();
		dynamicList.add(a);
		dynamicList.add(b);

		assertTrue(iter.hasNext());
		assertEquals(0, iter.nextIndex());
		assertEquals(-1, iter.previousIndex());
		assertSame(a, iter.next());
		assertEquals(1, iter.nextIndex());
		assertEquals(0, iter.previousIndex());
		dynamicList.remove(b);
		assertEquals(1, iter.nextIndex());
		assertEquals(0, iter.previousIndex());

		assertSame(a, iter.previous());
		assertEquals(0, iter.nextIndex());
		assertEquals(-1, iter.previousIndex());

		dynamicList.remove(a);
		assertEquals(0, iter.nextIndex());
		assertEquals(-1, iter.previousIndex());

	}

	public void testListIteratorAdd() {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();

		assertFalse(iter.hasNext());
		assertFalse(iter.hasPrevious());
		iter.add(a);
		assertTrue(iter.hasNext());
		assertFalse(iter.hasPrevious());
		assertEquals(1, dynamicList.size());

		iter.add(b);
		assertEquals(2, dynamicList.size());
		assertFalse(iter.hasPrevious());
		iter.add(c);
		assertEquals(3, dynamicList.size());
		assertFalse(iter.hasPrevious());

		assertSame(c, iter.next());
		assertEquals(1, iter.nextIndex());
		assertSame(b, iter.next());
		assertTrue(iter.hasPrevious());
		assertSame(a, iter.next());
	}

	public void testListIteratorHasNextHasPrevious() {
		Object a = new Object();
		dynamicList.add(a);
		assertEquals(1, dynamicList.size());

		// go forward and back
		assertSame(iter.next(), iter.previous());
		assertFalse(iter.hasPrevious());
		assertSame(a, iter.next());
		try {
			iter.previous();
		}
		catch (NoSuchElementException nsee) {
			// expected (since the previous hasPrevious returned false
		}

		assertTrue(iter.hasPrevious());

		assertSame(iter.previous(), iter.next());
		assertFalse(iter.hasNext());
	}

	public void testListIteratorNextIndexPreviousIndex() {
		Object a = new Object();

		assertEquals(-1, iter.previousIndex());
		assertEquals(0, iter.nextIndex());
		dynamicList.add(a);

		iter.next();
		assertEquals(0, iter.previousIndex());
		assertEquals(dynamicList.size(), iter.nextIndex());
		assertFalse(iter.hasNext());

		iter.remove();
		assertEquals(-1, iter.previousIndex());
		assertEquals(dynamicList.size(), iter.nextIndex());
	}

	public void testListIteratorPreviousException() {
		try {
			iter.previous();
			fail("should have thrown " + NoSuchElementException.class);
		}
		catch (NoSuchElementException ex) {
			// expected
		}

		try {
			iter.remove();
			fail("should have thrown " + IndexOutOfBoundsException.class + " or one of its subclasses");
		}
		catch (IndexOutOfBoundsException ex) {
			// expected
		}
	}

	public void testListIteratorRemoveBetweenOperations() {
		Object a = new Object();
		Object b = new Object();

		dynamicList.add(a);
		iter.next();
		iter.add(b);

		try {
			iter.remove();
			fail("should have thrown " + IllegalStateException.class);
		}
		catch (IllegalStateException ex) {
			// expected
		}

		Object o = iter.next();
		iter.set(o);
		iter.remove();

		try {
			iter.remove();
			fail("should have thrown " + IllegalStateException.class);
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}

	public void testListIteratorSetWithNext() {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();

		dynamicList.add(a);

		try {
			iter.set(c);
			fail("should have thrown " + IllegalStateException.class);
		}
		catch (IllegalStateException ex) {
			// expected
		}

		iter.next();
		iter.set(b);

		assertEquals(1, dynamicList.size());
		assertSame(b, dynamicList.get(0));
		assertFalse(iter.hasNext());

		iter.set(c);

		assertEquals(1, dynamicList.size());
		assertSame(c, dynamicList.get(0));
		assertFalse(iter.hasNext());

		iter.add(b);

		try {
			iter.set(c);
			fail("should have thrown " + IllegalStateException.class);
		}
		catch (IllegalStateException ex) {
			// expected
		}

		assertTrue(iter.hasNext());

		iter.next();
		iter.set(c);
		iter.remove();

		try {
			iter.set(c);
			fail("should have thrown " + IllegalStateException.class);
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}

	public void testListIteratorSetWithPrevious() {
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();

		dynamicList.add(a);

		try {
			iter.set(c);
			fail("should have thrown " + IllegalStateException.class);
		}
		catch (IllegalStateException ex) {
			// expected
		}

		iter.next();
		iter.previous();
		iter.set(b);

		assertEquals(1, dynamicList.size());
		assertSame(b, dynamicList.get(0));
		assertTrue(iter.hasNext());
		// move forward and back
		assertSame(iter.next(), iter.previous());

		iter.set(c);

		assertEquals(1, dynamicList.size());
		assertSame(c, dynamicList.get(0));
		assertTrue(iter.hasNext());
		// move forward and back
		assertSame(iter.next(), iter.previous());

		iter.add(b);

		try {
			iter.set(c);
			fail("should have thrown " + IllegalStateException.class);
		}
		catch (IllegalStateException ex) {
			// expected
		}

		assertTrue(iter.hasNext());
		// move forward and back
		assertSame(iter.next(), iter.previous());

		iter.set(c);
		assertSame(c, dynamicList.get(1));
		iter.remove();

		try {
			iter.set(c);
			fail("should have thrown " + IllegalStateException.class);
		}
		catch (IllegalStateException ex) {
			// expected
		}
		assertSame(c, dynamicList.get(0));
	}
}