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

package org.eclipse.gemini.blueprint.service.exporter.support;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

import org.easymock.IMocksControl;
import org.eclipse.gemini.blueprint.service.exporter.OsgiServiceRegistrationListener;
import org.eclipse.gemini.blueprint.service.exporter.TestRegistrationListener;
import org.eclipse.gemini.blueprint.service.exporter.support.DefaultInterfaceDetector;
import org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean;
import org.eclipse.gemini.blueprint.service.exporter.support.ServicePropertiesChangeEvent;
import org.eclipse.gemini.blueprint.service.exporter.support.ServicePropertiesChangeListener;
import org.eclipse.gemini.blueprint.service.exporter.support.ServicePropertiesListenerManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceRegistration;

/**
 * @author Costin Leau
 */
public class OsgiServiceFactoryBeanTest extends TestCase {

    private OsgiServiceFactoryBean exporter;

    private ConfigurableBeanFactory beanFactory;

    private IMocksControl beanFactoryControl;

    private BundleContext bundleContext;

    private IMocksControl ctxCtrl;

    private BundleContext ctx;

    class MockServiceFactory implements ServiceFactory {

        public Object getService(Bundle bundle, ServiceRegistration registration) {
            return null;
        }

        public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
        }
    }

    class UpdateableProperties extends Properties implements ServicePropertiesListenerManager {

        public List<ServicePropertiesChangeListener> listeners = new ArrayList<ServicePropertiesChangeListener>();

        public void addListener(ServicePropertiesChangeListener listener) {
            listeners.add(listener);
        }

        public void removeListener(ServicePropertiesChangeListener listener) {
            listeners.remove(listener);
        }

        public void update() {
            ServicePropertiesChangeEvent event = new ServicePropertiesChangeEvent(this);
            for (ServicePropertiesChangeListener listener : listeners) {
                listener.propertiesChange(event);
            }
        }
    }

    protected void setUp() throws Exception {
        exporter = new OsgiServiceFactoryBean();
        beanFactoryControl = createControl();
        beanFactory = this.beanFactoryControl.createMock(ConfigurableBeanFactory.class);
        bundleContext = new MockBundleContext();
        ctxCtrl = createControl();
        ctx = ctxCtrl.createMock(BundleContext.class);

        exporter.setBeanFactory(beanFactory);
        exporter.setBundleContext(bundleContext);
    }

    protected void tearDown() throws Exception {
        exporter = null;
        bundleContext = null;
        ctxCtrl = null;
        ctx = null;
    }

    public void testInitWithoutBundleContext() throws Exception {
        exporter.setBundleContext(null);
        exporter.setTarget(new Object());

        try {
            this.exporter.afterPropertiesSet();
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testInitWithoutBeanFactory() throws Exception {
        exporter.setBeanFactory(null);
        exporter.setTarget(new Object());

        try {
            this.exporter.afterPropertiesSet();
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testInitWithoutTargetOrTargetReference() throws Exception {
        try {
            this.exporter.afterPropertiesSet();
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testInitWithTargetAndTargetRerefence() throws Exception {
        final Object obj = new Object();
        exporter.setTarget(obj);
        exporter.setTargetBeanName("costin");
        expect(beanFactory.isSingleton("costin")).andReturn(false);
        expect(beanFactory.containsBean("costin")).andReturn(true);
        expect(beanFactory.getType("costin")).andReturn((Class)Object.class);
        beanFactoryControl.replay();
        try {
            this.exporter.afterPropertiesSet();
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testInitWithOnlyJustTarget() throws Exception {
        exporter.setTarget(new Object());
        exporter.setInterfaces(new Class<?>[]{Object.class});
        exporter.afterPropertiesSet();
    }

    public void testAutoDetectClassesForPublishingDisabled() throws Exception {
        exporter.setInterfaceDetector(DefaultInterfaceDetector.DISABLED);
        Class<?>[] clazz = DefaultInterfaceDetector.DISABLED.detect(Integer.class);
        assertNotNull(clazz);
        assertEquals(0, clazz.length);
    }

    public void testAutoDetectClassesForPublishingInterfaces() throws Exception {
        exporter.setInterfaceDetector(DefaultInterfaceDetector.INTERFACES);
        Class<?>[] clazz = DefaultInterfaceDetector.INTERFACES.detect(HashMap.class);
        Class<?>[] expected = new Class<?>[]{Cloneable.class, Serializable.class, Map.class};

        assertTrue(compareArrays(expected, clazz));
    }

    public void testAutoDetectClassesForPublishingClassHierarchy() throws Exception {
        exporter.setInterfaceDetector(DefaultInterfaceDetector.CLASS_HIERARCHY);
        Class<?>[] clazz = DefaultInterfaceDetector.CLASS_HIERARCHY.detect(HashMap.class);
        Class<?>[] expected = new Class<?>[]{HashMap.class, AbstractMap.class};
        assertTrue(compareArrays(expected, clazz));
    }

    public void testAutoDetectClassesForPublishingAll() throws Exception {
        exporter.setInterfaceDetector(DefaultInterfaceDetector.ALL_CLASSES);
        Class<?>[] clazz = DefaultInterfaceDetector.ALL_CLASSES.detect(HashMap.class);
        Class<?>[] expected =
                new Class<?>[]{Map.class, Cloneable.class, Serializable.class, HashMap.class, AbstractMap.class};
        assertTrue(compareArrays(expected, clazz));
    }

    public void testRegisterServiceWithNullClasses() throws Exception {
        try {
            exporter.registerService(null, new Properties());
            fail("Expected to throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testRegisterServiceWOClasses() throws Exception {
        try {
            exporter.registerService(new Class[0], new Properties());
            fail("Expected to throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testRegisterService() throws Exception {
        Class<?>[] clazz =
                new Class<?>[]{Serializable.class, HashMap.class, Cloneable.class, Map.class, LinkedHashMap.class};

        String[] names = new String[clazz.length];

        for (int i = 0; i < clazz.length; i++) {
            names[i] = clazz[i].getName();
        }

        final Properties props = new Properties();
        final ServiceRegistration reg = new MockServiceRegistration();

        exporter.setBundleContext(new MockBundleContext() {

            public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
                assertTrue(service instanceof ServiceFactory);
                return reg;
            }
        });

        Object proxy = createMock(ServiceFactory.class);
        exporter.setTarget(proxy);
        exporter.setInterfaces(new Class<?>[]{ServiceFactory.class});
        String beanName = "boo";
        exporter.setTargetBeanName(beanName);

        expect(beanFactory.isSingleton(beanName)).andReturn(true);
        expect(beanFactory.isPrototype(beanName)).andReturn(false);
        expect(beanFactory.containsBean(beanName)).andReturn(true);
        expect(beanFactory.getBean(beanName)).andReturn(proxy);
        expect(beanFactory.getType(beanName)).andStubReturn((Class) ServiceFactory.class);
        beanFactoryControl.replay();

        exporter.afterPropertiesSet();
        assertSame(reg, exporter.registerService(clazz, props));
    }

    public void testUnregisterWithNullServiceReg() throws Exception {
        exporter.unregisterService(null);
    }

    public void testUnregisterService() throws Exception {
        ServiceRegistration reg = createMock(ServiceRegistration.class);

        reg.unregister();
        expectLastCall();
        replay(reg);
        exporter.unregisterService(reg);
        verify(reg);
    }

    public void testUnregisterServiceAlreadyUnregistered() throws Exception {
        ServiceRegistration reg = createMock(ServiceRegistration.class);

        reg.unregister();
        expectLastCall().andThrow(new IllegalStateException());
        replay(reg);
        exporter.unregisterService(reg);
        verify(reg);
    }

    public void testLazyBeanServiceWithUsualBean() throws Exception {
        final ServiceRegistration reg = new MockServiceRegistration();
        final ServiceFactory[] factory = new ServiceFactory[1];

        Object service = new Object();

        BundleContext ctx = new MockBundleContext() {

            public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
                assertTrue(service instanceof ServiceFactory);
                factory[0] = (ServiceFactory) service;
                return reg;
            }
        };

        exporter.setBundleContext(ctx);

        String beanName = "fooBar";
        exporter.setTargetBeanName(beanName);
        exporter.setInterfaces(new Class<?>[]{service.getClass()});
        expect(beanFactory.isSingleton(beanName)).andReturn(true).times(2);
        expect(beanFactory.containsBean(beanName)).andReturn(true);
        expect(beanFactory.getBean(beanName)).andReturn(service);
        expect(beanFactory.getType(beanName)).andStubReturn((Class) service.getClass());
        beanFactoryControl.replay();

        exporter.afterPropertiesSet();
        exporter.registerService(new Class<?>[]{service.getClass()}, new Properties());

        assertSame(service, factory[0].getService(null, null));
        beanFactoryControl.verify();
    }

    public void testLazyBeanServiceWithServiceFactoryBean() throws Exception {
        final ServiceRegistration reg = new MockServiceRegistration();
        final ServiceFactory[] factory = new ServiceFactory[1];

        final Object actualService = new Object();
        Object service = new MockServiceFactory() {
            public Object getService(Bundle arg0, ServiceRegistration arg1) {
                return actualService;
            }
        };

        BundleContext ctx = new MockBundleContext() {

            public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
                assertTrue(service instanceof ServiceFactory);
                factory[0] = (ServiceFactory) service;
                return reg;
            }
        };

        String beanName = "fooBar";
        expect(beanFactory.isSingleton(beanName)).andReturn(true).times(2);
        expect(beanFactory.containsBean(beanName)).andReturn(true);
        expect(beanFactory.getBean(beanName)).andReturn(service);
        expect(beanFactory.getType(beanName)).andStubReturn((Class) service.getClass());
        beanFactoryControl.replay();

        exporter.setBundleContext(ctx);
        exporter.setBeanFactory(beanFactory);
        exporter.setTargetBeanName(beanName);
        exporter.setInterfaces(new Class<?>[]{service.getClass()});

        exporter.afterPropertiesSet();
        exporter.registerService(new Class<?>[]{actualService.getClass()}, new Properties());
        assertSame(actualService, factory[0].getService(null, null));
        beanFactoryControl.verify();
    }

    public void testLazyBeanServiceWithTargetObjectSet() throws Exception {
        final ServiceRegistration reg = new MockServiceRegistration();
        final ServiceFactory[] factory = new ServiceFactory[1];

        Object service = new Object();

        BundleContext ctx = new MockBundleContext() {

            public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
                assertTrue(service instanceof ServiceFactory);
                factory[0] = (ServiceFactory) service;
                return reg;
            }
        };

        exporter.setBundleContext(ctx);
        exporter.setBeanFactory(beanFactory);

        // give an actual target object not a target reference
        exporter.setTarget(service);
        exporter.setInterfaces(new Class<?>[]{service.getClass()});

        beanFactoryControl.replay();
        exporter.afterPropertiesSet();
        exporter.registerService(new Class<?>[]{service.getClass()}, new Properties());

        assertSame(service, factory[0].getService(null, null));
        beanFactoryControl.verify();
    }

    private boolean compareArrays(Object[] a, Object[] b) {
        if (a.length != b.length)
            return false;

        for (Object anA : a) {
            boolean found = false;
            for (Object aB : b) {
                if (anA.equals(aB)) {
                    found = true;
                    break;
                }
            }
            if (!found)
                return false;
        }
        return true;
    }

    public void testServiceFactory() throws Exception {
        ServiceFactory factory = new MockServiceFactory();

        ctx = new MockBundleContext();
        exporter.setBundleContext(ctx);
        exporter.setBeanFactory(beanFactory);
        exporter.setInterfaces(new Class<?>[]{Serializable.class, Cloneable.class});
        exporter.setTarget(factory);
        beanFactoryControl.replay();
        exporter.afterPropertiesSet();
    }

    public void testUpdateableProperties() throws Exception {
        UpdateableProperties properties = new UpdateableProperties();
        properties.setProperty("steve", "vai");

        exporter.setServiceProperties(properties);
        exporter.setTarget("string");
        exporter.setBeanName("string");
        exporter.setInterfaces(new Class<?>[]{Serializable.class});
        beanFactoryControl.replay();
        exporter.afterPropertiesSet();

        ServiceRegistration reg = exporter.getObject();
        assertEquals("vai", reg.getReference().getProperty("steve"));
        assertNull(reg.getReference().getProperty("updated"));

        properties.setProperty("steve", "jobs");
        properties.setProperty("updated", "true");
        properties.update();

        assertEquals("jobs", reg.getReference().getProperty("steve"));
        assertNotNull(reg.getReference().getProperty("updated"));
    }

    public void testPrototypeServiceFactory() throws Exception {
        ServiceFactory factory = new MockServiceFactory();
        String beanName = "prototype-sf";
        expect(beanFactory.isSingleton(beanName)).andReturn(false);
        expect(beanFactory.isPrototype(beanName)).andReturn(true);
        expect(beanFactory.containsBean(beanName)).andReturn(true);
        expect(beanFactory.getBean(beanName)).andReturn(factory);
        expect(beanFactory.getType(beanName)).andStubReturn((Class) factory.getClass());

        exporter.setTargetBeanName(beanName);
        exporter.setInterfaces(new Class<?>[]{Serializable.class});
        beanFactoryControl.replay();
        exporter.afterPropertiesSet();
    }

    public void testPrototypeServiceFactoryDestruction() throws Exception {
        ServiceFactory factory = new MockServiceFactory();
        String beanName = "prototype-sf";

        expect(beanFactory.isSingleton(beanName)).andReturn(false).times(2);
        expect(beanFactory.containsBean(beanName)).andReturn(true);
        expect(beanFactory.isPrototype(beanName)).andReturn(true);
        expect(beanFactory.getBean(beanName)).andReturn(factory);
        expect(beanFactory.getType(beanName)).andStubReturn((Class) factory.getClass());


        exporter.setTargetBeanName(beanName);
        exporter.setInterfaces(new Class<?>[]{Serializable.class});
        beanFactoryControl.replay();
        exporter.afterPropertiesSet();
        exporter.destroy();
    }

    public void testNonSingletonServiceFactoryRegistration() throws Exception {
        TestRegistrationListener listener = new TestRegistrationListener();

        ServiceFactory factory = new MockServiceFactory();
        String beanName = "prototype-sf";
        expect(beanFactory.isSingleton(beanName)).andReturn(false).times(3);
        expect(beanFactory.containsBean(beanName)).andReturn(true);
        expect(beanFactory.isPrototype(beanName)).andReturn(false);
        expect(beanFactory.getBean(beanName)).andReturn(factory);
        expect(beanFactory.getType(beanName)).andStubReturn((Class) factory.getClass());


        exporter.setTargetBeanName(beanName);
        exporter.setInterfaces(new Class<?>[]{Serializable.class});
        exporter.setListeners(new OsgiServiceRegistrationListener[]{listener});

        beanFactoryControl.replay();
        assertEquals(0, listener.registered.size());
        assertEquals(0, listener.unregistered.size());

        exporter.afterPropertiesSet();

        assertEquals(1, listener.registered.size());
        assertEquals(0, listener.unregistered.size());

        assertNull(listener.registered.keySet().iterator().next());
        exporter.destroy();
        assertEquals(1, listener.unregistered.size());
        assertNull(listener.unregistered.keySet().iterator().next());
    }

    public void testNonSingletonNonServiceFactoryRegistration() throws Exception {
        TestRegistrationListener listener = new TestRegistrationListener();

        Object obj = new Object();
        String beanName = "prototype-non-sf";

        expect(beanFactory.isSingleton(beanName)).andReturn(false).times(3);
        expect(beanFactory.containsBean(beanName)).andReturn(true);
        expect(beanFactory.getBean(beanName)).andReturn(obj);
        expect(beanFactory.getType(beanName)).andStubReturn((Class) Object.class);

        exporter.setTargetBeanName(beanName);
        exporter.setInterfaces(new Class<?>[]{Object.class});
        exporter.setListeners(new OsgiServiceRegistrationListener[]{listener});

        beanFactoryControl.replay();
        assertEquals(listener.registered.size(), 0);
        assertEquals(listener.unregistered.size(), 0);

        exporter.afterPropertiesSet();
        assertEquals(listener.registered.size(), 1);
        assertEquals(listener.unregistered.size(), 0);

        assertNull(listener.registered.keySet().iterator().next());
        exporter.destroy();
        assertEquals(listener.unregistered.size(), 1);
        assertNull(listener.unregistered.keySet().iterator().next());
    }
}