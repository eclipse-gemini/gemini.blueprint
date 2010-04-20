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

package org.eclipse.gemini.blueprint.internal.service.interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceDynamicInterceptor;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

public class OsgiServiceDynamicInterceptorSyntheticEventsTest extends TestCase {

	private ServiceDynamicInterceptor interceptor;

	private MockBundleContext bundleContext;

	private OsgiServiceLifecycleListener listener;

	private ServiceReference ref1, ref2, ref3;

	private Object service1, service2, service3;

	private List bindServices, unbindServices;

	private Object serviceProxy = new Object();


	protected void setUp() throws Exception {

		// generate services references in reverse order to have them increasing service ids
		ref3 = new MockServiceReference();
		ref2 = new MockServiceReference();
		ref1 = new MockServiceReference();

		service1 = "service 1";
		service2 = "service 2";
		service3 = "service 3";

		final Map services = new HashMap();
		services.put(ref1, service1);
		services.put(ref2, service2);
		services.put(ref3, service3);

		bindServices = new ArrayList();
		unbindServices = new ArrayList();

		bundleContext = new MockBundleContext() {

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[] { ref1, ref2, ref3 };
			}

			public ServiceReference getServiceReference(String clazz) {
				return ref3;
			}

			public Object getService(ServiceReference reference) {
				return services.get(reference);
			}
		};

		bundleContext.getBundle();

		listener = new OsgiServiceLifecycleListener() {

			public void bind(Object service, Map properties) throws Exception {
				bindServices.add(service);
			}

			public void unbind(Object service, Map properties) throws Exception {
				unbindServices.add(service);
			}

		};

		interceptor = new ServiceDynamicInterceptor(bundleContext, null, null, getClass().getClassLoader());
		interceptor.setMandatoryService(false);
		interceptor.setProxy(serviceProxy);
		interceptor.setListeners(new OsgiServiceLifecycleListener[] { listener });
		interceptor.setServiceImporter(new Object());

		interceptor.setRetryTimeout(1);
	}

	protected void tearDown() throws Exception {
		interceptor = null;
		bundleContext = null;
		listener = null;
	}

	public void testGetServices() throws Exception {
		assertSame(ref3, OsgiServiceReferenceUtils.getServiceReference(bundleContext, (String) null));
	}

	public void testOnlyOneSyntheticEventOnRegistrationIfMultipleServicesPresent() throws Exception {
		interceptor.afterPropertiesSet();
		assertEquals(1, bindServices.size());
		assertEquals(0, unbindServices.size());
		assertSame(serviceProxy, bindServices.get(0));
	}

	public void testOnlyOneSyntheticEventOnUnregistrationIfMultipleServicesPresent() throws Exception {
		interceptor.afterPropertiesSet();
		interceptor.destroy();
		assertEquals(1, bindServices.size());
		assertEquals(1, unbindServices.size());
		assertSame(serviceProxy, unbindServices.get(0));
	}
}
