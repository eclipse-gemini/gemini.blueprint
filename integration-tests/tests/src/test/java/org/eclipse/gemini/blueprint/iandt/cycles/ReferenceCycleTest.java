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

import java.awt.Shape;

import org.junit.Test;

/**
 * Integration test for checking cyclic injection between an importer and its
 * listeners.
 * 
 * @author Costin Leau
 */
public class ReferenceCycleTest extends BaseImporterCycleTest {

	private Shape importer;

	protected String[] getConfigLocations() {
		return new String[] { "/org/eclipse/gemini/blueprint/iandt/cycles/top-level-reference-importer.xml" };
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
		assertSame(listenerA.getTarget(), listenerB.getTarget());
	}

	public void setImporter(Shape importer) {
		this.importer = importer;
	}
}
