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

package org.eclipse.gemini.blueprint.blueprint.container;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.blueprint.CollectionTestComponent;
import org.eclipse.gemini.blueprint.blueprint.MyCustomDictionary;
import org.eclipse.gemini.blueprint.blueprint.MyCustomList;
import org.eclipse.gemini.blueprint.blueprint.container.SpringBlueprintContainer;
import org.eclipse.gemini.blueprint.blueprint.container.SpringBlueprintConverterService;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.context.support.PublicBlueprintDocumentLoader;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * @author Costin Leau
 */
public class TestBlueprintBuiltinConvertersTest extends TestCase {

	private static final String CONFIG = "builtin-converters.xml";

	private GenericApplicationContext context;
	private XmlBeanDefinitionReader reader;
	protected MockBundleContext bundleContext;
	private BlueprintContainer blueprintContainer;

	protected void setUp() throws Exception {
		bundleContext = new MockBundleContext();

		context = new GenericApplicationContext();
		context.setClassLoader(getClass().getClassLoader());
		context.getBeanFactory().setConversionService(
				new SpringBlueprintConverterService(null, context.getBeanFactory()));
		context.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));

		reader = new XmlBeanDefinitionReader(context);
		reader.setDocumentLoader(new PublicBlueprintDocumentLoader());
		reader.loadBeanDefinitions(new ClassPathResource(CONFIG, getClass()));
		context.refresh();

		blueprintContainer = new SpringBlueprintContainer(context);
	}

	protected void tearDown() throws Exception {
		context.close();
		context = null;
	}

	public void testConvertersAvailable() throws Exception {
		System.out.println(blueprintContainer.getComponentIds());
	}

	public void testCollection() throws Exception {
		CollectionTestComponent cpn = context.getBean("arrayToCollection", CollectionTestComponent.class);
		Object value = cpn.getPropertyValue();
		assertTrue(value instanceof Collection);
		System.out.println(value.getClass());
		Collection col = (Collection) value;
		assertEquals(3, col.size());
	}

	public void testSetToCollection() throws Exception {
		CollectionTestComponent cpn = context.getBean("setToCollection", CollectionTestComponent.class);
		Object value = cpn.getPropertyValue();
		assertTrue(value instanceof Set);
		System.out.println(value.getClass());
		Collection col = (Collection) value;
		assertEquals(2, col.size());
	}

	public void testCustomCollection() throws Exception {
		CollectionTestComponent cpn = context.getBean("customCollection", CollectionTestComponent.class);
		Object value = cpn.getPropertyValue();
		assertTrue(value instanceof MyCustomList);
		System.out.println(value.getClass());
		Collection col = (Collection) value;
		assertEquals(2, col.size());
		cpn = context.getBean("customDictionary", CollectionTestComponent.class);
		Object pv = cpn.getPropertyValue();
		assertTrue(pv instanceof Dictionary);
		assertTrue(pv instanceof MyCustomDictionary);
		assertEquals(2, ((MyCustomDictionary)pv).size());
	}
}