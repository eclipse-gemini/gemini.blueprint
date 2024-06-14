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

package org.eclipse.gemini.blueprint.iandt.importer;

import static org.junit.Assert.assertEquals;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.junit.Test;

/**
 * @author Costin Leau
 */
public class CollectionTest extends BaseIntegrationTest {

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "org/eclipse/gemini/blueprint/iandt/importer/collection.xml" };
	}

	@Test
	public void testServiceReferenceCollection() throws Exception {
		List list = applicationContext.getBean("reference-list", List.class);
		assertEquals(0, list.size());

		Listener listener = applicationContext.getBean("listener", Listener.class);
		assertEquals(0, listener.bind.size());
		Shape shape = new Area();
		bundleContext.registerService(Shape.class.getName(), shape, null);
		System.out.println("List is " + list);
		assertEquals(1, listener.bind.size());
	}
}
