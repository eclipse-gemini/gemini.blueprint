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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.eclipse.gemini.blueprint.blueprint.TestComponent;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ObjectUtils;

/**
 * 
 * @author Costin Leau
 * 
 */
public class ComponentElementTest {

	private static final String CONFIG = "basic-config.xml";

	private GenericApplicationContext context;
	private static String SIMPLE = "simple";
	private static String DEPENDS_ON = "depends-on";
	private XmlBeanDefinitionReader reader;

	@Before
	public void setup() throws Exception {
		context = new GenericApplicationContext();
		context.setClassLoader(getClass().getClassLoader());
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
		assertTrue("not enough beans found", context.getBeanDefinitionCount() > 6);
	}

	@Test
	public void testSimple() throws Exception {
		AbstractBeanDefinition def = (AbstractBeanDefinition) context.getBeanDefinition(SIMPLE);
		assertEquals(Object.class.getName(), def.getBeanClassName());
	}

	@Test
	public void testDependsOn() throws Exception {
		AbstractBeanDefinition def = (AbstractBeanDefinition) context.getBeanDefinition(DEPENDS_ON);
		assertEquals(Object.class.getName(), def.getBeanClassName());
		assertTrue(Arrays.equals(def.getDependsOn(), new String[] { SIMPLE }));
	}

	@Test
	public void testDestroyMethod() throws Exception {
		AbstractBeanDefinition def = (AbstractBeanDefinition) context.getBeanDefinition("destroy-method");
		assertEquals(Properties.class.getName(), def.getBeanClassName());
		assertEquals("clear", def.getDestroyMethodName());
	}

	@Test
	public void testLazyInit() throws Exception {
		AbstractBeanDefinition def = (AbstractBeanDefinition) context.getBeanDefinition("lazy-init");
		assertEquals(Object.class.getName(), def.getBeanClassName());
		assertTrue(def.isLazyInit());
	}

	@Test
	public void testFactoryMethod() throws Exception {
		AbstractBeanDefinition def = (AbstractBeanDefinition) context.getBeanDefinition("factory-method");
		assertEquals(System.class.getName(), def.getBeanClassName());
		assertEquals("currentTimeMillis", def.getFactoryMethodName());
	}

	@Test
	public void testFactoryComponent() throws Exception {
		AbstractBeanDefinition def = (AbstractBeanDefinition) context.getBeanDefinition("factory-component");
		assertNull(def.getBeanClassName());
		assertEquals("getName", def.getFactoryMethodName());
		assertEquals("thread", def.getFactoryBeanName());
	}

	@Test
	@Ignore
	public void tstSelfReferencePrototypeBean() throws Exception {
		TestComponent cmpn = context.getBean("self-reference", TestComponent.class);
		assertSame(cmpn, cmpn.getPropA());
	}
}