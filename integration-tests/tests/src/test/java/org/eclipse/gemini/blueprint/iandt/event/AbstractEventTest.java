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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.ServiceRegistration;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;

/**
 * @author Costin Leau
 * 
 */
public abstract class AbstractEventTest extends BaseIntegrationTest {

	protected OsgiBundleApplicationContextListener listener;

	private ServiceRegistration registration;
	/** list of events */
	protected List eventList = Collections.synchronizedList(new ArrayList());;
	/** lock */
	protected final Object lock = new Object();

	/** wait X minutes max */
	protected final long TIME_OUT = 3* 60 * 1000;


	protected void onSetUp() throws Exception {
		eventList.clear();

		listener = new OsgiBundleApplicationContextListener() {

			public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
				eventList.add(event);
				synchronized (lock) {
					lock.notify();
				}
			}
		};
	}

	protected void onTearDown() throws Exception {
		OsgiServiceUtils.unregisterService(registration);
		eventList.clear();
	}

	protected void registerEventListener() {
		// publish listener
		registration = bundleContext.registerService(
			new String[] { OsgiBundleApplicationContextListener.class.getName() }, listener, null);
	}

	/**
	 * Returns true if the wait ended through a notification, false otherwise.
	 * 
	 * @param maxWait
	 * @return
	 * @throws Exception
	 */
	protected boolean waitForEvent(long maxWait) {
		long start = System.currentTimeMillis();
		synchronized (lock) {
			try {
				lock.wait(maxWait);
			}
			catch (Exception ex) {
				return false;
			}
		}
		long stop = System.currentTimeMillis();
		boolean waitSuccessed = (stop - start <= maxWait);
		return waitSuccessed;
	}
}
