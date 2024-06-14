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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.Bundle;
import org.springframework.core.io.Resource;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.extender.event.BootstrappingDependenciesEvent;
import org.eclipse.gemini.blueprint.extender.event.BootstrappingDependencyEvent;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceDependency;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitEndedEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitStartingEvent;
import org.junit.Test;

/**
 * @author Costin Leau
 * 
 */
public abstract class DepedencyEventTest extends AbstractEventTest {

	private List refreshEvents = Collections.synchronizedList(new ArrayList(10));
	private List<BootstrappingDependenciesEvent> graceEvents =
			Collections.synchronizedList(new ArrayList<BootstrappingDependenciesEvent>(10));

	protected void onSetUp() throws Exception {
		refreshEvents.clear();

		// override the listener with another implementation that waits until the appCtx are fully started
		listener = new OsgiBundleApplicationContextListener() {

			public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
				System.out.println("receiving event " + event.getClass());
				if (event instanceof BootstrappingDependencyEvent) {
					eventList.add(event);
				}
				if (event instanceof BootstrappingDependenciesEvent) {
					graceEvents.add((BootstrappingDependenciesEvent) event);
				} else {
					refreshEvents.add(event);
				}
				synchronized (lock) {
					lock.notify();
				}
			}
		};
	}

	@Test
	public void testEventsForCtxThatWork() throws Exception {
		// publish listener
		registerEventListener();

		assertTrue("should start with an empty list", eventList.isEmpty());

		// install the dependency bundle
		Resource bundle =
				getLocator().locateArtifact("org.eclipse.gemini.blueprint.iandt", "dependencies", getSpringDMVersion());

		Resource dependency1 =
				getLocator().locateArtifact("org.eclipse.gemini.blueprint.iandt", "simple.service", getSpringDMVersion());

		Resource dependency2 =
				getLocator().locateArtifact("org.eclipse.gemini.blueprint.iandt", "simple.service2", getSpringDMVersion());

		Resource dependency3 =
				getLocator().locateArtifact("org.eclipse.gemini.blueprint.iandt", "simple.service3", getSpringDMVersion());

		Bundle bnd = bundleContext.installBundle(bundle.getURL().toExternalForm());

		// install the bundles but don't start them
		Bundle bnd1 = bundleContext.installBundle(dependency1.getURL().toExternalForm());
		Bundle bnd2 = bundleContext.installBundle(dependency2.getURL().toExternalForm());
		Bundle bnd3 = bundleContext.installBundle(dependency3.getURL().toExternalForm());

		try {

			bnd.start();

			// expect at least 3 events
			while (eventList.size() < 3) {
				if (!waitForEvent(TIME_OUT)) {
					fail("not enough events received after " + TIME_OUT + " ms");
				}
			}

			// check the event type and their name (plus the order)

			// simple service 3
			assertEquals("&simpleService3", getDependencyAt(0).getBeanName());
			assertEquals(OsgiServiceDependencyWaitStartingEvent.class, getNestedEventAt(0).getClass());
			// simple service 2
			assertEquals("&simpleService2", getDependencyAt(1).getBeanName());
			assertEquals(OsgiServiceDependencyWaitStartingEvent.class, getNestedEventAt(0).getClass());
			// simple service 1
			assertEquals("&nested", getDependencyAt(2).getBeanName());
			assertEquals(OsgiServiceDependencyWaitStartingEvent.class, getNestedEventAt(0).getClass());

			waitForContextStartEvent(bnd1);
			assertEquals("&nested", getDependencyAt(3).getBeanName());
			assertEquals(OsgiServiceDependencyWaitEndedEvent.class, getNestedEventAt(3).getClass());

			waitForContextStartEvent(bnd3);
			assertEquals("&simpleService3", getDependencyAt(4).getBeanName());
			assertEquals(OsgiServiceDependencyWaitEndedEvent.class, getNestedEventAt(4).getClass());
			// bnd3 context started event

			waitForContextStartEvent(bnd2);
			assertEquals("&simpleService2", getDependencyAt(5).getBeanName());
			assertEquals(OsgiServiceDependencyWaitEndedEvent.class, getNestedEventAt(5).getClass());
			// bnd2 context started event
			// wait until the bundle fully starts
			waitOnContextCreation("org.eclipse.gemini.blueprint.iandt.dependencies");
			// double check context started event

			// bnd1 context started event
			System.out.println("Refresh events received are " + refreshEvents);

			while (eventList.size() < 3) {
				if (!waitForEvent(TIME_OUT)) {
					fail("not enough events received after " + TIME_OUT + " ms");
				}
			}
			// at least 3 events have to be received
			assertTrue(refreshEvents.size() >= 3);

			for (BootstrappingDependenciesEvent event : graceEvents) {
				System.out.println(event.getDependenciesAsFilter());
			}

		} finally {
			bnd.uninstall();

			bnd1.uninstall();
			bnd2.uninstall();
			bnd3.uninstall();
		}
	}

	private OsgiServiceDependency getDependencyAt(int index) {
		return getNestedEventAt(index).getServiceDependency();
	}

	private OsgiServiceDependencyEvent getNestedEventAt(int index) {
		Object obj = eventList.get(index);
		System.out.println("received object " + obj.getClass() + "|" + obj);
		BootstrappingDependencyEvent event = (BootstrappingDependencyEvent) obj;
		return event.getDependencyEvent();
	}

	private void waitForContextStartEvent(Bundle bundle) throws Exception {
		int eventNumber = eventList.size();
		bundle.start();
		waitOnContextCreation(bundle.getSymbolicName());
		while (eventList.size() < eventNumber + 1)
			waitForEvent(TIME_OUT);
	}
}
