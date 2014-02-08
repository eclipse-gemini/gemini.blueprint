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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;
import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.compendium.internal.cm.ManagedServiceInstanceTrackerPostProcessor;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ManagedService;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceRegistration;

/**
 * @author Costin Leau
 * 
 */
public class ManagedPropertiesTest extends TestCase {

	private GenericApplicationContext appContext;
	private int unregistrationCounter;
	private int registrationCounter;

	protected void setUp() throws Exception {

		final Configuration cfg = createNiceMock(Configuration.class);
        expect(cfg.getProperties()).andReturn(new Hashtable<String, Object>());
        replay(cfg);

		registrationCounter = 0;
		unregistrationCounter = 0;

		BundleContext bundleContext = new MockBundleContext() {

			// always return a ConfigurationAdmin
			public Object getService(ServiceReference reference) {
				return new MockConfigurationAdmin() {

					public Configuration getConfiguration(String pid) throws IOException {
						return cfg;
					}
				};
			}

			public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
				if (service instanceof ManagedService) {
					registrationCounter++;
					return new MockServiceRegistration(clazzes, properties) {

						public void unregister() {
							super.unregister();
							unregistrationCounter++;
						}
					};
				}
				return super.registerService(clazzes, service, properties);
			}

		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		reader.loadBeanDefinitions(new ClassPathResource("managedService.xml", getClass()));
		appContext.refresh();
	}

	protected void tearDown() throws Exception {
		appContext.close();
		appContext = null;
	}

	private ManagedServiceInstanceTrackerPostProcessor getTrackerForBean(String beanName) {
		return (ManagedServiceInstanceTrackerPostProcessor) appContext
				.getBean(ManagedServiceInstanceTrackerPostProcessor.class.getName() + "#0#" + beanName);
	}

	public void testSimpleBeanTrackingBpp() throws Exception {
		ManagedServiceInstanceTrackerPostProcessor bpp = getTrackerForBean("simple");
		assertEquals("simple", TestUtils.getFieldValue(bpp, "pid"));
		assertNull(TestUtils.getFieldValue(bpp, "updateMethod"));
		assertNull(TestUtils.getFieldValue(bpp, "updateStrategy"));
	}

	public void testSimpleBeanWithNoNameTrackingBpp() throws Exception {
		ManagedServiceInstanceTrackerPostProcessor bpp =
				getTrackerForBean("org.eclipse.gemini.blueprint.compendium.OneSetter#0");
		assertEquals("non-name", TestUtils.getFieldValue(bpp, "pid"));
		assertNull(TestUtils.getFieldValue(bpp, "updateMethod"));
		assertNull(TestUtils.getFieldValue(bpp, "updateStrategy"));
	}

	public void testSimpleWUpdateBeanTrackingBpp() throws Exception {
		ManagedServiceInstanceTrackerPostProcessor bpp = getTrackerForBean("simpleWUpdate");
		assertEquals("simple", TestUtils.getFieldValue(bpp, "pid"));
		assertNull(TestUtils.getFieldValue(bpp, "updateMethod"));
	}

	public void testMultipleWUpdateBeanTrackingBpp() throws Exception {
		ManagedServiceInstanceTrackerPostProcessor bpp = getTrackerForBean("multipleWUpdate");
		assertEquals("multiple", TestUtils.getFieldValue(bpp, "pid"));
		assertNull(TestUtils.getFieldValue(bpp, "updateMethod"));
		assertEquals(true, TestUtils.getFieldValue(bpp, "autowireOnUpdate"));
	}

	public void testBeanManagedTrackingBpp() throws Exception {
		ManagedServiceInstanceTrackerPostProcessor bpp = getTrackerForBean("beanManaged");
		assertEquals("bean-managed", TestUtils.getFieldValue(bpp, "pid"));
		assertEquals("update", TestUtils.getFieldValue(bpp, "updateMethod"));
		assertEquals(false, TestUtils.getFieldValue(bpp, "autowireOnUpdate"));
	}
	
	public void testMixedManagedTrackingBpp() throws Exception {
		ManagedServiceInstanceTrackerPostProcessor bpp = getTrackerForBean("mixedManaged");
		assertEquals("bean-managed", TestUtils.getFieldValue(bpp, "pid"));
		assertEquals("update", TestUtils.getFieldValue(bpp, "updateMethod"));
		assertEquals(true, TestUtils.getFieldValue(bpp, "autowireOnUpdate"));
	}

	public void testTrackingCleanup() throws Exception {
		assertEquals(6, registrationCounter);
		assertEquals(0, unregistrationCounter);
		appContext.close();
		assertEquals(6, unregistrationCounter);
	}
}