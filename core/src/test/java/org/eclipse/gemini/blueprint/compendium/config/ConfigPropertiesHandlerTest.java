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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.service.exporter.support.ServicePropertiesChangeEvent;
import org.eclipse.gemini.blueprint.service.exporter.support.ServicePropertiesChangeListener;
import org.eclipse.gemini.blueprint.service.exporter.support.ServicePropertiesListenerManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * @author Costin Leau
 */
public class ConfigPropertiesHandlerTest extends TestCase {

	private GenericApplicationContext appContext;

	private BundleContext bundleContext;

	private MockControl adminControl;

	private ConfigurationAdmin admin;

	private Dictionary<String, String> config;

	private String persistentId = "foo.bar";
	private Configuration cfg;
	private ManagedService msCallback;


	protected void setUp() throws Exception {

		adminControl = MockControl.createControl(ConfigurationAdmin.class);
		admin = (ConfigurationAdmin) adminControl.getMock();
		MockControl configMock = MockControl.createControl(Configuration.class);
		cfg = (Configuration) configMock.getMock();

		config = new Hashtable<String, String>();

		adminControl.expectAndReturn(admin.getConfiguration(persistentId), cfg, MockControl.ONE_OR_MORE);
		configMock.expectAndReturn(cfg.getProperties(), config, MockControl.ONE_OR_MORE);

		adminControl.replay();
		configMock.replay();

		bundleContext = new MockBundleContext() {

			// add Configuration admin support
			@Override
			public Object getService(ServiceReference reference) {
				return admin;
			}

			// ManagedService registration
			@Override
			public ServiceRegistration registerService(String clazz, Object service, Dictionary properties) {
				// save the callback
				if (ManagedService.class.getName().equals(clazz)) {
					msCallback = (ManagedService) service;
				}
				return super.registerService(clazz, service, properties);
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		// reader.setEventListener(this.listener);
		reader.loadBeanDefinitions(new ClassPathResource("configProperties.xml", getClass()));
		appContext.refresh();
	}

	protected void tearDown() throws Exception {
		adminControl.verify();
	}

	public void testPropertiesLazyInit() throws Exception {
		adminControl.reset();
		adminControl.replay();
	}

	public void testBlankConfigProperties() throws Exception {
		config.put("Spring", "Source");
		Object bean = appContext.getBean("named");
		assertTrue(bean instanceof Properties);
		assertEquals(config, bean);
	}

	public void testPropertiesWithDefaultsAndNoOverride() throws Exception {
		persistentId = "noLocalOverride";

		adminControl.reset();
		adminControl.expectAndReturn(admin.getConfiguration(persistentId), cfg, MockControl.ONE_OR_MORE);
		adminControl.replay();

		config.put("foo", "foo");
		config.put("Spring", "Source");
		Object bean = appContext.getBean(persistentId);
		assertTrue(bean instanceof Properties);
		Properties props = (Properties) bean;
		assertFalse(config.equals(bean));
		// the local property has been replaced
		assertEquals("foo", props.getProperty("foo"));
		// but the one not present on the CM are still present
		assertTrue(props.containsKey("kry"));
		assertTrue(props.containsKey("Spring"));
		assertEquals(3, props.entrySet().size());
	}

	public void testPropertiesWithDefaultsAndOverride() throws Exception {
		persistentId = "localOverride";

		adminControl.reset();
		adminControl.expectAndReturn(admin.getConfiguration(persistentId), cfg, MockControl.ONE_OR_MORE);
		adminControl.replay();

		config.put("foo", "foo");
		config.put("Spring", "Source");
		Object bean = appContext.getBean(persistentId);
		assertTrue(bean instanceof Properties);
		Properties props = (Properties) bean;
		assertFalse(config.equals(bean));
		// the local property is still present
		assertEquals("bar", props.getProperty("foo"));
		// the CM props are still there
		assertTrue(props.containsKey("kry"));
		// and so are the local props
		assertTrue(props.containsKey("Spring"));
		assertEquals(3, props.entrySet().size());
	}

	// disabled until custom attributes are enabled again
	public void tstPropertiesWithPropRef() throws Exception {
		persistentId = "custom-attributes";

		adminControl.reset();
		adminControl.expectAndReturn(admin.getConfiguration(persistentId), cfg, MockControl.ONE_OR_MORE);
		adminControl.replay();

		config.put("foo", "foo");
		config.put("Spring", "Source");
		Object bean = appContext.getBean(persistentId);
		BeanDefinition bd = appContext.getBeanDefinition(persistentId);
		System.out.println(bd.getScope());
		assertTrue(bean instanceof Properties);
		Properties props = (Properties) bean;
		assertFalse(config.equals(bean));
		// the local property is still present
		assertEquals("bar", props.getProperty("foo"));
		// the CM props are still there
		assertTrue(props.containsKey("kry"));
		// and so are the local props
		assertTrue(props.containsKey("Spring"));
		assertEquals(3, props.entrySet().size());
	}

	public void testDynamicNoOverride() throws Exception {
		persistentId = "noLocalOverride";
		String beanId = "dynamic-noOverride";

		adminControl.reset();
		adminControl.expectAndReturn(admin.getConfiguration(persistentId), cfg, MockControl.ONE_OR_MORE);
		adminControl.replay();

		// initial config
		config.put("bo", "bozo");
		config.put("Spring", "Source");
		Object bean = appContext.getBean(beanId);
		assertTrue(bean instanceof Properties);
		Properties props = (Properties) bean;
		assertFalse(config.equals(bean));
		// the local property has been replaced
		assertEquals("bozo", props.getProperty("bo"));
		// but the one not present on the CM are still present
		assertTrue(props.containsKey("kry"));
		assertEquals("pton", props.getProperty("kry"));
		assertEquals("Source", props.getProperty("Spring"));
		assertEquals(3, props.entrySet().size());

		// CM updates
		assertNotNull(msCallback);

		Dictionary<String, String> newProps = new Hashtable<String, String>();
		newProps.put("bo", "b0z0");
		newProps.put("new", "prop");
		// trigger update
		msCallback.updated(newProps);

		// verify properties
		assertSame(props, appContext.getBean(beanId));
		assertEquals("b0z0", props.getProperty("bo"));
		assertEquals("prop", props.getProperty("new"));
		assertNull(props.getProperty("Spring"));
		// verify local properties
		assertEquals("pton", props.getProperty("kry"));
	}

	public void testDynamicOverride() throws Exception {
		persistentId = "localOverride";
		String beanId = "dynamic-override";

		adminControl.reset();
		adminControl.expectAndReturn(admin.getConfiguration(persistentId), cfg, MockControl.ONE_OR_MORE);
		adminControl.replay();

		// initial config
		config.put("bo", "bozo");
		config.put("Spring", "Source");
		Object bean = appContext.getBean(beanId);
		assertTrue(bean instanceof Properties);
		Properties props = (Properties) bean;
		assertFalse(config.equals(bean));
		// the local property has been replaced
		assertEquals("zo", props.getProperty("bo"));
		// but the one not present on the CM are still present
		assertEquals("pton", props.getProperty("kry"));
		assertEquals("Source", props.getProperty("Spring"));
		assertEquals(3, props.entrySet().size());

		// CM updates
		assertNotNull(msCallback);

		Dictionary<String, String> newProps = new Hashtable<String, String>();
		newProps.put("bo", "b0z0");
		newProps.put("new", "prop");
		// trigger update
		msCallback.updated(newProps);

		// verify properties
		assertSame(props, appContext.getBean(beanId));
		assertEquals("zo", props.getProperty("bo"));
		assertEquals("prop", props.getProperty("new"));
		assertNull(props.getProperty("Spring"));
		// verify local properties
		assertEquals("pton", props.getProperty("kry"));
	}

	public void testExtendedProperties() throws Exception {
		persistentId = "noLocalOverride";
		String beanId = "dynamic-noOverride";

		adminControl.reset();
		adminControl.expectAndReturn(admin.getConfiguration(persistentId), cfg, MockControl.ONE_OR_MORE);
		adminControl.replay();

		// initial config
		config.put("bo", "bozo");
		config.put("Spring", "Source");
		Object bean = appContext.getBean(beanId);
		assertTrue(bean instanceof Properties);
		assertTrue(bean instanceof ServicePropertiesListenerManager);
		Properties props = (Properties) bean;
		ServicePropertiesListenerManager manager = (ServicePropertiesListenerManager) bean;

		final Map<?, ?>[] updatedProps = new Map<?, ?>[1];
		manager.addListener(new ServicePropertiesChangeListener() {

			public void propertiesChange(ServicePropertiesChangeEvent event) {
				updatedProps[0] = event.getServiceProperties();
			}
		});

		// CM updates
		assertNotNull(msCallback);

		Dictionary<String, String> newProps = new Hashtable<String, String>();
		newProps.put("bo", "b0z0");
		newProps.put("new", "prop");
		// trigger update
		msCallback.updated(newProps);

		// verify listener properties
		assertNotNull(updatedProps[0]);
		// the properties contains both the CM props
		assertEquals("b0z0", updatedProps[0].get("bo"));
		assertEquals("prop", updatedProps[0].get("new"));
		// and the local defined ones
		assertEquals("pton", updatedProps[0].get("kry"));
		assertEquals(3, updatedProps[0].size());
	}
}