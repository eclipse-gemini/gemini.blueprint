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

package org.eclipse.gemini.blueprint.context.support;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.eclipse.gemini.blueprint.context.support.AbstractOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.io.OsgiBundleResource;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceRegistration;

/**
 * @author Costin Leau
 * 
 */
public class AbstractRefreshableOsgiBundleApplicationContextTest extends TestCase {

	private AbstractOsgiBundleApplicationContext context;
	private Bundle bundle;
	private BundleContext bundleCtx;
	private MockControl bundleCtrl, bundleCtxCtrl;


	protected void setUp() throws Exception {
		context = new AbstractOsgiBundleApplicationContext() {

			protected void loadBeanDefinitions(DefaultListableBeanFactory arg0) throws IOException, BeansException {
			}
		};

		bundleCtxCtrl = MockControl.createStrictControl(BundleContext.class);
		bundleCtx = (BundleContext) bundleCtxCtrl.getMock();

		bundleCtrl = MockControl.createNiceControl(Bundle.class);
		bundle = (Bundle) bundleCtrl.getMock();

		bundleCtxCtrl.expectAndReturn(bundleCtx.getBundle(), bundle);

	}

	protected void tearDown() throws Exception {
		context = null;
	}

	public void testBundleContext() throws Exception {

		String location = "osgibundle://someLocation";
		Resource bundleResource = new OsgiBundleResource(bundle, location);

		Dictionary dict = new Properties();
		bundleCtrl.expectAndReturn(bundle.getHeaders(), dict);
		bundleCtrl.expectAndReturn(bundle.getSymbolicName(), "symName", MockControl.ONE_OR_MORE);
		bundleCtrl.replay();
		bundleCtxCtrl.replay();

		context.setBundleContext(bundleCtx);
		assertSame(bundle, context.getBundle());
		assertSame(bundleCtx, context.getBundleContext());

		ClassLoader loader = context.getClassLoader();
		assertTrue(loader instanceof BundleDelegatingClassLoader);

		// do some resource loading
		assertEquals(bundleResource, context.getResource(location));

		bundleCtrl.verify();
		bundleCtxCtrl.verify();
	}

	public void testServicePublicationBetweenRefreshes() throws Exception {
		// [0] = service registration
		// [1] = service unregistration
		
		final int[] counters = new int[] { 0, 0 };

		MockBundleContext mCtx = new MockBundleContext() {

			public ServiceRegistration registerService(String clazz[], Object service, Dictionary properties) {
				counters[0]++;
				return new MockServiceRegistration(clazz, properties) {

					public void unregister() {
						counters[1]++;
					}
				};
			}

		};
		context.setBundleContext(mCtx);

		assertEquals(counters[0], 0);
		assertEquals(counters[1], 0);

		context.refresh();
		assertEquals(counters[0], 1);
		assertEquals(counters[1], 0);

		context.refresh();
		assertEquals(counters[0], 2);
		assertEquals(counters[1], 1);
	}
}