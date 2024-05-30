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
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.service.importer.support.ImportContextClassLoaderEnum;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.core.InfrastructureProxy;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * Unit test regarding the importers proxies and spring infrastructure proxy.
 * 
 * @author Costin Leau
 */
public class InfrastructureProxyTest {

	private StaticServiceProxyCreator proxyCreator;

	private final Class<?>[] classes = new Class<?>[] { Serializable.class, Comparable.class };

	private StaticServiceProxyCreator createProxyCreator(BundleContext ctx, Class<?>[] classes) {
		ClassLoader cl = getClass().getClassLoader();
		if (ctx == null) {
			ctx = new MockBundleContext();
		}
		return new StaticServiceProxyCreator(classes, cl, cl, ctx, ImportContextClassLoaderEnum.UNMANAGED, true, false);
	}

	@Before
	public void setup() throws Exception {
		proxyCreator = createProxyCreator(null, classes);
	}

	@After
	public void tearDown() throws Exception {
		proxyCreator = null;
	}

	@Test
	public void testCreatedProxy() throws Exception {
		MockServiceReference ref = new MockServiceReference();

		Object proxy = proxyCreator.createServiceProxy(ref).proxy;
		assertTrue(proxy instanceof ImportedOsgiServiceProxy);
		assertTrue(proxy instanceof InfrastructureProxy);
	}

	@Test
	public void testTargetProxy() throws Exception {
		final MockServiceReference ref = new MockServiceReference();
		final Object service = new Object();

		MockBundleContext ctx = new MockBundleContext() {

			public ServiceReference getServiceReference(String clazz) {
				return ref;
			}

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[] { ref };
			}

			public Object getService(ServiceReference reference) {
				return (reference == ref ? service : super.getService(reference));
			}
		};

		proxyCreator = createProxyCreator(ctx, classes);
		InfrastructureProxy proxy = (InfrastructureProxy) proxyCreator.createServiceProxy(ref).proxy;
		assertEquals(service, proxy.getWrappedObject());
		InfrastructureProxy anotherProxy =
				(InfrastructureProxy) proxyCreator.createServiceProxy(new MockServiceReference()).proxy;
		assertFalse(proxy.equals(anotherProxy));
		assertFalse(anotherProxy.getWrappedObject().equals(proxy.getWrappedObject()));
	}

	// FIXME: disabled due to some strange certificates problem with Equinox
	@Test
	@Ignore
	public void testBlueprintExceptions() throws Exception {
		MockServiceReference ref = new MockServiceReference(new String[] { Comparable.class.getName() });
		MockBundleContext ctx = new MockBundleContext() {

			@Override
			public Object getService(ServiceReference reference) {
				return null;
			}
		};
		ClassLoader cl = getClass().getClassLoader();
		StaticServiceProxyCreator creator =
				new StaticServiceProxyCreator(classes, cl, cl, ctx, ImportContextClassLoaderEnum.UNMANAGED, true, true);
		Comparable proxy = (Comparable) creator.createServiceProxy(ref).proxy;
		System.out.println(proxy.compareTo(null));
	}
}