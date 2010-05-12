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

import java.io.FilePermission;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.springframework.core.io.Resource;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.springframework.util.Assert;

/**
 * Base class with utility methods.
 * 
 * @author Costin Leau
 * 
 */
public abstract class BehaviorBaseTest extends BaseIntegrationTest {

	/**
	 * Does the given bundle, publish an application context or not?
	 * 
	 * @param alive
	 */
	protected void assertContextServiceIs(Bundle bundle, boolean alive, long maxWait) {
		Assert.notNull(bundle);

		try {
			waitOnContextCreation(bundle.getSymbolicName(), maxWait / 1000 + 1);
			if (!alive)
				fail("appCtx should have NOT been published for bundle "
						+ OsgiStringUtils.nullSafeNameAndSymName(bundle));
		}
		catch (RuntimeException timeout) {
			if (alive)
				fail("appCtx should have been published for bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle));
		}
	}

	protected Bundle installBundle(String bundleId) throws Exception {
		// locate bundle
		Resource bundleLocation = locateBundle(bundleId);
		assertTrue("bundle " + bundleId + " could not be found", bundleLocation.exists());

		return bundleContext.installBundle(bundleLocation.getURL().toString());
	}

	protected List getTestPermissions() {
		List list = super.getTestPermissions();
		list.add(new FilePermission("<<ALL FILES>>", "read"));
		list.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		list.add(new AdminPermission("*", AdminPermission.EXECUTE));
		list.add(new AdminPermission("*", AdminPermission.RESOLVE));
		return list;
	}
}
