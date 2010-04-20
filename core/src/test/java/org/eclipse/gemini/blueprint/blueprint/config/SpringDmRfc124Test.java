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

package org.eclipse.gemini.blueprint.blueprint.config;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
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
public class SpringDmRfc124Test extends TestCase {

	private static final String CONFIG = "spring-dm-rfc124.xml";

	private GenericApplicationContext context;
	private XmlBeanDefinitionReader reader;


	protected void setUp() throws Exception {
		BundleContext bundleContext = new MockBundleContext() {

			// service reference already registered
			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[] { new MockServiceReference(new String[] { Cloneable.class.getName() }) };
			}
		};

		context = new GenericApplicationContext();
		context.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		context.setClassLoader(getClass().getClassLoader());

		reader = new XmlBeanDefinitionReader(context);
		reader.loadBeanDefinitions(new ClassPathResource(CONFIG, getClass()));
		context.refresh();
	}

	protected void tearDown() throws Exception {
		context.close();
		context = null;
	}

	public void testContainerSanity() throws Exception {
		System.out.println(Arrays.toString(context.getBeanDefinitionNames()));
		assertEquals(5, context.getBeanDefinitionCount());
	}
}
