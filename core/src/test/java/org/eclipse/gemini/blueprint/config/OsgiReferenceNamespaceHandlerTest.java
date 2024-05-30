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

package org.eclipse.gemini.blueprint.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * Integration test for osgi:reference namespace handler.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiReferenceNamespaceHandlerTest {

	private GenericApplicationContext appContext;

	@Before
	public void setup() throws Exception {
		// reset counter just to be sure
		DummyListener.BIND_CALLS = 0;
		DummyListener.UNBIND_CALLS = 0;

		DummyListenerServiceSignature.BIND_CALLS = 0;
		DummyListenerServiceSignature.UNBIND_CALLS = 0;

		DummyListenerServiceSignature2.BIND_CALLS = 0;
		DummyListenerServiceSignature2.UNBIND_CALLS = 0;

		BundleContext bundleContext = new MockBundleContext() {
			// service reference already registered
			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[] { new MockServiceReference(new String[] { Cloneable.class.getName() }) };
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		// reader.setEventListener(this.listener);
		reader.loadBeanDefinitions(new ClassPathResource("osgiReferenceNamespaceHandlerTests.xml", getClass()));
		appContext.refresh();
	}

	@After
	public void tearDown() throws Exception {
		appContext.close();
	}

	@Test
	public void testSimpleReference() throws Exception {
		Object factoryBean = appContext.getBean("&serializable");

		assertTrue(factoryBean instanceof OsgiServiceProxyFactoryBean);
		OsgiServiceProxyFactoryBean proxyFactory = (OsgiServiceProxyFactoryBean) factoryBean;

		Class<?>[] intfs = (Class[]) TestUtils.getFieldValue(proxyFactory, "interfaces");

		assertEquals(1, intfs.length);
		assertSame(Serializable.class, intfs[0]);

		// get the factory product
		Object bean = appContext.getBean("serializable");
		assertTrue(bean instanceof Serializable);
		assertTrue(Proxy.isProxyClass(bean.getClass()));

	}

	@Test
	public void testFullReference() throws Exception {
		OsgiServiceProxyFactoryBean factory = (OsgiServiceProxyFactoryBean) appContext.getBean("&full-options");
		factory.getObject(); // required to initialise proxy and hook
		// listeners into the binding process

		OsgiServiceLifecycleListener[] listeners = (OsgiServiceLifecycleListener[]) TestUtils.getFieldValue(factory,
			"listeners");
		assertNotNull(listeners);
		assertEquals(5, listeners.length);

		assertEquals("already registered service should have been discovered", 4, DummyListener.BIND_CALLS);
		assertEquals(0, DummyListener.UNBIND_CALLS);

		listeners[1].bind(null, null);

		assertEquals(6, DummyListener.BIND_CALLS);

		listeners[1].unbind(null, null);
		assertEquals(2, DummyListener.UNBIND_CALLS);

		assertEquals(1, DummyListenerServiceSignature.BIND_CALLS);
		listeners[4].bind(null, null);
		assertEquals(2, DummyListenerServiceSignature.BIND_CALLS);

		assertEquals(0, DummyListenerServiceSignature.UNBIND_CALLS);
		listeners[4].unbind(null, null);
		assertEquals(1, DummyListenerServiceSignature.UNBIND_CALLS);

		assertEquals(1, DummyListenerServiceSignature2.BIND_CALLS);
		listeners[3].bind(null, null);
		assertEquals(2, DummyListenerServiceSignature2.BIND_CALLS);
		
		assertEquals(0, DummyListenerServiceSignature2.UNBIND_CALLS);
		listeners[3].unbind(null, null);
		assertEquals(1, DummyListenerServiceSignature2.UNBIND_CALLS);
	}

	@Test
	public void testMultipleInterfaces() throws Exception {
		OsgiServiceProxyFactoryBean factory = (OsgiServiceProxyFactoryBean) appContext.getBean("&multi-interfaces");
		Class<?>[] intfs = (Class[]) TestUtils.getFieldValue(factory, "interfaces");
		assertNotNull(intfs);
		assertEquals(2, intfs.length);

		assertTrue(Arrays.equals(new Class<?>[] { Serializable.class, Externalizable.class }, intfs));
	}

	@Test
	public void testBeanNameAttrToServiceBeanNameProperty() throws Exception {
		OsgiServiceProxyFactoryBean factory = (OsgiServiceProxyFactoryBean) appContext.getBean("&importerWithBeanName");
		Object obj = TestUtils.getFieldValue(factory, "serviceBeanName");
		assertEquals("bean-name attr hasn't been properly parsed", "someBean", obj);
	}
}