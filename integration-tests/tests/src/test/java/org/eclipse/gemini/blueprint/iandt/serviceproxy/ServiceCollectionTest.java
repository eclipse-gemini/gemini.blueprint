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

package org.eclipse.gemini.blueprint.iandt.serviceproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceCollection;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;
import org.junit.Test;
import org.springframework.util.ClassUtils;

abstract class ServiceCollectionTest extends BaseIntegrationTest {
	protected ServiceRegistration publishService(Object obj) throws Exception {
		return bundleContext.registerService(obj.getClass().getName(), obj, null);
	}

	public void testCGLIBAvailable() throws Exception {
		assertTrue(ClassUtils.isPresent("org.springframework.cglib.proxy.Enhancer", DefaultAopProxyFactory.class.getClassLoader()));
	}

	protected Collection createCollection() {
		BundleDelegatingClassLoader classLoader =
				BundleDelegatingClassLoader.createBundleClassLoaderFor(bundleContext.getBundle());

		OsgiServiceCollection collection = new OsgiServiceCollection(null, bundleContext, classLoader, null, false);
		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(classLoader);
			collection.setRequiredAtStartup(false);
			// collection.setInterfaces(new Class<?>[] { Date.class });
			collection.afterPropertiesSet();
		} finally {
			Thread.currentThread().setContextClassLoader(tccl);
		}

		return collection;
	}

	@Test
	public void testCollectionListener() throws Exception {
		Collection collection = createCollection();

		ServiceReference[] refs = bundleContext.getServiceReferences((String)null, null);

		assertEquals(refs.length, collection.size());
		int size = collection.size();
		// register a service
		long time = 123456;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);
		try {
			assertEquals(size + 1, collection.size());
		} finally {
			reg.unregister();
		}

		assertEquals(size, collection.size());
	}

	@Test
	public void testCollectionContent() throws Exception {
		Collection collection = createCollection();
		ServiceReference[] refs = bundleContext.getServiceReferences((String)null, null);

		assertEquals(refs.length, collection.size());
		int size = collection.size();

		// register a service
		long time = 123456;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);
		try {
			assertEquals(size + 1, collection.size());
			// test service
			Iterator iter = collection.iterator();
			// reach our new service index
			for (int i = 0; i < size; i++) {
				iter.next();
			}
			Object myService = iter.next();
			// be sure to use classes loaded by the same CL
			assertTrue(myService instanceof Date);
			assertEquals(time, ((Date) myService).getTime());
		} finally {
			reg.unregister();
		}

		assertEquals(size, collection.size());
	}
}