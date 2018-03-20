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

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.blueprint.TestComponent;
import org.eclipse.gemini.blueprint.blueprint.container.SpringBlueprintContainer;
import org.eclipse.gemini.blueprint.blueprint.container.support.BlueprintEditorRegistrar;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.container.NoSuchComponentException;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * 
 * @author Costin Leau
 * 
 */
public class ComponentSubElementTest extends TestCase {

	private static final String CONFIG = "component-subelements.xml";

	private GenericApplicationContext context;
	private BlueprintContainer BlueprintContainer;
	private XmlBeanDefinitionReader reader;
	protected MockBundleContext bundleContext;

	protected void setUp() throws Exception {
		bundleContext = new MockBundleContext();

		context = new GenericApplicationContext();
		context.setClassLoader(getClass().getClassLoader());
		context.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {

			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				beanFactory.addPropertyEditorRegistrar(new BlueprintEditorRegistrar());
			}
		});

		reader = new XmlBeanDefinitionReader(context);
		reader.loadBeanDefinitions(new ClassPathResource(CONFIG, getClass()));
		context.refresh();

		BlueprintContainer = new SpringBlueprintContainer(context.getBeanFactory());
	}

	protected void tearDown() throws Exception {
		context.close();
		context = null;
	}

	public void testNumberOfBeans() throws Exception {
		assertTrue("not enough beans found", context.getBeanDefinitionCount() > 4);
	}

	public void testConstructorArg() throws Exception {
		AbstractBeanDefinition def = (AbstractBeanDefinition) context.getBeanDefinition("constructor-arg");
		assertEquals(Integer.class.getName(), def.getBeanClassName());
		assertEquals("description", def.getDescription());
		ValueHolder argumentValue = def.getConstructorArgumentValues().getArgumentValue(0, int.class);
		assertNotNull(argumentValue);
	}

	public void testConstructorRef() throws Exception {
		AbstractBeanDefinition def = (AbstractBeanDefinition) context.getBeanDefinition("constructor-arg-ref");
		assertEquals(String.class.getName(), def.getBeanClassName());
		assertEquals("description2", def.getDescription());
		assertEquals(1, def.getConstructorArgumentValues().getArgumentCount());
	}

	public void testPropertyInline() throws Exception {
		AbstractBeanDefinition def = (AbstractBeanDefinition) context.getBeanDefinition("propertyValueInline");
		assertEquals(Socket.class.getName(), def.getBeanClassName());
		MutablePropertyValues propertyValues = def.getPropertyValues();
		PropertyValue propertyValue = propertyValues.getPropertyValue("keepAlive");
		assertNotNull(propertyValue);
		assertTrue(propertyValue.getValue() instanceof BeanMetadataElement);
	}

	public void testValueRef() throws Exception {
		AbstractBeanDefinition def = (AbstractBeanDefinition) context.getBeanDefinition("propertyValueRef");
		assertEquals(Socket.class.getName(), def.getBeanClassName());
		assertNotNull(def.getPropertyValues().getPropertyValue("sendBufferSize"));
	}

	public void testpropertyValueNested() throws Exception {
		AbstractBeanDefinition def = (AbstractBeanDefinition) context.getBeanDefinition("propertyValueNested");
		assertEquals(Socket.class.getName(), def.getBeanClassName());
		PropertyValue nested = def.getPropertyValues().getPropertyValue("sendBufferSize");
		assertTrue(nested.getValue() instanceof BeanDefinitionHolder);
	}

	public void testArray() throws Exception {
		TestComponent cmpn = (TestComponent) context.getBean("array");
		Object prop = cmpn.getPropA();
		assertTrue(prop instanceof Object[]);
		Object[] array = (Object[]) prop;
		assertEquals(Character.class, array[0].getClass());
		assertEquals("literal2", array[1]);
		assertNull(array[2]);
	}

	public void testMixedCollection() throws Exception {
		TestComponent cmpn = (TestComponent) context.getBean("mixedCollection");
		Object prop = cmpn.getPropA();
		assertTrue(prop instanceof List);
		List<?> list = (List<?>) prop;
		assertEquals("literal", list.get(0));
		assertEquals(Integer[].class, list.get(1).getClass());
		assertEquals(int[].class, list.get(2).getClass());
		assertEquals(new Integer(2), ((Integer[]) list.get(1))[0]);
		assertEquals(5, ((int[]) list.get(2))[1]);
	}

	public void testList() throws Exception {
		TestComponent cmpn = (TestComponent) context.getBean("list");
		Object prop = cmpn.getPropA();
		assertTrue(prop instanceof List);
		List<?> list = (List<?>) prop;
		assertEquals("value", list.get(0));
		assertEquals("idref", list.get(1));
		assertNull(list.get(2));
		assertSame(context.getBean("idref"), list.get(3));
	}

	public void testSet() throws Exception {
		TestComponent cmpn = (TestComponent) context.getBean("set");
		Object prop = cmpn.getPropA();
		assertTrue(prop instanceof Set);
		Set<?> set = (Set<?>) prop;
		assertTrue(set.contains("value"));
		assertTrue(set.contains("idref"));
		assertTrue(set.contains(null));
		assertTrue(set.contains(context.getBean("idref")));
	}

	public void testMap() throws Exception {
		TestComponent cmpn = (TestComponent) context.getBean("map");
		Object prop = cmpn.getPropA();
		assertTrue(prop instanceof Map);
		Map<?, ?> map = (Map) prop;
		assertEquals("bar", map.get("foo"));
		assertEquals(context.getBean("set"), map.get(context.getBean("list")));
		assertEquals(context.getBean("list"), map.get(context.getBean("set")));
	}

	public void testProps() throws Exception {
		TestComponent cmpn = (TestComponent) context.getBean("props");
		Object prop = cmpn.getPropA();
		assertTrue(prop instanceof Properties);
		Properties props = (Properties) prop;
		assertEquals("two", props.get("one"));
		assertEquals("smith", props.get("aero"));
	}

	public void testAmbigousComponent() throws Exception {
		System.out.println(context.getBean("ambigousComponent"));
	}

	public void testDependsOnTest() throws Exception {
		try {
			System.out.println(BlueprintContainer.getComponentInstance("dependsOnComponent"));
			fail("expected validation exception");
		} catch (NoSuchComponentException nsce) {
		}
	}
}