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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.aopalliance.aop.Advice;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.aop.framework.ProxyFactory;
import org.eclipse.gemini.blueprint.service.ServiceUnavailableException;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceDynamicInterceptor;
import org.eclipse.gemini.blueprint.test.AbstractConfigurableBundleCreatorTests;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;
import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;
import org.junit.Test;
import org.springframework.util.ClassUtils;

/**
 * @author Costin Leau
 * 
 */
public abstract class ServiceProxyTst extends AbstractConfigurableBundleCreatorTests {
	protected String getManifestLocation() {
		return null;
	}

	private ServiceRegistration publishService(Object obj) throws Exception {
		return bundleContext.registerService(obj.getClass().getName(), obj, null);
	}

	private Object createProxy(final Class<?> clazz, Advice cardinalityInterceptor) {
		ProxyFactory factory = new ProxyFactory();
		factory.setProxyTargetClass(true);
		factory.setOptimize(true);
		factory.setTargetClass(clazz);

		factory.addAdvice(cardinalityInterceptor);
		factory.setFrozen(true);

		return factory.getProxy(ProxyFactory.class.getClassLoader());
	}

	private Advice createCardinalityAdvice(Class<?> clazz) {
		ClassLoader classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundleContext.getBundle());
		ServiceDynamicInterceptor interceptor = new ServiceDynamicInterceptor(bundleContext, null,
			OsgiFilterUtils.createFilter(OsgiFilterUtils.unifyFilter(clazz, null)), classLoader);
		// fast retry
		interceptor.setMandatoryService(true);
		interceptor.afterPropertiesSet();
		interceptor.getRetryTemplate().reset(1);
		return interceptor;

	}

	@Test
	public void testCglibLibraryVisibility() {
		// note that cglib is not declared inside this bundle but should be seen
		// by spring-core (which contains the util classes)
		assertTrue(ClassUtils.isPresent("org.springframework.cglib.proxy.Enhancer", ProxyFactory.class.getClassLoader()));
	}

	@Test
	public void testDynamicEndProxy() throws Exception {
		long time = 123456;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);
		BundleContext ctx = bundleContext;

		try {
			ServiceReference ref = ctx.getServiceReference(Date.class.getName());
			assertNotNull(ref);
			Date proxy = (Date) createProxy(Date.class, createCardinalityAdvice(Date.class));
			assertEquals(time, proxy.getTime());
			// take down service
			reg.unregister();
			// reference is invalid
			assertNull(ref.getBundle());

			try {
				proxy.getTime();
				fail("should have thrown exception");
			}
			catch (ServiceUnavailableException sue) {
				// service failed
			}

			// rebind the service
			reg = publishService(date);
			// retest the service
			assertEquals(time, proxy.getTime());
		}
		finally {
			if (reg != null)
				try {
					reg.unregister();
				}
				catch (Exception ex) {
					// ignore
				}
		}
	}

}
