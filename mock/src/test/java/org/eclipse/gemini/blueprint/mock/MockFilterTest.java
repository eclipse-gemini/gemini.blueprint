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

import java.util.Hashtable;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.mock.MockFilter;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.osgi.framework.Filter;

/**
 * @author Costin Leau
 * 
 */
public class MockFilterTest extends TestCase {

	Filter mock;

	protected void setUp() throws Exception {
		mock = new MockFilter();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockFilter#match(org.osgi.framework.ServiceReference)}.
	 */
	public void testMatchServiceReference() {
		assertFalse(mock.match(new MockServiceReference()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockFilter#match(java.util.Dictionary)}.
	 */
	public void testMatchDictionary() {
		assertFalse(mock.match(new Hashtable()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockFilter#matchCase(java.util.Dictionary)}.
	 */
	public void testMatchCase() {
		assertFalse(mock.matchCase(new Hashtable()));
	}

}
