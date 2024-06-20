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

package org.eclipse.gemini.blueprint.iandt.testingFramework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.junit.Test;

/**
 * Test the injection executed on the current test. This verifies that the
 * application context has been created and that injection properly executes.
 * 
 * @author Costin Leau
 * 
 */
public class AppCtxInjectionTest extends BaseIntegrationTest {

	private HashMap map;


	public void setMap(HashMap map) {
		this.map = map;
	}

	@Test
	public void testInjection() throws Exception {
		System.out.println(Arrays.toString(applicationContext.getBeanDefinitionNames()));
		assertNotNull(map);
		assertEquals(applicationContext.getBean("injected-bean"), map);
	}

	protected String[] getConfigLocations() {
		return new String[] { "/org/eclipse/gemini/blueprint/iandt/testingFramework/AppCtxInjectionTest.xml" };
	}
}
