/******************************************************************************
 * Copyright (c) 2012 VMware Inc.
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

package org.eclipse.gemini.blueprint.extender.internal.activator;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;

import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.eclipse.gemini.blueprint.mock.MockServiceRegistration;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextClosedEvent;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.extender.event.BootstrappingDependenciesEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ListListenerAdapterTest {

    private final class TestBundleContext extends MockBundleContext {

        private TestBundleContext() {
        }

        public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
            return null;
        }

        public Object getService(ServiceReference reference) {
            if (reference != null) {
                Object service = reference.getProperty(SERVICE_PROPERTY);
                if (service != null) {
                    return service;
                }
            }

            return super.getService(reference);
        }

        @SuppressWarnings("rawtypes")
        public ServiceRegistration registerService(String[] clazzes, final Object service, Dictionary properties) {
            MockServiceRegistration reg = new MockServiceRegistration(properties);

            MockServiceReference ref = new MockServiceReference(getBundle(), properties, reg, clazzes) {

                @Override
                public Object getProperty(String key) {
                    if (SERVICE_PROPERTY.equals(key)) {
                        return service;
                    } else {
                        return super.getProperty(key);
                    }
                }

            };
            ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, ref);

            for (Iterator iter = serviceListeners.iterator(); iter.hasNext();) {
                ServiceListener listener = (ServiceListener) iter.next();
                listener.serviceChanged(event);
            }

            return reg;
        }
    }

    private static final String SERVICE_PROPERTY = "service";

    private boolean fired;

    private MockBundleContext ctx;

    private ListListenerAdapter adapter;
    
    @Before
    public void setup() throws Exception {
        fired = false;
        ctx = new TestBundleContext();
        adapter = new ListListenerAdapter(ctx);
        adapter.afterPropertiesSet();
    }
    
    @After
    public void tearDown() throws Exception {
        adapter.destroy();
    }

    @Test
    public void testNormal() throws Exception {

        OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent> listener = new OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent>() {

            public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
                fired = true;
            }
        };

        ctx.registerService(OsgiBundleApplicationContextListener.class.getName(), listener, null);
        
        adapter.onOsgiApplicationEvent(new BootstrappingDependenciesEvent(new OsgiBundleXmlApplicationContext(), new MockBundle(),
            new ArrayList<OsgiServiceDependencyEvent>(), null, 0));
        assertTrue(fired);
    }

    @Test
    public void testEventFiltering() throws Exception {

        OsgiBundleApplicationContextListener<OsgiBundleContextClosedEvent> listener = new OsgiBundleApplicationContextListener<OsgiBundleContextClosedEvent>() {

            public void onOsgiApplicationEvent(OsgiBundleContextClosedEvent event) {
                fired = true;
            }
        };

        ctx.registerService(OsgiBundleApplicationContextListener.class.getName(), listener, null);
        
        adapter.onOsgiApplicationEvent(new BootstrappingDependenciesEvent(new OsgiBundleXmlApplicationContext(), new MockBundle(),
            new ArrayList<OsgiServiceDependencyEvent>(), null, 0));
        
        adapter.onOsgiApplicationEvent(new OsgiBundleContextClosedEvent(new OsgiBundleXmlApplicationContext(), new MockBundle()));
        assertTrue(fired);
    }
}
