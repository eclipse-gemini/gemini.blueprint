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

package org.eclipse.gemini.blueprint.iandt.referenceProxy;

import java.util.List;
import java.util.PropertyPermission;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.eclipse.gemini.blueprint.iandt.reference.proxy.ServiceReferer;
import org.eclipse.gemini.blueprint.iandt.simpleservice.MyService;
import org.eclipse.gemini.blueprint.service.ServiceUnavailableException;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Hal Hildebrand Date: Nov 25, 2006 Time: 12:42:30 PM
 */
public class ReferenceProxyTest extends BaseIntegrationTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt, simple.service," + getSpringDMVersion(),
			"org.eclipse.gemini.blueprint.iandt, reference.proxy," + getSpringDMVersion() };
	}

	public void testReferenceProxyLifecycle() throws Exception {

		MyService reference = ServiceReferer.serviceReference;

		assertNotNull("reference not initialized", reference);
		assertNotNull("no value specified in the reference", reference.stringValue());

		Bundle simpleServiceBundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext,
			"org.eclipse.gemini.blueprint.iandt.simpleservice");

		assertNotNull("Cannot find the simple service bundle", simpleServiceBundle);
		System.out.println("stopping bundle");
		simpleServiceBundle.stop();

		while (simpleServiceBundle.getState() == Bundle.STOPPING) {
			System.out.println("waiting for bundle to stop");
			Thread.sleep(10);
		}
		System.out.println("bundle stopped");

		// Service should be unavailable
		try {
			reference.stringValue();
			fail("ServiceUnavailableException should have been thrown!");
		}
		catch (ServiceUnavailableException e) {
			// Expected
		}

		System.out.println("starting bundle");
		simpleServiceBundle.start();

		waitOnContextCreation("org.eclipse.gemini.blueprint.iandt.simpleservice");

		System.out.println("bundle started");
		// Service should be running
		assertNotNull(reference.stringValue());
	}

	protected long getDefaultWaitTime() {
		return 15L;
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		return perms;
	}

	protected List getIAndTPermissions() {
		List perms = super.getIAndTPermissions();
		perms.add(new PropertyPermission("*", "read"));
		perms.add(new PropertyPermission("*", "write"));
		return perms;
	}

}
