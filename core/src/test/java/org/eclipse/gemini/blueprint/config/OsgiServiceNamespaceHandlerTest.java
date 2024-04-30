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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.eclipse.gemini.blueprint.mock.MockServiceRegistration;
import org.eclipse.gemini.blueprint.service.exporter.OsgiServiceRegistrationListener;
import org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Integration test for osgi:service namespace handler.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceNamespaceHandlerTest {

	private GenericApplicationContext appContext;

	private BundleContext bundleContext;

	private final List services = new ArrayList();

	private ServiceRegistration registration;

	@Before
	public void setup() throws Exception {

		services.clear();

		RegistrationListener.BIND_CALLS = 0;
		RegistrationListener.UNBIND_CALLS = 0;

		CustomRegistrationListener.REG_CALLS = 0;
		CustomRegistrationListener.UNREG_CALLS = 0;

		registration = new MockServiceRegistration();

		bundleContext = new MockBundleContext() {

			public ServiceReference[] getServiceReferences(String clazz, String filter) {
				return new ServiceReference[] { new MockServiceReference(new String[] { Cloneable.class.getName() }) };
			}

			public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
				services.add(service);
				return registration;
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		// reader.setEventListener(this.listener);
		reader.loadBeanDefinitions(new ClassPathResource("osgiServiceNamespaceHandlerTests.xml", getClass()));
		appContext.refresh();
	}

	private Object getServiceAtIndex(int index) {
		Object sFactory = services.get(index);
		assertNotNull(sFactory);
		assertTrue(sFactory instanceof ServiceFactory);
		ServiceFactory fact = (ServiceFactory) sFactory;
		return fact.getService(null, null);
	}

	@Test
	public void testSimpleService() {
		Object bean = appContext.getBean("&inlineReference");
		assertSame(OsgiServiceFactoryBean.class, bean.getClass());
		OsgiServiceFactoryBean exporter = (OsgiServiceFactoryBean) bean;

		assertTrue(Arrays.equals(new Class<?>[] { Serializable.class }, getInterfaces(exporter)));
		assertEquals("string", getTargetBeanName(exporter));
		//assertEquals(appContext.getBean("string"), getTarget(exporter));

		assertSame(appContext.getBean("string"), getServiceAtIndex(0));
	}

	@Test
	public void testBiggerService() {
		OsgiServiceFactoryBean exporter = (OsgiServiceFactoryBean) appContext.getBean("&manyOptions");

		assertTrue(Arrays.equals(new Class<?>[] { Serializable.class, CharSequence.class }, getInterfaces(exporter)));
		Properties prop = new Properties();
		prop.setProperty("foo", "bar");
		prop.setProperty("white", "horse");
		assertEquals(prop, exporter.getServiceProperties());

		// Should be wrapped with a TCCL setting proxy
		System.out.println(getServiceAtIndex(1));
		assertNotSame(appContext.getBean("string"), getServiceAtIndex(1));

		assertEquals("string", getTargetBeanName(exporter));
		//assertEquals(appContext.getBean("string"), getTarget(exporter));
	}

	@Test
	public void testNestedService() {
		OsgiServiceFactoryBean exporter = (OsgiServiceFactoryBean) appContext.getBean("&nestedService");
		assertTrue(Arrays.equals(new Class<?>[] { Object.class }, getInterfaces(exporter)));

		Object service = getServiceAtIndex(2);
		assertSame(HashMap.class, service.getClass());

		assertNull(getTargetBeanName(exporter));
		assertNotNull(getTarget(exporter));
	}

	@Test
	public void testServiceExporterFactoryBean() {
		Object bean = appContext.getBean("nestedService");
		assertTrue(bean instanceof ServiceRegistration);
		assertNotSame("registration not wrapped to provide exporting listener notification", registration, bean);
	}

	@Test
	public void testServiceProperties() {
		OsgiServiceFactoryBean exporter = (OsgiServiceFactoryBean) appContext.getBean("&serviceProperties");
		Map properties = exporter.getServiceProperties();
		assertEquals(2, properties.size());
		assertTrue(properties.get("string") instanceof String);
		assertTrue(properties.get("int") instanceof Integer);

		assertNull(getTargetBeanName(exporter));
		assertNotNull(getTarget(exporter));

	}

	@Test
	public void testListeners() {
		OsgiServiceFactoryBean exporter = (OsgiServiceFactoryBean) appContext.getBean("&exporterWithListener");
		OsgiServiceRegistrationListener[] listeners = getListeners(exporter);
		assertEquals(2, listeners.length);
	}

	@Test
	public void testListenersInvoked() throws Exception {
		// registration should have been already called
		assertEquals(2, RegistrationListener.BIND_CALLS);

		Object target = appContext.getBean("exporterWithListener");
		assertTrue(target instanceof ServiceRegistration);

		assertEquals(0, RegistrationListener.UNBIND_CALLS);
		unregister((ServiceRegistration) target);
		assertEquals(2, RegistrationListener.UNBIND_CALLS);
		assertNotNull(RegistrationListener.SERVICE_REG);
		assertNotNull(RegistrationListener.SERVICE_UNREG);
		assertSame(RegistrationListener.SERVICE_REG, RegistrationListener.SERVICE_UNREG);
	}

	@Test
	public void testFBWithCustomListeners() {
		OsgiServiceFactoryBean exporter = (OsgiServiceFactoryBean) appContext.getBean("&exporterWithCustomListener");
		OsgiServiceRegistrationListener[] listeners = getListeners(exporter);
		assertEquals(1, listeners.length);
	}

	@Test
	public void testCustomListenerInvoked() throws Exception {
		// registration should have been already called (service already
		// published)
		assertEquals(1, CustomRegistrationListener.REG_CALLS);

		Object target = appContext.getBean("exporterWithCustomListener");

		assertTrue(target instanceof ServiceRegistration);
		assertEquals(0, CustomRegistrationListener.UNREG_CALLS);
		unregister((ServiceRegistration) target);
		assertEquals(1, CustomRegistrationListener.UNREG_CALLS);
		// check service instance passed around
		assertSame(appContext.getBean("string"), CustomRegistrationListener.SERVICE_REG);
		assertSame(appContext.getBean("string"), CustomRegistrationListener.SERVICE_UNREG);
	}

	private OsgiServiceRegistrationListener[] getListeners(OsgiServiceFactoryBean exporter) {
		return (OsgiServiceRegistrationListener[]) TestUtils.getFieldValue(exporter, "listeners");
	}

	private Class<?>[] getInterfaces(OsgiServiceFactoryBean exporter) {
		return (Class[]) TestUtils.getFieldValue(exporter, "interfaces");
	}

	private String getTargetBeanName(OsgiServiceFactoryBean exporter) {
		return (String) TestUtils.getFieldValue(exporter, "targetBeanName");
	}

	private Object getTarget(OsgiServiceFactoryBean exporter) {
		return TestUtils.getFieldValue(exporter, "target");
	}

	private void unregister(ServiceRegistration target) throws Exception {
		Field fld = target.getClass().getDeclaredField("delegate");
		fld.setAccessible(true);
		ServiceRegistration reg = (ServiceRegistration) fld.get(target);
		reg.unregister();
	}
}