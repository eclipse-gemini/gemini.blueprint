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

package org.eclipse.gemini.blueprint.service.exporter.support.internal.support;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.eclipse.gemini.blueprint.service.exporter.OsgiServiceRegistrationListener;
import org.eclipse.gemini.blueprint.service.exporter.SimpleOsgiServiceRegistrationListener;
import org.eclipse.gemini.blueprint.service.exporter.support.internal.support.ListenerNotifier;
import org.eclipse.gemini.blueprint.service.exporter.support.internal.support.ServiceRegistrationDecorator;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

public class ServiceRegistrationWrapperTest extends TestCase {

	private ServiceRegistration registration;

	private ServiceRegistration actualRegistration;

	private MockControl mc;

	protected void setUp() throws Exception {
		mc = MockControl.createControl(ServiceRegistration.class);
		actualRegistration = (ServiceRegistration) mc.getMock();

		final ListenerNotifier notifier =
				new ListenerNotifier(
						new OsgiServiceRegistrationListener[] { new SimpleOsgiServiceRegistrationListener() });

		ServiceRegistrationDecorator registrationDecorator = new ServiceRegistrationDecorator(actualRegistration);
		registrationDecorator.setNotifier(new UnregistrationNotifier() {

			public void unregister(Map properties) {
				notifier.callUnregister(null, properties);
			}
		});

		registration = registrationDecorator;

		SimpleOsgiServiceRegistrationListener.REGISTERED = 0;
		SimpleOsgiServiceRegistrationListener.UNREGISTERED = 0;
	}

	protected void tearDown() throws Exception {
		mc.verify();
		registration = null;
	}

	public void testGetReference() {
		ServiceReference reference = new MockServiceReference();
		mc.expectAndReturn(actualRegistration.getReference(), reference);
		mc.replay();

		assertSame(reference, registration.getReference());
	}

	public void testSetProperties() {
		Dictionary props = new Hashtable();
		actualRegistration.setProperties(props);
		mc.replay();

		registration.setProperties(props);
	}

	public void testUnregister() {
		ServiceReference reference = new MockServiceReference();
		mc.expectAndReturn(actualRegistration.getReference(), reference);
		actualRegistration.unregister();
		mc.replay();

		registration.unregister();
	}

	public void testUnregistrationNotified() {
		assertEquals(0, SimpleOsgiServiceRegistrationListener.UNREGISTERED);

		ServiceReference reference = new MockServiceReference();
		mc.expectAndReturn(actualRegistration.getReference(), reference);
		actualRegistration.unregister();
		mc.replay();

		registration.unregister();

		assertEquals(1, SimpleOsgiServiceRegistrationListener.UNREGISTERED);
	}

	public void testExceptionProperlyPropagates() {
		assertEquals(0, SimpleOsgiServiceRegistrationListener.UNREGISTERED);
		IllegalStateException excep = new IllegalStateException();
		mc.expectAndThrow(actualRegistration.getReference(), excep);

		mc.replay();
		try {
			registration.unregister();
		} catch (IllegalStateException ise) {
			assertSame(excep, ise);
		}
		// check listener hasn't been called
		assertEquals(0, SimpleOsgiServiceRegistrationListener.UNREGISTERED);
	}
}