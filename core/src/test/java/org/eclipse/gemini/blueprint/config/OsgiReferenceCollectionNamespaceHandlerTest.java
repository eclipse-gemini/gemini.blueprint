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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Externalizable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceList;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceSet;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceSortedList;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceSortedSet;
import org.eclipse.gemini.blueprint.service.importer.support.internal.util.ServiceReferenceComparator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * @author Costin Leau
 * 
 */
public class OsgiReferenceCollectionNamespaceHandlerTest {

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
				return new ServiceReference[0];
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		// reader.setEventListener(this.listener);
		reader.loadBeanDefinitions(new ClassPathResource("osgiReferenceCollectionNamespaceHandlerTests.xml", getClass()));
		appContext.refresh();
	}

	@After
	public void tearDown() throws Exception {
		appContext.close();
	}

	@Test
	public void testSimpleList() {
		Object factoryBean = appContext.getBean("&simpleList");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);
		// get the factory product
		Object bean = appContext.getBean("simpleList");
		assertFalse(bean instanceof OsgiServiceList);
		assertTrue(bean instanceof List);
	}

	@Test
	public void testSimpleSet() {
		Object factoryBean = appContext.getBean("&simpleSet");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);
		// get the factory product
		Object bean = appContext.getBean("simpleSet");
		assertFalse(bean instanceof OsgiServiceSet);
		assertTrue(bean instanceof Set);
	}

	@Test
	public void testSimpleListWithGreedyProxyingOn() throws Exception {
		Object factoryBean = appContext.getBean("&simpleListWithGreedyProxying");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);
		assertEquals(Boolean.TRUE, (Boolean) TestUtils.getFieldValue(factoryBean, "greedyProxying"));
		// get the factory product
		Object bean = appContext.getBean("simpleListWithGreedyProxying");
		assertFalse(bean instanceof OsgiServiceList);
		assertTrue(bean instanceof List);
	}

	@Test
	public void testSimpleListWithDefaultProxying() throws Exception {
		Object factoryBean = appContext.getBean("&simpleSet");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);
		assertEquals(Boolean.FALSE, (Boolean) TestUtils.getFieldValue(factoryBean, "greedyProxying"));
	}

	@Test
	public void testImplicitSortedList() {
		Object factoryBean = appContext.getBean("&implicitSortedList");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);
		// get the factory product
		Object bean = appContext.getBean("implicitSortedList");
		assertFalse(bean instanceof OsgiServiceSortedList);
		assertTrue(bean instanceof List);

		OsgiServiceSortedList exposedProxy = (OsgiServiceSortedList) TestUtils.getFieldValue(factoryBean,
			"exposedProxy");
		assertSame(appContext.getBean("defaultComparator"), exposedProxy.comparator());
	}

	@Test
	public void testImplicitSortedSet() {
		Object factoryBean = appContext.getBean("&implicitSortedSet");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);

		Object bean = appContext.getBean("implicitSortedSet");
		assertFalse(bean instanceof OsgiServiceSortedSet);
		assertTrue(bean instanceof SortedSet);

		assertSame(appContext.getBean("defaultComparator"), ((SortedSet) bean).comparator());
	}

	@Test
	public void testSimpleSortedList() {
		Object factoryBean = appContext.getBean("&implicitSortedList");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);

		Object bean = appContext.getBean("implicitSortedList");
		assertFalse(bean instanceof OsgiServiceSortedList);
		assertTrue(bean instanceof List);

		Class<?>[] intfs = getInterfaces(factoryBean);
		assertTrue(Arrays.equals(new Class<?>[] { Serializable.class }, intfs));
	}

	@Test
	public void testSimpleSortedSet() {
		Object factoryBean = appContext.getBean("&implicitSortedSet");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);

		Object bean = appContext.getBean("implicitSortedSet");
		assertFalse(bean instanceof OsgiServiceSortedSet);
		assertTrue(bean instanceof SortedSet);

		Class<?>[] intfs = getInterfaces(factoryBean);
		assertTrue(Arrays.equals(new Class<?>[] { Externalizable.class }, intfs));
	}

	@Test
	public void testSortedSetWithNaturalOrderingOnRefs() throws Exception {
		Object factoryBean = appContext.getBean("&sortedSetWithNaturalOrderingOnRefs");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);

		Comparator comp = getComparator(factoryBean);

		assertNotNull(comp);
		assertSame(ServiceReferenceComparator.class, comp.getClass());

		Class<?>[] intfs = getInterfaces(factoryBean);
		assertTrue(Arrays.equals(new Class<?>[] { Externalizable.class }, intfs));

		OsgiServiceLifecycleListener[] listeners = getListeners(factoryBean);
		assertEquals(2, listeners.length);

		Object bean = appContext.getBean("sortedSetWithNaturalOrderingOnRefs");
		assertFalse(bean instanceof OsgiServiceSortedSet);
		assertTrue(bean instanceof SortedSet);

	}

	@Test
	public void testSortedListWithNaturalOrderingOnServs() throws Exception {
		Object factoryBean = appContext.getBean("&sortedListWithNaturalOrderingOnServs");
		assertTrue(factoryBean instanceof OsgiServiceCollectionProxyFactoryBean);

		assertNull(getComparator(factoryBean));

		Object bean = appContext.getBean("sortedListWithNaturalOrderingOnServs");
		assertFalse(bean instanceof OsgiServiceSortedList);
		assertTrue(bean instanceof List);

		Class<?>[] intfs = getInterfaces(factoryBean);
		assertTrue(Arrays.equals(new Class<?>[] { Externalizable.class }, intfs));
	}

	private Class<?>[] getInterfaces(Object proxy) {
		return (Class[]) TestUtils.getFieldValue(proxy, "interfaces");
	}

	private Comparator getComparator(Object proxy) {
		return (Comparator) TestUtils.getFieldValue(proxy, "comparator");
	}

	private OsgiServiceLifecycleListener[] getListeners(Object proxy) {
		return (OsgiServiceLifecycleListener[]) TestUtils.getFieldValue(proxy, "listeners");
	}
	
	
}
