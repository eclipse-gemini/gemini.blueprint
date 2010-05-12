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

package org.eclipse.gemini.blueprint.iandt.extender;

import java.awt.Point;
import java.io.FilePermission;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.springframework.core.io.Resource;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;

/**
 * @author Costin Leau
 * 
 */
public class ExtenderVersionTest extends BaseIntegrationTest {

	protected String getManifestLocation() {
		return null;
	}

	// given bundle should not be picked up by the extender since it expects a
	// certain version
	public void testBundleIgnoredBasedOnSpringExtenderVersion() throws Exception {

		String bundleId = "org.eclipse.gemini.blueprint.iandt, extender-version-bundle," + getSpringDMVersion();
		Resource location = locateBundle(bundleId);

		Bundle bundle = bundleContext.installBundle(location.getURL().toString());
		assertNotNull(bundle);
		bundle.start();

		assertTrue(OsgiBundleUtils.isBundleActive(bundle));
		assertNull("no point should be published ", bundleContext.getServiceReference(Point.class.getName()));
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		perms.add(new AdminPermission("*", AdminPermission.RESOLVE));
		perms.add(new FilePermission("<<ALL FILES>>", "read"));
		return perms;
	}
}
