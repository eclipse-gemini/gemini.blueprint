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

package org.eclipse.gemini.blueprint.mock;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.eclipse.gemini.blueprint.mock.ArrayEnumerator;

import junit.framework.TestCase;

/**
 * @author Costin Leau
 * 
 */
public class ArrayEnumeratorTest extends TestCase {

	private Enumeration enm;

	private Object[] source;

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		source = new Object[] { "A", "B", "C" };
		enm = new ArrayEnumerator(source);
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		enm = null;
		source = null;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.ArrayEnumerator#hasMoreElements()}.
	 */
	public void testHasMoreElements() {
		assertTrue(enm.hasMoreElements());
	}

	public void testHasMoreElementsWithEmptySource() {
		enm = new ArrayEnumerator(new Object[0]);
		assertFalse(enm.hasMoreElements());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.ArrayEnumerator#nextElement()}.
	 */
	public void testNextElement() {
		assertEquals("A", enm.nextElement());
		assertEquals("B", enm.nextElement());
		assertEquals("C", enm.nextElement());
	}

	public void testNextElementException() {
		enm = new ArrayEnumerator(new Object[0]);
		try {
			enm.nextElement();
			fail("should have thrown exception");
		}
		catch (NoSuchElementException ex) {
			// expected
		}
	}

}
