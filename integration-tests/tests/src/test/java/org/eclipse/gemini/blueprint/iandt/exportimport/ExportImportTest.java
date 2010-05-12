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

package org.eclipse.gemini.blueprint.iandt.exportimport;

import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;

/**
 * @author Costin Leau
 */
public class ExportImportTest extends BaseIntegrationTest {

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "org/eclipse/gemini/blueprint/iandt/exportimport/export-import.xml" };
	}

	public void testCollectionSize() throws Exception {
		List list = (List) applicationContext.getBean("list");
		assertEquals(2, list.size());
		assertEquals(2, Listener.bind);
	}
	
	public void testExportNA() throws Exception {
		applicationContext.getBean("export-na");
		System.out.println(Listener.unbind);
		assertEquals(1, Listener.unbind);		
	}
}
