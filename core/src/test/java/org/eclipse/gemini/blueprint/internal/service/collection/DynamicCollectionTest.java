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

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.DynamicCollection;

/**
 * Tests regarding the collection behaviour.
 * 
 * @author Costin Leau
 * 
 */
@SuppressWarnings("unchecked")
public class DynamicCollectionTest extends TestCase {

	private Collection dynamicCollection;


	protected void setUp() throws Exception {
		dynamicCollection = new DynamicCollection();
	}

	protected void tearDown() throws Exception {
		dynamicCollection = null;
	}

	public void testAdd() {
		assertTrue(dynamicCollection.add(new Object()));
	}

	public void testAddDuplicate() {
		Object obj = new Object();
		assertTrue(dynamicCollection.add(obj));
		assertTrue(dynamicCollection.add(obj));
	}

	public void testRemove() {
		Object obj = new Object();
		assertFalse(dynamicCollection.remove(obj));
		dynamicCollection.add(obj);
		assertTrue(dynamicCollection.remove(obj));
		assertFalse(dynamicCollection.remove(obj));
	}

	public void testRemoveDuplicate() {
		Object obj = new Object();
		assertFalse(dynamicCollection.remove(obj));
		dynamicCollection.add(obj);
		dynamicCollection.add(obj);
		assertTrue(dynamicCollection.remove(obj));
		assertTrue(dynamicCollection.remove(obj));
		dynamicCollection.add(obj);
		assertTrue(dynamicCollection.remove(obj));
	}

}
