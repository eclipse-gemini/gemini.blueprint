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

import java.net.URL;
import java.util.Date;
import java.util.Locale;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.blueprint.TestComponent;
import org.eclipse.gemini.blueprint.blueprint.container.SpringBlueprintContainer;
import org.eclipse.gemini.blueprint.blueprint.container.SpringBlueprintConverterService;
import org.eclipse.gemini.blueprint.context.support.PublicBlueprintDocumentLoader;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * 
 * @author Costin Leau
 */
public class ConstructorInjectionTest extends TestCase {

	private static final String CONFIG = "blueprint-construct-inject.xml";

	private GenericApplicationContext context;
	private XmlBeanDefinitionReader reader;
	private BlueprintContainer container;

	protected void setUp() throws Exception {
		context = new GenericApplicationContext();
		context.setClassLoader(getClass().getClassLoader());
		reader = new XmlBeanDefinitionReader(context);
		reader.setDocumentLoader(new PublicBlueprintDocumentLoader());
		reader.loadBeanDefinitions(new ClassPathResource(CONFIG, getClass()));
		context.getBeanFactory().setConversionService(new SpringBlueprintConverterService(null, context.getBeanFactory()));
		context.refresh();
		container = new SpringBlueprintContainer(context.getBeanFactory());
	}

	protected void tearDown() throws Exception {
		context.close();
		context = null;
	}

	private TestComponent getComponent(String name) {
		return context.getBean(name, TestComponent.class);
	}

	private <T> T getPropA(String name) {
		TestComponent tc = getComponent(name);
		return (T) tc.getPropA();
	}

	public void tstCtrAssign() throws Exception {
		Object propA = getPropA("constructorAssign");
	}

	public void testCharArray() throws Exception {
		Object propA = getPropA("compWrappedCharArray");
		assertTrue(propA instanceof Character[]);
	}

	public void testPrimitiveShortArray() throws Exception {
		Object propA = getPropA("compPrimShortArray");
		assertTrue(propA instanceof short[]);
	}

	public void testDateArray() throws Exception {
		Date[] array = getPropA("compDateArray");
		Date date = new Date("19 Feb 2009");
		assertEquals(array[0], date);

	}

	public void testURLArray() throws Exception {
		URL[] array = getPropA("compURLArray");
		assertEquals(2, array.length);
	}

	public void testClassArray() throws Exception {
		Class<?>[] propA = getPropA("compClassArray");
		assertEquals(String.class, propA[0]);
	}

	public void testLocaleArray() throws Exception {
		Locale[] propA = getPropA("compLocaleArray");
		assertEquals(Locale.US, propA[0]);
	}

	public void testPrimitiveConstructor() throws Exception {
		try {
			Object component = context.getBean("primToWrapperArg");
			fail("Expected an ambuigity exception");
		} catch (Exception ex) {
			// expected
		}
	}

	public void testPrimitiveFactoryMethod() throws Exception {
		try {
			Object component = context.getBean("primToWrapperFactory");
			fail("Expected an ambuigity exception");
		} catch (Exception ex) {
			// expected
		}
	}

	public void testNestedValue() throws Exception {
		Object component = context.getBean("nestedURLValue");
	}

	public void testNestedValueFactory() throws Exception {
		Object component = context.getBean("nestedURLValueFactory");
	}

	public void testEmptyArray() throws Exception {
		Object component = context.getBean("emptyArrayConstruct");
	}

	public void testCollectionConversion() throws Exception {
		try {
			Object component = context.getBean("collectionConflict");
			fail("Expected an ambuigity exception");
		} catch (Exception ex) {
			// expected
		}
	}
	

	public void testCompProperties() throws Exception {
		Object component = context.getBean("compProperties");
	}
}