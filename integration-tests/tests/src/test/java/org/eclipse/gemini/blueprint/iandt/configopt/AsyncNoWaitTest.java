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

package org.eclipse.gemini.blueprint.iandt.configopt;

import java.awt.Shape;
import java.awt.geom.Area;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;

/**
 * Integration test for AsyncNoWait
 * 
 * @author Costin Leau
 * 
 */
public class AsyncNoWaitTest extends BehaviorBaseTest {

	private ServiceRegistration registration;

	protected void onTearDown() throws Exception {
		OsgiServiceUtils.unregisterService(registration);
	}

	public void testBehaviour() throws Exception {
		String bundleId = "org.eclipse.gemini.blueprint.iandt, async-nowait-bundle,"
				+ getSpringDMVersion();

		// start it
		Bundle bundle = installBundle(bundleId);
		bundle.start();

		// wait for the bundle to start and fail
		Thread.sleep(3000);

		// put service up
		registration = bundleContext.registerService(Shape.class.getName(), new Area(), null);

		assertTrue("bundle " + bundle + "hasn't been fully started", OsgiBundleUtils.isBundleActive(bundle));

		// check that the appCtx is *not* published 
		// TODO: this fails sometimes on the build server - find out why
		// assertContextServiceIs(bundle, false, 1000);
	}
}
