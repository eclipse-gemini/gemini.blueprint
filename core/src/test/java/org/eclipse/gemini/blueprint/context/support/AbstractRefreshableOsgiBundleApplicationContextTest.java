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

import static org.easymock.EasyMock.*;

import org.easymock.IMocksControl;

import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceRegistration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.gemini.blueprint.io.OsgiBundleResource;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceRegistration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;

/**
 * @author Costin Leau
 */
public class AbstractRefreshableOsgiBundleApplicationContextTest extends TestCase {

    private AbstractOsgiBundleApplicationContext context;
    private IMocksControl mocksControl;
    private Bundle bundle;
    private BundleContext bundleCtx;

    protected void setUp() throws Exception {
        context = new AbstractOsgiBundleApplicationContext() {

            protected void loadBeanDefinitions(DefaultListableBeanFactory arg0) throws IOException, BeansException {
            }
        };

        mocksControl = createStrictControl();

        bundleCtx = mocksControl.createMock(BundleContext.class);
        bundle = createNiceMock(Bundle.class);
        expect(bundleCtx.getBundle()).andReturn(bundle);
    }

    protected void tearDown() throws Exception {
        context = null;
    }

    public void testBundleContext() throws Exception {

        String location = "osgibundle://someLocation";
        Resource bundleResource = new OsgiBundleResource(bundle, location);

        Dictionary dict = new Properties();

        expect(bundle.getHeaders()).andReturn(dict);
        expect(bundle.getSymbolicName()).andReturn("symName").atLeastOnce();

        replay(bundle, bundleCtx);

        context.setBundleContext(bundleCtx);
        assertSame(bundle, context.getBundle());
        assertSame(bundleCtx, context.getBundleContext());

        ClassLoader loader = context.getClassLoader();
        assertTrue(loader instanceof BundleDelegatingClassLoader);

        // do some resource loading
        assertEquals(bundleResource, context.getResource(location));

        verify(bundle, bundleCtx);
    }

    public void testServicePublicationBetweenRefreshes() throws Exception {
        // [0] = service registration
        // [1] = service unregistration

        final int[] counters = new int[]{0, 0};

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