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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.compendium.internal.cm.ManagedServiceFactoryFactoryBean;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.service.exporter.support.DefaultInterfaceDetector;
import org.eclipse.gemini.blueprint.service.exporter.support.ExportContextClassLoaderEnum;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * Parsing test for ManagedServiceFactory/<managed-service-factory/>
 * 
 * @author Costin Leau
 */
public class ManagedServiceFactoryTest extends TestCase {

	private GenericApplicationContext appContext;

	protected void setUp() throws Exception {

		MockControl mc = MockControl.createNiceControl(Configuration.class);
		final Configuration cfg = (Configuration) mc.getMock();
		mc.expectAndReturn(cfg.getProperties(), new Properties());
		mc.replay();

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

	protected void tearDown() throws Exception {
		appContext.close();
		appContext = null;
	}

	public void testBasicParsing() throws Exception {
		Object factory = appContext.getBean("&simple");
		assertTrue(factory instanceof ManagedServiceFactoryFactoryBean);
	}

	public void testBasicExportAttributes() throws Exception {
		Object factory = appContext.getBean("&simple");
		Object intfs = TestUtils.getFieldValue(factory, "interfaces");
		assertTrue(Arrays.equals((Object[]) intfs, new Class<?>[] { Object.class }));
		Object autoExport = TestUtils.getFieldValue(factory, "detector");
		assertEquals(DefaultInterfaceDetector.ALL_CLASSES, autoExport);
	}

	public void testNestedInterfaceElement() throws Exception {
		Object factory = appContext.getBean("&ccl");
		Object intfs = TestUtils.getFieldValue(factory, "interfaces");
		assertTrue(Arrays.equals((Object[]) intfs, new Class<?>[] { Map.class, Serializable.class }));
	}

	public void testCCLAttribute() throws Exception {
		Object factory = appContext.getBean("&ccl");
		Object ccl = TestUtils.getFieldValue(factory, "ccl");
		assertEquals(ExportContextClassLoaderEnum.SERVICE_PROVIDER, ccl);
	}

	public void testContainerUpdateAttr() throws Exception {
		Object factory = appContext.getBean("&container-update");
		Object strategy = TestUtils.getFieldValue(factory, "autowireOnUpdate");
		assertEquals(true, strategy);
	}

	public void testBeanManagedUpdateAttr() throws Exception {
		Object factory = appContext.getBean("&bean-update");
		Object strategy = TestUtils.getFieldValue(factory, "autowireOnUpdate");
		Object method = TestUtils.getFieldValue(factory, "updateMethod");
		assertEquals(false, strategy);
		assertEquals("update", method);
	}
}