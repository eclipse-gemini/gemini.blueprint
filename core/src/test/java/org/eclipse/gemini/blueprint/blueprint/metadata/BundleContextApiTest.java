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

package org.eclipse.gemini.blueprint.blueprint.metadata;

import java.util.Set;

/**
 * Basic test for the ModuleContext API. Some of the metadata calls are checked by different tests.
 * 
 * 
 * @author Costin Leau
 */
public class BundleContextApiTest extends BaseMetadataTest {

	@Override
	protected String getConfig() {
		return "/org/eclipse/gemini/blueprint/blueprint/config/mixed-rfc124-beans.xml";
	}

	public void testComponentNames() throws Exception {
		Set<String> names = blueprintContainer.getComponentIds();
		assertEquals(7, names.size());
	}

	public void tstBundleContext() {
		//assertSame(bundleContext, blueprintContainer.getBundleContext());
	}

	public void testComponent() {
		checkBeanAssertion("simple-component");
		checkBeanAssertion("nested-bean");
	}

	private void checkBeanAssertion(String name) {
		assertSame(applicationContext.getBean(name), blueprintContainer.getComponentInstance(name));
	}

	public void testComponentMetadata() {
		assertNotNull(blueprintContainer.getComponentMetadata("simple-component"));
		assertNotNull(blueprintContainer.getComponentMetadata("nested-bean"));
	}
}