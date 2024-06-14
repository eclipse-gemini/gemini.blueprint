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

package org.eclipse.gemini.blueprint.iandt.proxycreator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.junit.Test;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;

/**
 * Integration test that checks that a new classloader is created when the
 * bundle is refreshed. The test updates a bundle that internally creates JDK
 * and CGLIB proxies which, will fail in case the old CL is preserved.
 * 
 * @author Costin Leau
 * 
 */
public class ProxyCreatorTest extends BaseIntegrationTest {

	private static final String PROXY_CREATOR_SYM_NAME = "org.eclipse.gemini.blueprint.iandt.proxy.creator";

	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt,proxy.creator," + getSpringDMVersion()};
	}

	@Test
	public void testNewProxiesCreatedOnBundleRefresh() throws Exception {
		// get a hold of the bundle proxy creator bundle and update it
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, PROXY_CREATOR_SYM_NAME);

		assertNotNull("proxy creator bundle not found", bundle);
		// update bundle (and thus create a new version of the classes)
		bundle.update();

		// make sure it starts-up
		try {
			waitOnContextCreation(PROXY_CREATOR_SYM_NAME, 60);
		}
		catch (Exception ex) {
			fail("updating the bundle failed");
		}
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		perms.add(new AdminPermission("*", AdminPermission.RESOLVE));
		return perms;
	}
}
