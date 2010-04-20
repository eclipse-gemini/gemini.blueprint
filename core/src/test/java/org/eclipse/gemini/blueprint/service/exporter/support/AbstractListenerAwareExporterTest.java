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

package org.eclipse.gemini.blueprint.service.exporter.support;

import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.service.exporter.OsgiServiceRegistrationListener;
import org.eclipse.gemini.blueprint.service.exporter.SimpleOsgiServiceRegistrationListener;
import org.eclipse.gemini.blueprint.service.exporter.support.internal.support.ListenerNotifier;

public class AbstractListenerAwareExporterTest extends TestCase {

	private AbstractOsgiServiceExporter exporter;

	protected void setUp() throws Exception {
		SimpleOsgiServiceRegistrationListener.REGISTERED = 0;
		SimpleOsgiServiceRegistrationListener.UNREGISTERED = 0;

		exporter = new AbstractOsgiServiceExporter() {

			protected void registerService() {
			}

			protected void unregisterService() {
			}

		};
		exporter.setListeners(new OsgiServiceRegistrationListener[] { new SimpleOsgiServiceRegistrationListener() });
	}

	protected void tearDown() throws Exception {
		exporter = null;
	}

	public void testNotifyListenersOnRegistration() {
		assertEquals(0, SimpleOsgiServiceRegistrationListener.REGISTERED);
		assertEquals(0, SimpleOsgiServiceRegistrationListener.UNREGISTERED);

		ListenerNotifier notifier = exporter.getNotifier();
		notifier.callRegister(new Object(), new HashMap());

		assertEquals(1, SimpleOsgiServiceRegistrationListener.REGISTERED);
		assertEquals(0, SimpleOsgiServiceRegistrationListener.UNREGISTERED);
	}

	public void testNotifyListenersOnUnregistration() {
		assertEquals(0, SimpleOsgiServiceRegistrationListener.REGISTERED);
		assertEquals(0, SimpleOsgiServiceRegistrationListener.UNREGISTERED);
		ListenerNotifier notifier = exporter.getNotifier();
		notifier.callUnregister(new Object(), new HashMap());

		assertEquals(0, SimpleOsgiServiceRegistrationListener.REGISTERED);
		assertEquals(1, SimpleOsgiServiceRegistrationListener.UNREGISTERED);
	}

}
