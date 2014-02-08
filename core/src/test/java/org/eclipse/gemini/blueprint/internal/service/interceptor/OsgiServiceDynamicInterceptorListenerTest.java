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

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.eclipse.gemini.blueprint.service.importer.ServiceReferenceProxy;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceDynamicInterceptor;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * Test for the listener rebinding behavior. Makes sure the bind/unbind contract is properly respected.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceDynamicInterceptorListenerTest extends TestCase {

	private ServiceDynamicInterceptor interceptor;

	private OsgiServiceLifecycleListener listener;

	private MockBundleContext bundleContext;

	private ServiceReference[] refs;

	protected void setUp() throws Exception {
		listener = new SimpleTargetSourceLifecycleListener();

		refs = new ServiceReference[] { new MockServiceReference() };

		bundleContext = new MockBundleContext() {

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return refs;
			}
		};

		interceptor = new ServiceDynamicInterceptor(bundleContext, null, null, getClass().getClassLoader());
		interceptor.setListeners(new OsgiServiceLifecycleListener[] { listener });
		interceptor.setMandatoryService(false);
		interceptor.setProxy(new Object());
		interceptor.setServiceImporter(new Object());
		interceptor.setSticky(false);

		interceptor.setRetryTimeout(1);

		SimpleTargetSourceLifecycleListener.BIND = 0;
		SimpleTargetSourceLifecycleListener.UNBIND = 0;
	}

	protected void tearDown() throws Exception {
		interceptor = null;
		listener = null;
		bundleContext = null;
	}

	public void testBind() {
		assertEquals(0, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);

		interceptor.afterPropertiesSet();

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);
	}

	public void testUnbind() {
		interceptor.afterPropertiesSet();

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);

		ServiceListener sl = (ServiceListener) bundleContext.getServiceListeners().iterator().next();

		// save old ref and invalidate it so new services are not found
		ServiceReference oldRef = refs[0];
		refs = null;

		sl.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, oldRef));

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(1, SimpleTargetSourceLifecycleListener.UNBIND);
	}

	public void testRebindWhenNewServiceAppears() {
		interceptor.afterPropertiesSet();

		ServiceListener sl = (ServiceListener) bundleContext.getServiceListeners().iterator().next();

		Dictionary props = new Hashtable();
		// increase service ranking
		props.put(Constants.SERVICE_RANKING, 10);

		ServiceReference ref = new MockServiceReference(null, props, null);

		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, ref);

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);

		sl.serviceChanged(event);

		assertEquals(2, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);
	}

	public void testRebindWhenServiceGoesDownButAReplacementIsFound() {
		interceptor.afterPropertiesSet();

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);

		ServiceListener sl = (ServiceListener) bundleContext.getServiceListeners().iterator().next();

		// unregister the old service
		sl.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, refs[0]));

		// a new one is found since the mock context will return one again
		assertEquals(2, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);
	}

	public void testStickinessWhenABetterServiceIsAvailable() throws Exception {
		interceptor.setSticky(true);
		interceptor.afterPropertiesSet();

		ServiceListener sl = (ServiceListener) bundleContext.getServiceListeners().iterator().next();

		Dictionary props = new Hashtable();
		// increase service ranking
		props.put(Constants.SERVICE_RANKING, 10);

		ServiceReference ref = new MockServiceReference(null, props, null);
		ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, ref);

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);

		sl.serviceChanged(event);

		assertEquals("the proxy is not sticky", 1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);
	}

	public void testStickinessWhenServiceGoesDown() throws Exception {
		interceptor.setSticky(true);
		interceptor.afterPropertiesSet();

		ServiceListener sl = (ServiceListener) bundleContext.getServiceListeners().iterator().next();

		Dictionary props = new Hashtable();
		// increase service ranking
		props.put(Constants.SERVICE_RANKING, 10);

		ServiceReference higherRankingRef = new MockServiceReference(null, props, null);
		refs = new ServiceReference[] { new MockServiceReference(), higherRankingRef };

		assertTrue(Arrays.equals(bundleContext.getServiceReferences((String)null, null), refs));

		assertEquals(1, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);

		sl.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, refs[0]));

		assertEquals(2, SimpleTargetSourceLifecycleListener.BIND);
		assertEquals(0, SimpleTargetSourceLifecycleListener.UNBIND);

		assertSame("incorrect backing reference selected", higherRankingRef, ((ServiceReferenceProxy) interceptor
				.getServiceReference()).getTargetServiceReference());
	}
}