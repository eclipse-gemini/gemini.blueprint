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

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.gemini.blueprint.service.ServiceUnavailableException;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceDynamicInterceptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockFilter;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * @author Costin Leau
 * 
 */
public class OsgiServiceDynamicInterceptorTest extends TestCase {

	private ServiceDynamicInterceptor interceptor;

	private ServiceReference reference, ref2, ref3;

	private Object service, serv2, serv3;

	private String serv2Filter;

	private String nullFilter;

	private ServiceListener listener;

	private BundleContext ctx;


	protected void setUp() throws Exception {
		service = new Object();
		serv2 = new Object();
		serv3 = new Object();

		reference = new MockServiceReference();
		ref2 = new MockServiceReference();
		ref3 = new MockServiceReference();

		serv2Filter = "serv2";
		nullFilter = "null";

		// special mock context
		// 1. will return no service for the null Filter ("null")
		// 2. will return ref2 for filter serv2Filter

		// the same goes with getService
		ctx = new MockBundleContext() {

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				if (serv2Filter.equals(filter))
					return new ServiceReference[] { ref2 };
				else if (nullFilter.equals(filter)) {
					return null;
				}
				return new ServiceReference[] { reference };
			}

			public Object getService(ServiceReference ref) {
				if (reference == ref) {
					return service;
				}
				if (ref2 == ref) {
					return serv2;
				}

				if (ref3 == ref) {
					return serv3;
				}

				// simulate a non available service
				return null;
			}

			public void addServiceListener(ServiceListener list, String filter) throws InvalidSyntaxException {
				listener = list;
			}
		};

		createInterceptor(null);
	}

	protected void tearDown() throws Exception {
		service = null;
		interceptor = null;
		listener = null;
	}

	private void createInterceptor(Filter filter) {
		interceptor = new ServiceDynamicInterceptor(ctx, null, filter, getClass().getClassLoader());

		interceptor.setMandatoryService(false);

		interceptor.setRetryTimeout(1);
		interceptor.setProxy(new Object());
		interceptor.setServiceImporter(new Object());

		interceptor.afterPropertiesSet();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.service.interceptor.ServiceDynamicInterceptor#OsgiServiceDynamicInterceptor()}.
	 */
	public void testOsgiServiceDynamicInterceptor() {
		assertNotNull(interceptor.getRetryTemplate());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.service.interceptor.ServiceDynamicInterceptor#lookupService()}.
	 */
	public void testLookupService() throws Throwable {
		Object serv = interceptor.getTarget();
		assertSame(service, serv);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.service.interceptor.ServiceDynamicInterceptor#doInvoke(java.lang.Object, org.aopalliance.intercept.MethodInvocation)}.
	 */
	public void testDoInvoke() throws Throwable {
		Object target = new Object();
		Method m = target.getClass().getDeclaredMethod("hashCode", null);

		MethodInvocation invocation = new MockMethodInvocation(m);
		assertEquals(new Integer(service.hashCode()), interceptor.invoke(invocation));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.service.interceptor.ServiceDynamicInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)}.
	 */
	public void testInvocationWhenServiceNA() throws Throwable {
		// service n/a

		Object target = new Object();
		Method m = target.getClass().getDeclaredMethod("hashCode", null);

		MethodInvocation invocation = new MockMethodInvocation(m);
		ServiceReference oldRef = reference;
		reference = null;

		try {
			interceptor.invoke(invocation);
			fail("should have thrown exception");
		}
		catch (ServiceUnavailableException ex) {
			// expected
		}

		// service is up
		reference = oldRef;

		assertEquals(new Integer(service.hashCode()), interceptor.invoke(invocation));
	}

	public void testInvocationTimeoutWhenServiceNA() throws Throwable {
		// service n/a

		Object target = new Object();
		Method m = target.getClass().getDeclaredMethod("hashCode", null);

		MethodInvocation invocation = new MockMethodInvocation(m);
		createInterceptor(new MockFilter(nullFilter));
		ServiceEvent event = new ServiceEvent(ServiceEvent.UNREGISTERING, reference);
		listener.serviceChanged(event);
		interceptor.getRetryTemplate().reset(3000);
		long now = System.currentTimeMillis();
		try {
			interceptor.invoke(invocation);
			fail("should have thrown exception");
		}
		catch (ServiceUnavailableException ex) {
			// expected
		}

		// service is up
		interceptor.getRetryTemplate().reset(1);

		assertTrue("Call did not block for 3000ms, actually blocked for " + (System.currentTimeMillis() - now) + "ms",
			(System.currentTimeMillis() - now) >= 3000);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.service.interceptor.ServiceDynamicInterceptor#getTarget()}.
	 */
	public void testGetTarget() throws Throwable {
		// add service
		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, reference);
		listener.serviceChanged(event);

		Object target = interceptor.getTarget();
		assertSame("target not properly discovered", service, target);
	}

	public void testGetTargetWhenMultipleServicesAreAvailable() throws Throwable {
		// add service
		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, reference);
		listener.serviceChanged(event);

		event = new ServiceEvent(ServiceEvent.REGISTERED, ref2);
		listener.serviceChanged(event);

		Object target = interceptor.getTarget();
		assertSame("target not properly discovered", service, target);

		createInterceptor(new MockFilter(serv2Filter));
		event = new ServiceEvent(ServiceEvent.UNREGISTERING, reference);
		listener.serviceChanged(event);

		try {
			target = interceptor.getTarget();
		}
		catch (ServiceUnavailableException sue) {
			fail("target not rebound after service is down");
		}

		assertSame("wrong service rebound", serv2, target);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.service.interceptor.ServiceDynamicInterceptor#afterPropertiesSet()}.
	 */
	public void testAfterPropertiesSet() {
		assertNotNull("should have initialized listener", listener);
	}

	/**
	 * HSH - Mandatory cardinality is enforced by the extender in the wait-for
	 * semantic regarding dependent services of cardinality {1..}
	 * 
	 * public void testMandatoryCardinality() { MockBundleContext ctx = new
	 * MockBundleContext() { public ServiceReference[]
	 * getServiceReferences(String clazz, String filter) throws
	 * InvalidSyntaxException { return null; } }; interceptor = new
	 * OsgiServiceDynamicInterceptor(ctx, ImportContextClassLoader.UNMANAGED);
	 * interceptor.setFilter(new MockFilter()); RetryTemplate template = new
	 * RetryTemplate(); template.setRetryNumbers(1); template.setWaitTime(10);
	 * interceptor.setRetryTemplate(template); try {
	 * interceptor.afterPropertiesSet(); fail("expected exception"); } catch
	 * (ServiceUnavailableException sue) { // expected } }
	 */
}
