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

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.ServiceRegistration;
import org.eclipse.gemini.blueprint.test.AbstractConfigurableBundleCreatorTests;

/**
 * @author Costin Leau
 * 
 */
public class ExporterCycleTest extends BaseIntegrationTest {

	private ListenerA listenerA;
	private ListenerB listenerB;
	private ServiceRegistration registration;


	protected String[] getConfigLocations() {
		return new String[] { "/org/eclipse/gemini/blueprint/iandt/cycles/top-level-exporter.xml" };
	}

	public void testListenerA() throws Exception {
		assertSame(registration, listenerA.getTarget());
	}
	
	public void testListenerB() throws Exception {
		assertSame(registration, listenerB.getTarget());
	}

	
	public void testListenersBetweenThem() throws Exception {
		assertSame(listenerB.getTarget(), listenerA.getTarget());
	}


	public void setListenerA(ListenerA listener) {
		this.listenerA = listener;
	}

	public void setListenerB(ListenerB nestedListener) {
		this.listenerB = nestedListener;
	}

	public void setRegistration(ServiceRegistration registration) {
		this.registration = registration;
	}

}
