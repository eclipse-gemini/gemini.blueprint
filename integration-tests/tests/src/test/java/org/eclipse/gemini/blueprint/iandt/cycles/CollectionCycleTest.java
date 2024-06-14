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

package org.eclipse.gemini.blueprint.iandt.cycles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

/**
 * Integration test for checking cyclic injection between an importer and its
 * listeners.
 * 
 * @author Costin Leau
 */
public class CollectionCycleTest extends BaseImporterCycleTest {

	private Collection importer;


	protected String[] getConfigLocations() {
		return new String[] { "/org/eclipse/gemini/blueprint/iandt/cycles/top-level-collection-importer.xml" };
	}

	protected void onSetUp() throws Exception {
		super.onSetUp();
		importer = (Collection) applicationContext.getBean("importer");
		assertTrue(applicationContext.isSingleton("importer"));
		assertTrue(applicationContext.isSingleton("&importer"));
	}

	@Test
	public void testListenerA() throws Exception {
		assertEquals(importer.toString(), listenerA.getTarget().toString());
	}

	@Test
	public void testListenerB() throws Exception {
		assertEquals(importer.toString(), listenerB.getTarget().toString());
	}

	@Test
	public void testListenersBetweenThem() throws Exception {
		Object a = listenerA.getTarget();
		Object b = listenerB.getTarget();
		assertSame(listenerA.getTarget(), listenerB.getTarget());
	}
}
