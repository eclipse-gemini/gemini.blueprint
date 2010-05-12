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

import java.awt.Polygon;
import java.awt.Shape;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.ServiceRegistration;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;

/**
 * @author Costin Leau
 * 
 */
public abstract class BaseImporterCycleTest extends BaseIntegrationTest {

	protected ListenerA listenerA;
	protected ListenerB listenerB;
	private Shape service;
	private ServiceRegistration registration;

	protected void onSetUp() throws Exception {
		service = new Polygon();
		registration = bundleContext.registerService(Shape.class.getName(), service, null);
	}

	protected void onTearDown() throws Exception {
		service = null;
		OsgiServiceUtils.unregisterService(registration);
	}

	public void setListenerA(ListenerA listener) {
		this.listenerA = listener;
	}

	public void setListenerB(ListenerB nestedListener) {
		this.listenerB = nestedListener;
	}
}
