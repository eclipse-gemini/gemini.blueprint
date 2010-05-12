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

package org.eclipse.gemini.blueprint.iandt.deadlocks;

import java.io.FilePermission;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.io.Resource;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;

/**
 * @author Hal Hildebrand Date: Jun 5, 2007 Time: 9:10:11 PM
 */
public class DeadlockHandlingTest extends BaseIntegrationTest {

	// Specifically do not wait
	protected boolean shouldWaitForSpringBundlesContextCreation() {
		return false;
	}

	/**
	 * While it may appear that this test is doing nothing, what it is doing is
	 * testing what happens when the OSGi framework is shutdown while the
	 * Spring/OSGi extender is deadlocked. If all goes well, the test will
	 * gracefully end. If not, it will hang for quite a while.
	 */
	public void testErrorHandling() throws Exception {
		Resource errorResource = getLocator().locateArtifact("org.eclipse.gemini.blueprint.iandt", "deadlock",
			getSpringDMVersion());
		assertNotNull("bundle resource exists", errorResource);
		Bundle errorBundle = bundleContext.installBundle(errorResource.getURL().toExternalForm());
		assertNotNull("bundle exists", errorBundle);
		errorBundle.start();
		StringBuilder filter = new StringBuilder();

		filter.append("(&");
		filter.append("(").append(Constants.OBJECTCLASS).append("=").append(
			AbstractRefreshableApplicationContext.class.getName()).append(")");
		filter.append("(").append(ConfigurableOsgiBundleApplicationContext.APPLICATION_CONTEXT_SERVICE_PROPERTY_NAME);
		filter.append("=").append("org.eclipse.gemini.blueprint.iandt.deadlock").append(")");
		filter.append(")");
		ServiceTracker tracker = new ServiceTracker(bundleContext, bundleContext.createFilter(filter.toString()), null);

		try {
			tracker.open();

			AbstractRefreshableApplicationContext appContext = (AbstractRefreshableApplicationContext) tracker.waitForService(3000);
			assertNull("Deadlock context should not be published", appContext);
		}
		finally {
			tracker.close();
		}
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