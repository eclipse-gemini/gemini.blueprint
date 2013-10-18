/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.iandt.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;

/**
 * @author Hal Hildebrand Date: Oct 15, 2006 Time: 5:51:36 PM
 */
public class LifecycleTest extends BaseIntegrationTest {

	protected String getManifestLocation() {
		return null;
	}

	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt,lifecycle," + getSpringDMVersion() };
	}

	public void testLifecycle() throws Exception {
		assertNotSame("Guinea pig has already been shutdown", "true",
			System.getProperty("org.eclipse.gemini.blueprint.iandt.lifecycle.GuineaPig.close"));

		assertEquals("Guinea pig didn't startup", "true",
			System.getProperty("org.eclipse.gemini.blueprint.iandt.lifecycle.GuineaPig.startUp"));
		Bundle[] bundles = bundleContext.getBundles();
		Bundle testBundle = null;
		for (int i = 0; i < bundles.length; i++) {
			if ("org.eclipse.gemini.blueprint.iandt.lifecycle".equals(bundles[i].getSymbolicName())) {
				testBundle = bundles[i];
				break;
			}
		}

		assertNotNull("Could not find the test bundle", testBundle);
		StringBuilder filter = new StringBuilder();
		filter.append("(&");
		filter.append("(").append(Constants.OBJECTCLASS).append("=").append(ApplicationContext.class.getName()).append(
			")");
		filter.append("(").append(ConfigurableOsgiBundleApplicationContext.APPLICATION_CONTEXT_SERVICE_PROPERTY_NAME);
		filter.append("=").append(testBundle.getSymbolicName()).append(")");
		filter.append(")");
		ServiceTracker tracker = new ServiceTracker(bundleContext, bundleContext.createFilter(filter.toString()), null);
		try {

			tracker.open();

			AbstractRefreshableApplicationContext appContext = (AbstractRefreshableApplicationContext) tracker.waitForService(30000);
			assertNotNull("test application context", appContext);
			assertTrue("application context is active", appContext.isActive());

			testBundle.stop();
			while (testBundle.getState() == Bundle.STOPPING) {
				Thread.sleep(10);
			}
			assertEquals("Guinea pig didn't shutdown", "true",
				System.getProperty("org.eclipse.gemini.blueprint.iandt.lifecycle.GuineaPig.close"));

			assertFalse("application context is inactive", appContext.isActive());
		}
		finally {
			tracker.close();
		}
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		return perms;
	}
}
