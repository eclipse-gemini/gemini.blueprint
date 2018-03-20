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

import org.osgi.framework.Bundle;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;

/**
 * Integration test for Sync NoWait.
 * 
 * Start two bundles, one which requires a dependency and one which provides it.
 * However, since they are started synchronously, the first one will fail.
 * 
 * @author Costin Leau
 * 
 */
public class SyncNoWaitTest extends BehaviorBaseTest {

	private String tailBundleId = "org.eclipse.gemini.blueprint.iandt, sync-tail-bundle,"
			+ getGeminiBlueprintVersion();

	private String bundleId = "org.eclipse.gemini.blueprint.iandt, sync-nowait-bundle,"
			+ getGeminiBlueprintVersion();

	public void testBehaviour() throws Exception {

		// locate bundle
		Bundle bundle = installBundle(bundleId);
		Bundle tail = installBundle(tailBundleId);

		// start bundle first
		bundle.start();

		assertTrue("bundle " + bundle + "hasn't been fully started", OsgiBundleUtils.isBundleActive(bundle));

		// followed by its tail
		tail.start();
		assertTrue("bundle " + tail + "hasn't been fully started", OsgiBundleUtils.isBundleActive(tail));

		// wait for the listener to get the bundles and wait for timeout

		// make sure the appCtx is not up
		// check that the appCtx is *not* published (it waits for the service to
		// appear)
		assertContextServiceIs(bundle, false, 3000);

		// wait for appCtx to timeout
		//Thread.sleep(3000);

		// check that the dependency service is actually started as the
		// dependency
		// bundle has started
		assertNotNull(bundleContext.getServiceReference(Shape.class.getName()));
	}

}
