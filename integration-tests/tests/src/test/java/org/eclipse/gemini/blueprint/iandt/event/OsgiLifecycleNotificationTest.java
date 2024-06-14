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

package org.eclipse.gemini.blueprint.iandt.event;

import static org.junit.Assert.assertTrue;

import java.io.FilePermission;
import java.util.List;

import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextFailedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextRefreshedEvent;
import org.junit.Test;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.springframework.core.io.Resource;

/**
 * Integration test for the appCtx notification mechanism.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiLifecycleNotificationTest extends AbstractEventTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt, extender.listener.bundle," + getSpringDMVersion() };
	}

	protected void onSetUp() throws Exception {
		super.onSetUp();

	}

	@Test
	public void testEventsForCtxThatWork() throws Exception {

		listener = new OsgiBundleApplicationContextListener() {

			public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
				if (event instanceof OsgiBundleContextRefreshedEvent) {
					eventList.add(event);
					synchronized (lock) {
						lock.notify();
					}
				}
			}
		};

		registerEventListener();

		assertTrue("should start with an empty list", eventList.isEmpty());
		// install a simple osgi bundle and check the list of events

		Resource bundle = getLocator().locateArtifact("org.eclipse.gemini.blueprint.iandt", "simple.service",
			getSpringDMVersion());

		Bundle bnd = bundleContext.installBundle(bundle.getURL().toExternalForm());
		try {

			bnd.start();

			assertTrue("no event received", waitForEvent(TIME_OUT));
			System.out.println("events received " + eventList);
		}
		finally {
			bnd.uninstall();
		}
	}

	@Test
	public void testEventsForCtxThatFail() throws Exception {

		listener = new OsgiBundleApplicationContextListener() {

			public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
				if (event instanceof OsgiBundleContextFailedEvent) {
					eventList.add(event);
					synchronized (lock) {
						lock.notify();
					}
				}
			}
		};

		registerEventListener();

		assertTrue("should start with an empty list", eventList.isEmpty());
		// install a simple osgi bundle and check the list of events

		Resource bundle = getLocator().locateArtifact("org.eclipse.gemini.blueprint.iandt", "error", getSpringDMVersion());

		Bundle bnd = bundleContext.installBundle(bundle.getURL().toExternalForm());

		try {
			bnd.start();

			assertTrue("event not received", waitForEvent(TIME_OUT));
		}
		finally {
			bnd.uninstall();
		}
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
