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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
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
public class NestedReferencesTest {

	public static class Holder {
		private Object data;

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}
	}

	private GenericApplicationContext appContext;

	@Before
	public void setup() throws Exception {

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
		reader.loadBeanDefinitions(new ClassPathResource("osgiReferenceNestedBeans.xml", getClass()));
		appContext.refresh();
	}

	@After
	public void tearDown() throws Exception {
		appContext.close();
	}

	@Test
	public void testNestedBeansMadeTopLevel() throws Exception {
		// 5 top-level plus 4 promoted beans
		assertEquals(5 + 4, appContext.getBeanDefinitionCount());
	}

	@Test
	public void testNestedReferenceWithName() {
		Object bean = appContext.getBean("satriani#org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean#0");
		Holder holder = (Holder) appContext.getBean("nestedNamedReference", Holder.class);
		assertSame(bean, holder.data);
	}

	@Test
	public void testNestedReferenceWithoutName() throws Exception {
		Object bean = appContext.getBean("org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean#0");
		Holder holder = (Holder) appContext.getBean("nestedAnonymousReference", Holder.class);
		assertSame(bean, holder.data);
	}

	@Test
	public void testNestedCollectionWithName() throws Exception {
		Object bean = appContext.getBean("dire-straits#org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean#0");
		Holder holder = (Holder) appContext.getBean("nestedNamedCollection", Holder.class);
		assertSame(bean, holder.data);
	}

	@Test
	public void testNesteCollectionWithoutName() throws Exception {
		Object bean = appContext.getBean("org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean#0");
		Holder holder = (Holder) appContext.getBean("nestedAnonymousCollection", Holder.class);
		assertSame(bean, holder.data);
	}
}