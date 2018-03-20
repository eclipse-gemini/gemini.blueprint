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

import junit.framework.TestCase;
import org.eclipse.gemini.blueprint.io.OsgiBundleResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.util.Dictionary;
import java.util.Properties;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Costin Leau
 */
public class AbstractRefreshableOsgiBundleApplicationContextTest extends TestCase {
    private AbstractOsgiBundleApplicationContext context;
    private Bundle bundle;
    private BundleContext bundleCtx;

    protected void setUp() throws Exception {
        context = new AbstractOsgiBundleApplicationContext() {
            protected void loadBeanDefinitions(DefaultListableBeanFactory arg0) throws BeansException {
            }
        };

        bundleCtx = mock(BundleContext.class);
        bundle = mock(Bundle.class);
        when(bundleCtx.getBundle()).thenReturn(bundle);
    }

    protected void tearDown() throws Exception {
        context = null;
    }

    public void testBundleContext() {

        String location = "osgibundle://someLocation";
        Resource bundleResource = new OsgiBundleResource(bundle, location);

        Dictionary dict = new Properties();
        when(bundle.getHeaders()).thenReturn(dict);
        when(bundle.getSymbolicName()).thenReturn("symName");

        context.setBundleContext(bundleCtx);
        assertSame(bundle, context.getBundle());
        assertSame(bundleCtx, context.getBundleContext());

        ClassLoader loader = context.getClassLoader();
        assertTrue(loader instanceof BundleDelegatingClassLoader);

        // do some resource loading
        assertEquals(bundleResource, context.getResource(location));

        verify(bundle, atLeast(1)).getBundleContext();
        verify(bundle, atLeast(1)).getHeaders();
        verify(bundle, atLeast(1)).getSymbolicName();
    }

    public void testServicePublicationBetweenRefreshes() {
        ServiceRegistration registration = mock(ServiceRegistration.class);
        MockBundleContext mCtx = spy(new MockBundleContext());
        doReturn(registration).when(mCtx).registerService(isA(String[].class), isA(Object.class), isA(Dictionary.class));

        context.setBundleContext(mCtx);

        verify(mCtx, never()).registerService(isA(String[].class), isA(Object.class), isA(Dictionary.class));
        verify(registration, never()).unregister();

        context.refresh();
        verify(mCtx).registerService(isA(String[].class), isA(ApplicationContext.class), isA(Dictionary.class));
        verify(mCtx).registerService(isA(String[].class), isA(BlueprintContainer.class), isA(Dictionary.class));
        verify(registration, never()).unregister();

        context.refresh();
        verify(mCtx, times(2)).registerService(isA(String[].class), isA(ApplicationContext.class), isA(Dictionary.class));
        verify(mCtx, times(2)).registerService(isA(String[].class), isA(BlueprintContainer.class), isA(Dictionary.class));
        verify(registration).unregister();
    }
}