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

package org.eclipse.gemini.blueprint.iandt.serviceListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.iandt.service.listener.MyListener;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.junit.Test;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;

/**
 * @author Hal Hildebrand Date: Nov 14, 2006 Time: 8:18:15 AM
 */
public class ServiceListenerTest extends BaseIntegrationTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt,simple.service," + getSpringDMVersion(),
			"org.eclipse.gemini.blueprint.iandt, service.listener," + getSpringDMVersion() };
	}

	@Test
	public void testServiceListener() throws Exception {
		assertEquals("Expected initial binding of service", 1, MyListener.BOUND_COUNT);
		assertEquals("Unexpected initial unbinding of service", 0, MyListener.UNBOUND_COUNT);

		Bundle simpleServiceBundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext,
			"org.eclipse.gemini.blueprint.iandt.simpleservice");

		assertNotNull("Cannot find the simple service bundle", simpleServiceBundle);

		simpleServiceBundle.stop();
		while (simpleServiceBundle.getState() == Bundle.STOPPING) {
			Thread.sleep(100);
		}

		assertEquals("Expected one binding of service", 1, MyListener.BOUND_COUNT);
		assertTrue("Expected only one unbinding of service", MyListener.UNBOUND_COUNT < 2);
		assertEquals("Expected unbinding of service not seen", 1, MyListener.UNBOUND_COUNT);

		logger.debug("about to restart simple service");
		simpleServiceBundle.start();
		waitOnContextCreation("org.eclipse.gemini.blueprint.iandt.simpleservice");
		// wait some more to let the listener binding propagate
		Thread.sleep(1000);

		logger.debug("simple service succesfully restarted");
		assertTrue("Expected only two bindings of service", MyListener.BOUND_COUNT < 3);
		assertEquals("Expected binding of service not seen", 2, MyListener.BOUND_COUNT);
		assertEquals("Unexpected unbinding of service", 1, MyListener.UNBOUND_COUNT);
	}

	protected long getDefaultWaitTime() {
		return 10L;
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		return perms;
	}
}
