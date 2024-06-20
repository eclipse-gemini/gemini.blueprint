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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.blueprint.CustomType;
import org.eclipse.gemini.blueprint.blueprint.TestComponent;
import org.eclipse.gemini.blueprint.blueprint.container.SpringBlueprintConverter;
import org.eclipse.gemini.blueprint.blueprint.container.support.BlueprintEditorRegistrar;
import org.eclipse.gemini.blueprint.service.importer.support.ServiceReferenceEditor;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.ReifiedType;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Costin Leau
 * 
 */
public class TypeConverterTest {

	private static final String CONFIG = "type-converters.xml";

	private GenericApplicationContext context;
	private XmlBeanDefinitionReader reader;

	@Before
	public void setup() throws Exception {
		context = new GenericApplicationContext();
		context.setClassLoader(getClass().getClassLoader());
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

		beanFactory.registerCustomEditor(ServiceReference.class, ServiceReferenceEditor.class);
		beanFactory.addPropertyEditorRegistrar(new BlueprintEditorRegistrar());

		reader = new XmlBeanDefinitionReader(context);
		reader.loadBeanDefinitions(new ClassPathResource(CONFIG, getClass()));
		context.refresh();
	}

	@After
	public void tearDown() throws Exception {
		context.close();
		context = null;
	}

	@Test
	public void testNumberOfBeans() throws Exception {
		System.out.println("The beans declared are: " + ObjectUtils.nullSafeToString(context.getBeanDefinitionNames()));
		assertTrue("not enough beans found", context.getBeanDefinitionCount() >= 3);
	}

	@Test
	public void testReferenceToConverter() throws Exception {
		TestComponent component = (TestComponent) context.getBean("conversion");
		Object prop = component.getPropB();
		assertTrue(prop instanceof ComponentHolder);
		assertEquals("rachmaninoff", ((ComponentHolder) prop).getProperty());
	}

	@Test
	public void testNestedConverter() throws Exception {
		TestComponent component = (TestComponent) context.getBean("conversion");
		Object prop = component.getPropA();
		assertTrue(prop instanceof TestComponent);
		assertEquals("sergey", ((TestComponent) prop).getPropA());
	}

	@Test
	public void testConversionService() throws Exception {
		SpringBlueprintConverter cs = new SpringBlueprintConverter(context.getBeanFactory());

		Object converted = cs.convert("1", new ReifiedType(Long.class));
		assertNotNull(converted);
		assertEquals(Long.valueOf("1"), converted);

		assertEquals(Boolean.TRUE, cs.convert("T", new ReifiedType(Boolean.class)));
	}

	@Test
	public void testBooleanConversion() throws Exception {
		TestComponent comp = (TestComponent) context.getBean("booleanConversion");
		assertEquals(Boolean.TRUE, comp.getPropA());
	}

	@Test
	public void testArrayConversion() throws Exception {
		TestComponent comp = (TestComponent) context.getBean("arrayConversion");
		assertTrue(comp.getPropA() instanceof CustomType[]);
	}

	@Test
	public void testReferenceDelegate() throws Exception {
		TestComponent comp = (TestComponent) context.getBean("serviceReference");
		assertNotNull(comp.getServiceReference());
	}

	@Test
	public void testObjectToCollectionConversion1() throws Exception {
		TestComponent comp = (TestComponent) context.getBean("objectToCollectionConversion1");
		Object propA = comp.getPropA();
		assertThat(propA).isInstanceOf(List.class);
		assertThat((List) propA).hasSize(1);
	}

	@Test
	public void testObjectToCollectionConversion2() throws Exception {
		TestComponent comp = (TestComponent) context.getBean("objectToCollectionConversion2");
		Object propA = comp.getPropA();
		assertThat(propA).isInstanceOf(List.class);
		assertThat((List) propA).hasSize(1);
	}
}