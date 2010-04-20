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

package org.eclipse.gemini.blueprint.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.util.OsgiListenerUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * @author Costin Leau
 * 
 */
public class OsgiListenerUtilsTest extends TestCase {

	private MockBundleContext bundleContext;
	private Map services;
	private ServiceReference ref1, ref2, ref3;
	private Object service1, service2, service3;


	protected void setUp() throws Exception {

		ref1 = new MockServiceReference();
		ref2 = new MockServiceReference();
		ref3 = new MockServiceReference();

		service1 = new Object();
		service2 = new Object();
		service3 = new Object();

		services = new LinkedHashMap();

		services.put(ref1, service1);
		services.put(ref2, service2);
		services.put(ref3, service3);

		bundleContext = new MockBundleContext() {

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[] { ref1, ref2, ref3 };
			}

			public ServiceReference getServiceReference(String clazz) {
				return ref3;
			}

			public Object getService(ServiceReference reference) {
				Object service = services.get(reference);
				if (service == null)
					return super.getService(reference);
				return service;
			}
		};

	}

	protected void tearDown() throws Exception {
		bundleContext = null;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.OsgiListenerUtils#addServiceListener(org.osgi.framework.BundleContext, org.osgi.framework.ServiceListener, java.lang.String)}.
	 */
	public void testAddServiceListenerBundleContextServiceListenerString() {
		final List refs = new ArrayList();

		ServiceListener list = new ServiceListener() {

			public void serviceChanged(ServiceEvent event) {
				if (ServiceEvent.REGISTERED == event.getType())
					refs.add(event.getSource());
			}
		};

		OsgiListenerUtils.addServiceListener(bundleContext, list, (String) null);

		assertFalse(refs.isEmpty());
		assertEquals(3, refs.size());
		assertSame(ref1, refs.get(0));
		assertSame(ref2, refs.get(1));
		assertSame(ref3, refs.get(2));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.OsgiListenerUtils#addSingleServiceListener(org.osgi.framework.BundleContext, org.osgi.framework.ServiceListener, java.lang.String)}.
	 */
	public void testAddSingleServiceListenerBundleContextServiceListenerString() {
		final List refs = new ArrayList();

		ServiceListener listener = new ServiceListener() {

			public void serviceChanged(ServiceEvent event) {
				if (ServiceEvent.REGISTERED == event.getType())
					refs.add(event.getSource());
			}
		};

		OsgiListenerUtils.addSingleServiceListener(bundleContext, listener, (String) null);

		assertFalse(refs.isEmpty());
		assertEquals(1, refs.size());
		assertSame(ref1, refs.get(0));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.OsgiListenerUtils#removeServiceListener(org.osgi.framework.BundleContext, org.osgi.framework.ServiceListener)}.
	 */
	public void testRemoveServiceListenerBundleContextServiceListener() {
		ServiceListener listener = new ServiceListener() {

			public void serviceChanged(ServiceEvent event) {
			}
		};

		OsgiListenerUtils.addSingleServiceListener(bundleContext, listener, (String) null);
		assertEquals(1, bundleContext.getServiceListeners().size());
		OsgiListenerUtils.removeServiceListener(bundleContext, listener);
	}

}
