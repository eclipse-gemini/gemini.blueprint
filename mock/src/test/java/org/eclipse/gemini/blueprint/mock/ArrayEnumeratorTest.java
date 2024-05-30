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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Costin Leau
 * 
 */
public class ArrayEnumeratorTest {

	private Enumeration enm;

	private Object[] source;

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setup() throws Exception {
		source = new Object[] { "A", "B", "C" };
		enm = new ArrayEnumerator(source);
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	public void tearDown() throws Exception {
		enm = null;
		source = null;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.ArrayEnumerator#hasMoreElements()}.
	 */
	@Test
	public void testHasMoreElements() {
		assertTrue(enm.hasMoreElements());
	}

	@Test
	public void testHasMoreElementsWithEmptySource() {
		enm = new ArrayEnumerator(new Object[0]);
		assertFalse(enm.hasMoreElements());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.ArrayEnumerator#nextElement()}.
	 */
	@Test
	public void testNextElement() {
		assertEquals("A", enm.nextElement());
		assertEquals("B", enm.nextElement());
		assertEquals("C", enm.nextElement());
	}

	@Test
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
