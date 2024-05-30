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

package org.eclipse.gemini.blueprint.config.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.eclipse.gemini.blueprint.config.internal.adapter.OsgiServiceLifecycleListenerAdapter;
import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.eclipse.gemini.blueprint.service.importer.ServiceReferenceProxy;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.StaticServiceReferenceProxy;
import org.eclipse.gemini.blueprint.util.internal.MapBasedDictionary;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * @author Costin Leau
 */
public class OsgiServiceLifecycleListenerAdapterTest {

	protected static class JustListener implements OsgiServiceLifecycleListener {

		public static int BIND_CALLS = 0;

		public static int UNBIND_CALLS = 0;

		public void bind(Object service, Map properties) throws Exception {
			BIND_CALLS++;
		}

		public void unbind(Object service, Map properties) throws Exception {
			UNBIND_CALLS++;
		}
	}

	protected static class CustomListener {

		public static int BIND_CALLS = 0;

		public static int UNBIND_CALLS = 0;

		public static List BIND_SERVICES = new ArrayList();

		public static List UNBIND_SERVICES = new ArrayList();

		public void myBind(Object service, Map properties) {
			BIND_CALLS++;
			BIND_SERVICES.add(service);

		}

		public void myUnbind(Object service, Map properties) {
			UNBIND_CALLS++;
			UNBIND_SERVICES.add(service);

		}

		public void wrongBind() {
			BIND_CALLS++;
		}

		public void wrongUnbind() {
			UNBIND_CALLS--;
		}
	}

	protected static class CustomAndListener extends JustListener {

		public Integer aBind(Object service, Map props) throws Exception {
			super.bind(service, props);
			return null;
		}

		public void aUnbind(Object service, Map props) throws Exception {
			super.unbind(service, props);
		}
	}

	protected static class OverloadedCustomMethods extends CustomListener {

		public void myBind(Date service, Map properties) {
			super.myBind(service, properties);
		}

		public void myUnbind(String service, Map properties) {
			super.myUnbind(service, properties);
		}
	}

	/**
	 * Override standard methods with ones that throw exceptions.
	 * 
	 * @author Costin Leau
	 */
	protected static class ExceptionListener extends CustomAndListener {

		public void bind(Object service, Map properties) throws Exception {
			throw new Exception("expected!") {

				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}

		public void unbind(Object service, Map properties) throws Exception {
			throw new IOException("expected!") {

				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}
	}

	protected static class ExceptionCustomListener extends CustomListener {

		public void myBind(Date service, Map properties) {
			throw (RuntimeException) new RuntimeException("expected!") {

				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}

		public void myUnbind(String service, Map properties) throws IOException {
			throw new IOException("expected!") {

				public synchronized Throwable fillInStackTrace() {
					return null;
				}
			};
		}
	}

	// piggy backs on JustListener static fields
	protected static class DictionaryAndMapCustomListener {

		public void bind(Object service, Dictionary properties) {
			JustListener.BIND_CALLS++;
		}

		public void unbind(Object service, Map props) {
			JustListener.UNBIND_CALLS++;
		}

		public void unbind(Object service, Dictionary props) throws Exception {
			JustListener.UNBIND_CALLS++;
		}
	}

	protected static class OverridingMethodListener extends CustomListener {

		public void myBind(Object service, Map properties) {
			BIND_CALLS++;
		}

		public void myUnbind(Object service, Map properties) {
			UNBIND_CALLS++;
		}
	}

	protected static class JustBind {

		public void myBind(Object service, Map properties) {
			JustListener.BIND_CALLS++;
		}
	}

	protected static class JustUnbind {

		public void myUnbind(Object service, Map properties) {
			JustListener.UNBIND_CALLS++;
		}
	}

	protected static class CustomServiceRefListener {

		private void myUnbind(ServiceReference ref) {
			JustListener.UNBIND_CALLS++;
		}

		private void myBind(ServiceReference ref) {
			JustListener.BIND_CALLS++;
		}
	}

	protected static class CustomServiceRefAndObjectWOMapListener {

		private void myUnbind(ServiceReference ref) {
			JustListener.UNBIND_CALLS++;
		}

		private void myBind(ServiceReference ref) {
			JustListener.BIND_CALLS++;
		}

		private void myBind(Object obj) {
			JustListener.BIND_CALLS++;
		}

	}

	private OsgiServiceLifecycleListenerAdapter listener;

	private static final String BEAN_NAME = "bla";

	@Before
	public void setup() throws Exception {
		JustListener.BIND_CALLS = 0;
		JustListener.UNBIND_CALLS = 0;
		OverloadedCustomMethods.BIND_SERVICES = new ArrayList();
		OverloadedCustomMethods.UNBIND_SERVICES = new ArrayList();

		CustomListener.BIND_CALLS = 0;
		CustomListener.UNBIND_CALLS = 0;
	}

	@After
	public void tearDown() throws Exception {
		listener = null;
		OverloadedCustomMethods.BIND_SERVICES = null;
		OverloadedCustomMethods.UNBIND_SERVICES = null;
	}

	@Test
	public void testWrapperOverListener() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new JustListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.afterPropertiesSet();

		assertEquals(0, JustListener.BIND_CALLS);
		listener.bind(null, null);
		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);
		listener.bind(null, null);
		assertEquals(2, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.unbind(null, null);
		assertEquals(1, JustListener.UNBIND_CALLS);
		assertEquals(2, JustListener.BIND_CALLS);
		listener.unbind(null, null);
		assertEquals(2, JustListener.UNBIND_CALLS);
		assertEquals(2, JustListener.BIND_CALLS);
	}

	@Test
	public void testWrapperOverNoInvalidClass() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setTargetBeanName(BEAN_NAME);
		listener.setTarget(new Object());
		listener.setBeanFactory(createMockBF(new Object()));

		try {
			listener.afterPropertiesSet();
			fail("should have thrown exception");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	@Test
	public void testWrapperWithIncorrectCustomMethodNames() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setTargetBeanName(BEAN_NAME);
		listener.setTarget(new Object());
		listener.setBindMethod("pop");
		listener.setUnbindMethod("corn");

		try {
			listener.afterPropertiesSet();
			fail("should have thrown exception");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	@Test
	public void testWrapperWithCorrectCustomMethodNamesButIncorrectArgumentTypes() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setTarget(new CustomListener());
		listener.setBindMethod("wrongBind");
		listener.setUnbindMethod("wrongUnbind");

		assertEquals(0, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);
		try {
			listener.afterPropertiesSet();
			fail("should have thrown exception");
		} catch (IllegalArgumentException ex) {
			// expected
		}

		assertEquals(0, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);
	}

	@Test
	public void testWrapperWithCustomMethods() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new CustomListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		Object service = new Object();
		assertEquals(0, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);
		listener.bind(new Object(), null);
		assertEquals(1, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);

		listener.bind(service, null);
		assertEquals(2, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);

		listener.unbind(service, null);
		assertEquals(2, CustomListener.BIND_CALLS);
		assertEquals(1, CustomListener.UNBIND_CALLS);

		listener.unbind(service, null);
		assertEquals(2, CustomListener.BIND_CALLS);
		assertEquals(2, CustomListener.UNBIND_CALLS);
	}

	@Test
	public void testWrapperWithCustomMethodsAndNullParameters() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new CustomListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		assertEquals(0, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);
		listener.bind(null, null);
		assertEquals("null services allowed", 1, CustomListener.BIND_CALLS);

		listener.unbind(null, null);

		assertEquals("null services allowed", 1, CustomListener.UNBIND_CALLS);
	}

	@Test
	public void testWrapperWithBothCustomAndInterfaceMethods() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new CustomAndListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("aBind");
		listener.setUnbindMethod("aUnbind");
		listener.afterPropertiesSet();

		Object service = new Object();

		assertEquals(0, CustomAndListener.BIND_CALLS);
		assertEquals(0, CustomAndListener.UNBIND_CALLS);
		listener.bind(service, null);
		assertEquals(2, CustomAndListener.BIND_CALLS);
		assertEquals(0, CustomAndListener.UNBIND_CALLS);

		listener.unbind(service, null);
		assertEquals(2, CustomAndListener.BIND_CALLS);
		assertEquals(2, CustomAndListener.UNBIND_CALLS);
	}

	@Test
	public void testWrapperWithCustomOverloadedMethodsAndDifferentServiceTypes() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new OverloadedCustomMethods()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		Object objService = new Object();
		Date dateService = new Date();
		String stringService = "token";

		assertEquals(0, OverloadedCustomMethods.BIND_CALLS);
		assertEquals(0, OverloadedCustomMethods.UNBIND_CALLS);
		listener.bind(objService, null);
		assertEquals("only one method accepts Object(s)", 1, OverloadedCustomMethods.BIND_CALLS);
		assertEquals(0, OverloadedCustomMethods.UNBIND_CALLS);

		listener.bind(dateService, null);
		assertEquals("two method accept Date(s)", 3, OverloadedCustomMethods.BIND_CALLS);
		assertEquals(0, OverloadedCustomMethods.UNBIND_CALLS);

		listener.unbind(stringService, null);
		assertEquals("two method accept Date(s)", 3, OverloadedCustomMethods.BIND_CALLS);
		assertEquals("two method accept String(s)", 2, OverloadedCustomMethods.UNBIND_CALLS);

		listener.unbind(objService, null);
		assertEquals("two method accept Date(s)", 3, OverloadedCustomMethods.BIND_CALLS);
		assertEquals("only one method accepts Object(s)", 3, OverloadedCustomMethods.UNBIND_CALLS);

		assertEquals(3, OverloadedCustomMethods.BIND_SERVICES.size());
		assertSame("incorrect call order", objService, OverloadedCustomMethods.BIND_SERVICES.get(0));
		assertSame("incorrect call order", dateService, OverloadedCustomMethods.BIND_SERVICES.get(1));
		assertSame("incorrect call order", dateService, OverloadedCustomMethods.BIND_SERVICES.get(2));

		assertEquals(3, OverloadedCustomMethods.UNBIND_SERVICES.size());
		assertSame("incorrect call order", stringService, OverloadedCustomMethods.UNBIND_SERVICES.get(0));
		assertSame("incorrect call order", stringService, OverloadedCustomMethods.UNBIND_SERVICES.get(1));
		assertSame("incorrect call order", objService, OverloadedCustomMethods.UNBIND_SERVICES.get(2));
	}

	@Test
	public void testExceptionOnListenerMethod() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new ExceptionListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("aBind");
		listener.setUnbindMethod("aUnbind");
		listener.afterPropertiesSet();

		Object service = new Object();
		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);
		listener.bind(service, null);
		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.unbind(service, null);
		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(1, JustListener.UNBIND_CALLS);
	}

	@Test
	public void testExceptionOnCustomMethods() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new ExceptionCustomListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		Date service = new Date();
		assertEquals(0, ExceptionCustomListener.BIND_CALLS);
		assertEquals(0, ExceptionCustomListener.UNBIND_CALLS);
		listener.bind(service, null);
		assertEquals("should have called overloaded method with type Object", 1, ExceptionCustomListener.BIND_CALLS);
		assertEquals(0, ExceptionCustomListener.UNBIND_CALLS);

		listener.unbind(service, null);
		assertEquals(1, ExceptionCustomListener.BIND_CALLS);
		assertEquals("should have called overloaded method with type Object", 1, ExceptionCustomListener.UNBIND_CALLS);
	}

	@Test
	public void testStandardListenerWithListeningMethodsSpecifiedAsCustomOnes() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new JustListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("bind");
		listener.setUnbindMethod("unbind");
		listener.afterPropertiesSet();

		Object service = null;
		assertEquals(0, JustListener.BIND_CALLS);
		listener.bind(service, null);
		// only the interface is being called since the service is null
		assertEquals(2, JustListener.BIND_CALLS);

		service = new Object();

		listener.bind(service, null);
		assertEquals(4, JustListener.BIND_CALLS);
	}

	@Test
	public void testListenerWithOverloadedTypesAndMultipleParameterTypes() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new DictionaryAndMapCustomListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("bind");
		listener.setUnbindMethod("unbind");
		listener.afterPropertiesSet();

		Object service = new Date();
		MapBasedDictionary props = new MapBasedDictionary();

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);
		listener.bind(service, props);

		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.unbind(service, props);
		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals("only one unbind method should be called", 1, JustListener.UNBIND_CALLS);
	}

	@Test
	public void testOverridingMethodsDiscovery() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new OverridingMethodListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		assertEquals(0, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);

		listener.bind(new Object(), null);
		assertEquals(1, CustomListener.BIND_CALLS);
		assertEquals(0, CustomListener.UNBIND_CALLS);

		listener.unbind(new Object(), null);
		assertEquals(1, CustomListener.BIND_CALLS);
		assertEquals(1, CustomListener.UNBIND_CALLS);

	}

	@Test
	public void testJustCustomBindMethod() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new JustBind()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.afterPropertiesSet();

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.bind(new Object(), null);

		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);
	}

	@Test
	public void testJustCustomUnbindMethod() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new JustUnbind()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.unbind(new Object(), null);

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(1, JustListener.UNBIND_CALLS);
	}

	@Test
	public void testCustomServiceRefBind() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new CustomServiceRefListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.afterPropertiesSet();

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.bind(new ImportedOsgiServiceProxyMock(), null);

		assertEquals(1, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);
	}

	@Test
	public void testCustomServiceRefUnbind() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new CustomServiceRefListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setUnbindMethod("myUnbind");
		listener.afterPropertiesSet();

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.unbind(new ImportedOsgiServiceProxyMock(), null);

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(1, JustListener.UNBIND_CALLS);
	}
	
	@Test
	public void testCustomServiceRefAndObjectWOMapBind() throws Exception {
		listener = new OsgiServiceLifecycleListenerAdapter();
		listener.setBeanFactory(createMockBF(new CustomServiceRefAndObjectWOMapListener()));
		listener.setTargetBeanName(BEAN_NAME);
		listener.setBindMethod("myBind");
		listener.afterPropertiesSet();

		assertEquals(0, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);

		listener.bind(new ImportedOsgiServiceProxyMock(), null);

		assertEquals(2, JustListener.BIND_CALLS);
		assertEquals(0, JustListener.UNBIND_CALLS);
	}
	

	private class ImportedOsgiServiceProxyMock implements ImportedOsgiServiceProxy {

		public Map getServiceProperties() {
			return new HashMap();
		}

		public ServiceReferenceProxy getServiceReference() {
			return new StaticServiceReferenceProxy(new MockServiceReference());
		}
	}

	private ConfigurableBeanFactory createMockBF(Object target) {
		ConfigurableBeanFactory cbf = createNiceMock(ConfigurableBeanFactory.class);

		expect(cbf.getBean(BEAN_NAME)).andReturn(target);
		expect(cbf.getType(BEAN_NAME)).andReturn((Class)target.getClass());

		replay(cbf);
		return cbf;
	}
}
