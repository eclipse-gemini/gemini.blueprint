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

package org.eclipse.gemini.blueprint.iandt.importer;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.ServiceRegistration;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;

/**
 * @author Costin Leau
 * 
 */
public class ServiceComparatorTest extends BaseIntegrationTest {

	private ServiceRegistration registration1, registration2, reg3;

	private Object service1, service2, service3;

	/** load context on each test run */
	private ConfigurableOsgiBundleApplicationContext context;


	public static interface MyInterface extends Comparable, Serializable, Cloneable {

		String value();
	}

	static class MyClass implements MyInterface, Serializable, Cloneable {

		private String value;


		protected Object clone() throws CloneNotSupportedException {
			return null;
		}

		public int compareTo(Object o) {
			MyInterface other = (MyInterface) o;
			return value.compareTo(other.value());
		}

		public String value() {
			return value;
		}

		MyClass(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}

		public boolean equals(Object obj) {
			if (obj instanceof MyInterface)
				return this.compareTo(obj) == 0;
			return false;
		}

	}

	static class TestBean {

		private Serializable prop1;
		private Cloneable prop2;
		private MyInterface prop3;


		public void setProp1(Serializable prop1) {
			this.prop1 = prop1;
		}

		public void setProp2(Cloneable prop2) {
			this.prop2 = prop2;
		}

		public void setProp3(MyInterface prop3) {
			this.prop3 = prop3;
		}

		public Serializable getProp1() {
			return prop1;
		}

		public Cloneable getProp2() {
			return prop2;
		}

		public MyInterface getProp3() {
			return prop3;
		}
	}


	protected void onSetUp() throws Exception {
		// publish service
		String[] intfs = new String[] { MyInterface.class.getName(), Serializable.class.getName(),
			Cloneable.class.getName() };

		service1 = new MyClass("abc");
		service2 = new MyClass("xyz");
		service3 = new MyClass("mnp");

		// register the services
		registration1 = bundleContext.registerService(intfs, service1, null);
		registration2 = bundleContext.registerService(intfs, service2, null);
		reg3 = bundleContext.registerService(intfs, service3, null);

		context = loadAppContext();
	}

	protected void onTearDown() throws Exception {
		context.close();
		OsgiServiceUtils.unregisterService(registration1);
		OsgiServiceUtils.unregisterService(registration2);
		OsgiServiceUtils.unregisterService(reg3);
	}

	private ConfigurableOsgiBundleApplicationContext loadAppContext() {
		OsgiBundleXmlApplicationContext appContext = new OsgiBundleXmlApplicationContext(
			new String[] { "/org/eclipse/gemini/blueprint/iandt/importer/importer-ordering.xml" });
		appContext.setBundleContext(bundleContext);
		appContext.refresh();
		return appContext;
	}

	public void testComparableImportedObjects() throws Exception {
		assertNotNull(context);
		assertTrue("service 1 is greater then service2", ((Comparable) service1).compareTo(service2) < 0);

		Set set = (Set) context.getBean("setWithServiceOrder");
		assertEquals(3, set.size());
		Iterator iter = set.iterator();

		System.out.println(set);
		assertEquals(service1, iter.next());
		assertEquals(service3, iter.next());
		assertEquals(service2, iter.next());
		assertTrue(service1 instanceof Serializable);
	}

	public void testServiceReferenceOrderingOnImportedObjects() throws Exception {
		Set set = (Set) context.getBean("setWithServiceReference");
		assertNotNull(set);
		assertEquals(3, set.size());
		Iterator iter = set.iterator();

		assertEquals(service3, iter.next());
		assertEquals(service2, iter.next());
		assertEquals(service1, iter.next());
	}

	public void testReferenceInjection() throws Exception {
		TestBean bean = (TestBean) context.getBean("testBean");
		assertNotNull(bean.getProp1());
		assertNotNull(bean.getProp2());
		assertNotNull(bean.getProp3());
		assertSame(bean.getProp1(), bean.getProp2());
		assertSame(bean.getProp2(), bean.getProp3());
	}
}
