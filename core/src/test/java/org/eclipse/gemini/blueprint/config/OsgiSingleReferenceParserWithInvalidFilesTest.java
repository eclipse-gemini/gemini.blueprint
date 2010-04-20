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

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Costin Leau
 * 
 */
public class OsgiSingleReferenceParserWithInvalidFilesTest extends TestCase {
	private GenericApplicationContext appContext;

	protected void setUp() throws Exception {
		BundleContext bundleContext = new MockBundleContext() {
			// service reference already registered
			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[0];
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());
	}

	protected void tearDown() throws Exception {
		appContext.close();
		appContext = null;
	}

	private void readCtxFromResource(String resourceName) {
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		reader.loadBeanDefinitions(new ClassPathResource(resourceName, getClass()));
		appContext.refresh();
	}

	private void expectException(String resourceName) {
		try {
			readCtxFromResource(resourceName);
			fail("should have thrown parsing exception, invalid resource " + resourceName);
		}
		catch (FatalBeanException ex) {
			// expected
		}
	}

	public void testInlineInterfaceAndNestedInterfaces() throws Exception {
		expectException("osgiSingleReferenceInvalidInterface.xml");
	}

	public void testListenerWithNestedDefinitionAndInlinedRefVariant1() throws Exception {
		expectException("osgiSingleReferenceWithInvalidListener1.xml");
	}
	public void testListenerWithNestedDefinitionAndInlinedRefVariant2() throws Exception {
		expectException("osgiSingleReferenceWithInvalidListener2.xml");
	}

}
