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

import java.io.Serializable;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.internal.support.RetryTemplate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * @author Costin Leau
 * 
 */
public class OsgiDefaultsTests extends TestCase {

	private GenericApplicationContext appContext;

	protected void setUp() throws Exception {
		BundleContext bundleContext = new MockBundleContext() {
			// service reference already registered
			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[] { new MockServiceReference(new String[] { Serializable.class.getName() }) };
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		reader.loadBeanDefinitions(new ClassPathResource("osgiDefaults.xml", getClass()));
		appContext.refresh();
	}

	protected void tearDown() throws Exception {
		appContext.close();
		appContext = null;
	}

	public void testLocalDefinitionForTimeout() throws Exception {
		OsgiServiceProxyFactoryBean fb = (OsgiServiceProxyFactoryBean) appContext.getBean("&refWLocalConfig");
		assertEquals(55, getTimeout(fb));
	}

	public void testLocalDefinitionForCardinalityOnMultiImporter() throws Exception {
		OsgiServiceCollectionProxyFactoryBean fb =
				(OsgiServiceCollectionProxyFactoryBean) appContext.getBean("&colWLocalConfig");
		assertEquals(Availability.MANDATORY, getCardinality(fb));
	}

	public void testLocalDefinitionForCardinalityOnSingleImporter() throws Exception {
		OsgiServiceProxyFactoryBean fb = (OsgiServiceProxyFactoryBean) appContext.getBean("&refWLocalConfig");
		assertEquals(Availability.MANDATORY, getCardinality(fb));
	}

	public void testTimeoutDefault() throws Exception {
		OsgiServiceProxyFactoryBean fb = (OsgiServiceProxyFactoryBean) appContext.getBean("&refWDefaults");
		assertEquals("default osgi timeout not applied", 10, getTimeout(fb));
	}

	public void testCardinalityDefaultOnSingleImporter() throws Exception {
		OsgiServiceProxyFactoryBean fb = (OsgiServiceProxyFactoryBean) appContext.getBean("&refWDefaults");
		assertEquals(Availability.OPTIONAL, getCardinality(fb));
	}

	public void testCardinalityDefaultOnMultiImporter() throws Exception {
		OsgiServiceCollectionProxyFactoryBean fb =
				(OsgiServiceCollectionProxyFactoryBean) appContext.getBean("&colWDefaults");
		assertEquals(Availability.OPTIONAL, getCardinality(fb));
	}

	public void testNormalBeanInjection() throws Exception {
		appContext.getBean("nestedURLValue");
	}
	
	private Availability getCardinality(Object obj) {
		return (Availability) TestUtils.getFieldValue(obj, "availability");
	}

	private long getTimeout(Object obj) {
		return ((RetryTemplate) TestUtils.getFieldValue(obj, "retryTemplate")).getWaitTime();
	}
}