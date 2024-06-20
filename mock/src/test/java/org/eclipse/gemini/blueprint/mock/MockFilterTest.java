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

import static org.junit.Assert.assertFalse;

import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Filter;

/**
 * @author Costin Leau
 * 
 */
public class MockFilterTest {

	Filter mock;

	@Before
	public void setup() throws Exception {
		mock = new MockFilter();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockFilter#match(org.osgi.framework.ServiceReference)}.
	 */
	@Test
	public void testMatchServiceReference() {
		assertFalse(mock.match(new MockServiceReference()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockFilter#match(java.util.Dictionary)}.
	 */
	@Test
	public void testMatchDictionary() {
		assertFalse(mock.match(new Hashtable()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockFilter#matchCase(java.util.Dictionary)}.
	 */
	@Test
	public void testMatchCase() {
		assertFalse(mock.matchCase(new Hashtable()));
	}
}
