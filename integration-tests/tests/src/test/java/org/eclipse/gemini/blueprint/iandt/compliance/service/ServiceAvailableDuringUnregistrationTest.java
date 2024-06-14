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

package org.eclipse.gemini.blueprint.iandt.compliance.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.awt.Polygon;
import java.awt.Shape;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;
import org.junit.Test;

/**
 * @author Costin Leau
 * 
 */
// works on KF 2.0.3+
public class ServiceAvailableDuringUnregistrationTest extends BaseIntegrationTest {

	private Shape service;

	@Test
	public void testServiceAliveDuringUnregistration() throws Exception {
		service = new Polygon();

		ServiceRegistration reg = bundleContext.registerService(Shape.class.getName(), service, null);

		String filter = OsgiFilterUtils.unifyFilter(Shape.class, null);

		ServiceListener listener = new ServiceListener() {

			public void serviceChanged(ServiceEvent event) {
				if (ServiceEvent.UNREGISTERING == event.getType()) {
					ServiceReference ref = event.getServiceReference();
					Object aliveService = bundleContext.getService(ref);
					assertNotNull("services not available during unregistration", aliveService);
					assertSame(service, aliveService);
				}
			}
		};

		try {
			bundleContext.addServiceListener(listener, filter);
			reg.unregister();
		}
		finally {
			bundleContext.removeServiceListener(listener);
		}
	}
}
