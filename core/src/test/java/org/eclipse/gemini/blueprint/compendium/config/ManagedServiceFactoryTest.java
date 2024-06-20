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

package org.eclipse.gemini.blueprint.compendium.config;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.compendium.internal.cm.ManagedServiceFactoryFactoryBean;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.service.exporter.support.DefaultInterfaceDetector;
import org.eclipse.gemini.blueprint.service.exporter.support.ExportContextClassLoaderEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Parsing test for ManagedServiceFactory/<managed-service-factory/>
 * 
 * @author Costin Leau
 */
public class ManagedServiceFactoryTest {

	private GenericApplicationContext appContext;

	@Before
	public void setup() throws Exception {


		final Configuration cfg = createMock(Configuration.class);
		expect(cfg.getProperties()).andReturn(new Hashtable<String, Object>());
		replay(cfg);

		BundleContext bundleContext = new MockBundleContext() {

			// always return a ConfigurationAdmin
			public Object getService(ServiceReference reference) {
				return new MockConfigurationAdmin() {

					public Configuration getConfiguration(String pid) throws IOException {
						return cfg;
					}
				};
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		reader.loadBeanDefinitions(new ClassPathResource("managedServiceFactory.xml", getClass()));
		appContext.refresh();
	}

	@After
	public void tearDown() throws Exception {
		appContext.close();
		appContext = null;
	}

	@Test
	public void testBasicParsing() throws Exception {
		Object factory = appContext.getBean("&simple");
		assertTrue(factory instanceof ManagedServiceFactoryFactoryBean);
	}

	@Test
	public void testBasicExportAttributes() throws Exception {
		Object factory = appContext.getBean("&simple");
		Object intfs = TestUtils.getFieldValue(factory, "interfaces");
		assertTrue(Arrays.equals((Object[]) intfs, new Class<?>[] { Object.class }));
		Object autoExport = TestUtils.getFieldValue(factory, "detector");
		assertEquals(DefaultInterfaceDetector.ALL_CLASSES, autoExport);
	}

	@Test
	public void testNestedInterfaceElement() throws Exception {
		Object factory = appContext.getBean("&ccl");
		Object intfs = TestUtils.getFieldValue(factory, "interfaces");
		assertTrue(Arrays.equals((Object[]) intfs, new Class<?>[] { Map.class, Serializable.class }));
	}

	@Test
	public void testCCLAttribute() throws Exception {
		Object factory = appContext.getBean("&ccl");
		Object ccl = TestUtils.getFieldValue(factory, "ccl");
		assertEquals(ExportContextClassLoaderEnum.SERVICE_PROVIDER, ccl);
	}

	@Test
	public void testContainerUpdateAttr() throws Exception {
		Object factory = appContext.getBean("&container-update");
		Object strategy = TestUtils.getFieldValue(factory, "autowireOnUpdate");
		assertEquals(true, strategy);
	}

	@Test
	public void testBeanManagedUpdateAttr() throws Exception {
		Object factory = appContext.getBean("&bean-update");
		Object strategy = TestUtils.getFieldValue(factory, "autowireOnUpdate");
		Object method = TestUtils.getFieldValue(factory, "updateMethod");
		assertEquals(false, strategy);
		assertEquals("update", method);
	}
}