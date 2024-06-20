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

package org.eclipse.gemini.blueprint.iandt.jdk5.bridgemethods;

import static org.junit.Assert.assertEquals;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

/**
 * Integration test for listeners with bridge methods.
 * 
 * @author Costin Leau
 * 
 */
public class BridgeMethodTest extends BaseIntegrationTest {

	protected String[] getConfigLocations() {
		return new String[] { "/org/eclipse/gemini/blueprint/iandt/jdk5/bridgemethods/config.xml" };
	}

	@Test
	public void testGenerifiedListener() throws Exception {
		assertEquals(Listener.BIND_CALLS, 0);
		assertEquals(Listener.UNBIND_CALLS, 1);

		// register a point
		ServiceRegistration reg = bundleContext.registerService(Shape.class.getName(), new Area(), null);
		List list = (List) applicationContext.getBean("collection");
		assertEquals(1, list.size());
		assertEquals(Listener.BIND_CALLS, 1);
		assertEquals(Listener.UNBIND_CALLS, 1);

		reg.unregister();
		assertEquals(Listener.BIND_CALLS, 1);
		assertEquals(Listener.UNBIND_CALLS, 2);
	}
}
