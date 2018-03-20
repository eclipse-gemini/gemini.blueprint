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

package org.eclipse.gemini.blueprint.iandt.jdk5.io;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.iandt.io.component.ComponentBean;

/**
 * Integration test for Spring 2.5 component scan.
 * 
 * @author Costin Leau
 * 
 */
public class ComponentScanTest extends BaseIntegrationTest {

	protected String[] getConfigLocations() {
		return new String[] { "/org/eclipse/gemini/blueprint/iandt/jdk5/io/component-scan.xml" };
	}

	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt,component.scan.bundle," + getGeminiBlueprintVersion() };
	}

	public void testComponentScan() throws Exception {
		// force an import on component bean
		assertNotNull(ComponentBean.class);
		assertTrue("component scan did not pick up all classes", applicationContext.containsBean("bean"));
		assertTrue("component scan did not pick up all classes", applicationContext.containsBean("componentBean"));
	}
}
