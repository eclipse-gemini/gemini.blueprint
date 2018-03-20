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

import java.awt.Point;

import org.osgi.framework.Bundle;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;

/**
 * Integration test for publish-context directive.
 * 
 * @author Costin Leau
 * 
 */
public class NoPublishTest extends BehaviorBaseTest {

	public void testBehaviour() throws Exception {
		String bundleId = "org.eclipse.gemini.blueprint.iandt, nopublish-bundle,"
				+ getGeminiBlueprintVersion();

		// start it
		Bundle bundle = installBundle(bundleId);
		bundle.start();
		// wait for the listener to catch up
		Thread.sleep(1000);
		assertTrue("bundle " + bundle + "hasn't been fully started", OsgiBundleUtils.isBundleActive(bundle));

		// check that the appCtx is not publish
		assertContextServiceIs(bundle, false, 1000);

		// but the point service is
		assertNotNull("point service should have been published"
				+ bundleContext.getServiceReference(Point.class.getName()));
	}
}
