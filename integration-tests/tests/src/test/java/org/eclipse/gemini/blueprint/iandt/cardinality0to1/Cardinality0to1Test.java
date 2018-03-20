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

package org.eclipse.gemini.blueprint.iandt.cardinality0to1;

import java.io.FilePermission;
import java.util.List;
import java.util.PropertyPermission;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.eclipse.gemini.blueprint.iandt.cardinality0to1.test.MyListener;
import org.eclipse.gemini.blueprint.iandt.cardinality0to1.test.ReferenceContainer;
import org.eclipse.gemini.blueprint.service.ServiceUnavailableException;

/**
 * @author Hal Hildebrand Date: Dec 6, 2006 Time: 6:04:42 PM
 */
public class Cardinality0to1Test extends BaseIntegrationTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt, simple.service," + getGeminiBlueprintVersion(),
			"org.eclipse.gemini.blueprint.iandt, cardinality0to1," + getGeminiBlueprintVersion() };
	}

	public void test0to1Cardinality() throws Exception {
		Bundle simpleService2Bundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.eclipse.gemini.blueprint.iandt", "simple.service2", getGeminiBlueprintVersion()).getURL().toExternalForm());

		assertNotNull("Cannot find the simple service 2 bundle", simpleService2Bundle);

		assertNotSame("simple service 2 bundle is in the activated state!", new Integer(Bundle.ACTIVE), new Integer(
			simpleService2Bundle.getState()));

		assertEquals("Unxpected initial binding of service", 0, MyListener.BOUND_COUNT);
		assertEquals("Unexpected initial unbinding of service", 1, MyListener.UNBOUND_COUNT);
		assertNotNull("Service reference should be not null", ReferenceContainer.service);

		try {
			ReferenceContainer.service.stringValue();
			fail("Service should be unavailable");
		}
		catch (ServiceUnavailableException e) {
			// expected
		}

		startDependency(simpleService2Bundle);

		assertEquals("Expected initial binding of service", 1, MyListener.BOUND_COUNT);
		assertEquals("Unexpected initial unbinding of service", 1, MyListener.UNBOUND_COUNT);
		assertNotNull("Service reference should be not null", ReferenceContainer.service);

		assertNotNull(ReferenceContainer.service.stringValue());

	}

	private void startDependency(Bundle simpleService2Bundle) throws BundleException, InterruptedException {
		System.out.println("Starting dependency");
		simpleService2Bundle.start();

		waitOnContextCreation("org.eclipse.gemini.blueprint.iandt.simpleservice2");

		System.out.println("Dependency started");
	}

	protected List getIAndTPermissions() {
		List perms = super.getIAndTPermissions();
		// export package
		perms.add(new PropertyPermission("*", "read"));
		perms.add(new PropertyPermission("*", "write"));
		return perms;
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		perms.add(new FilePermission("<<ALL FILES>>", "read"));
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		perms.add(new AdminPermission("*", AdminPermission.LIFECYCLE));
		perms.add(new AdminPermission("*", AdminPermission.RESOLVE));
		perms.add(new PropertyPermission("*", "read"));
		perms.add(new PropertyPermission("*", "write"));
		return perms;
	}
}
