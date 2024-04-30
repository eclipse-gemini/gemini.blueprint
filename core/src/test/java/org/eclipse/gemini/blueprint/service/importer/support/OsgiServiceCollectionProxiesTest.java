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

package org.eclipse.gemini.blueprint.service.importer.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.service.importer.ServiceReferenceProxy;
import org.eclipse.gemini.blueprint.service.importer.support.ImportContextClassLoaderEnum;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceProxyCreator;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceCollection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * Unit test for the static proxies returned by Osgi collection.
 * 
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceCollectionProxiesTest {

	private OsgiServiceCollection col;

	private Map services;

	private String[] classInterfaces = new String[] { Cloneable.class.getName() };

	private ServiceProxyCreator proxyCreator;

	@Before
	public void setup() throws Exception {
		services = new LinkedHashMap();

		BundleContext ctx = new MockBundleContext() {

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[0];
			}

			public Object getService(ServiceReference reference) {
				Object service = services.get(reference);
				return (service == null ? new Object() : service);
			}

		};

		ClassLoader cl = getClass().getClassLoader();
		proxyCreator =
				new StaticServiceProxyCreator(new Class<?>[] { Cloneable.class }, cl, cl, ctx,
						ImportContextClassLoaderEnum.UNMANAGED, false, false);
	}

	@After
	public void tearDown() throws Exception {
		col = null;
	}

	@Test
	public void testHashCodeBetweenProxyAndTarget() {
		Date date = new Date(123);

		ServiceReference ref = new MockServiceReference(classInterfaces);
		services.put(ref, date);

		Object proxy = proxyCreator.createServiceProxy(ref).proxy;

		assertFalse("proxy and service should have different hashcodes", date.hashCode() == proxy.hashCode());

	}

	@Test
	public void testHashCodeBetweenProxies() {
		Date date = new Date(123);

		ServiceReference ref = new MockServiceReference(classInterfaces);
		services.put(ref, date);

		Object proxy = proxyCreator.createServiceProxy(ref).proxy;
		Object proxy2 = proxyCreator.createServiceProxy(ref).proxy;
		assertEquals("proxies for the same service should have the same hashcode", proxy.hashCode(), proxy2.hashCode());
	}

	@Test
	public void testEqualsBetweenProxyAndTarget() {
		Date date = new Date(123);

		ServiceReference ref = new MockServiceReference(classInterfaces);
		services.put(ref, date);

		Object proxy = proxyCreator.createServiceProxy(ref).proxy;

		assertFalse("proxy and service should not be equal", date.equals(proxy));
	}

	@Test
	public void testEqualsBetweenProxies() {
		Date date = new Date(123);

		ServiceReference ref = new MockServiceReference(classInterfaces);
		services.put(ref, date);

		Object proxy = proxyCreator.createServiceProxy(ref).proxy;
		Object proxy2 = proxyCreator.createServiceProxy(ref).proxy;
		assertEquals("proxies for the same target should be equal", proxy, proxy2);
	}

	@Test
	public void testHashCodeBetweenProxyAndItself() {
		Date date = new Date(123);

		ServiceReference ref = new MockServiceReference(classInterfaces);
		services.put(ref, date);

		Object proxy = proxyCreator.createServiceProxy(ref).proxy;

		assertEquals("proxy should consistent hashcode", proxy.hashCode(), proxy.hashCode());
	}

	@Test
	public void testEqualsBetweenProxyAndItself() {
		Date date = new Date(123);

		ServiceReference ref = new MockServiceReference(classInterfaces);
		services.put(ref, date);

		Object proxy = proxyCreator.createServiceProxy(ref).proxy;
		assertEquals("proxy should be equal to itself", proxy, proxy);
	}

	@Test
	public void testServiceReferenceProxy() throws Exception {
		Date date = new Date(123);
		ServiceReference ref = new MockServiceReference(classInterfaces);
		services.put(ref, date);

		Object proxy = proxyCreator.createServiceProxy(ref).proxy;
		assertTrue(proxy instanceof ImportedOsgiServiceProxy);
		ServiceReferenceProxy referenceProxy = ((ImportedOsgiServiceProxy) proxy).getServiceReference();
		assertNotNull(referenceProxy);
		assertSame(ref, referenceProxy.getTargetServiceReference());
	}

	@Test
	public void testServiceReferenceProxyEquality() throws Exception {

		Date date = new Date(123);

		ServiceReference ref = new MockServiceReference(classInterfaces);
		services.put(ref, date);

		Object proxy = proxyCreator.createServiceProxy(ref).proxy;
		Object proxy2 = proxyCreator.createServiceProxy(ref).proxy;

		ServiceReferenceProxy referenceProxy = ((ImportedOsgiServiceProxy) proxy).getServiceReference();
		assertSame(ref, referenceProxy.getTargetServiceReference());
		ServiceReferenceProxy referenceProxy2 = ((ImportedOsgiServiceProxy) proxy2).getServiceReference();
		assertSame(ref, referenceProxy2.getTargetServiceReference());
		assertEquals(referenceProxy, referenceProxy2);
		assertFalse(referenceProxy == referenceProxy2);
	}
}