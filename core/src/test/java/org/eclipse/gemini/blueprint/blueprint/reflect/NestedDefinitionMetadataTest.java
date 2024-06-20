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

package org.eclipse.gemini.blueprint.blueprint.reflect;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.blueprint.container.SpringBlueprintContainer;
import org.eclipse.gemini.blueprint.blueprint.container.support.BlueprintEditorRegistrar;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.context.support.PublicBlueprintDocumentLoader;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.ServiceReferenceMetadata;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * @author Costin Leau
 */
public class NestedDefinitionMetadataTest {

	private static final String CONFIG = "nested-managers.xml";

	private GenericApplicationContext context;
	private XmlBeanDefinitionReader reader;
	protected MockBundleContext bundleContext;
	private BlueprintContainer blueprintContainer;

	@Before
	public void setup() throws Exception {
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
		reader.setDocumentLoader(new PublicBlueprintDocumentLoader());
		reader.loadBeanDefinitions(new ClassPathResource(CONFIG, getClass()));
		context.refresh();

		blueprintContainer = new SpringBlueprintContainer(context);
	}

	@After
	public void tearDown() throws Exception {
		context.close();
		context = null;
	}

	@Test
	public void testFirstLevel() throws Exception {
		String name = "first-level";
		BeanDefinition def = context.getBeanDefinition(name);
		Collection<ComponentMetadata> metadata = ComponentMetadataFactory.buildNestedMetadata(def);
		assertEquals(2, metadata.size());
	}

	@Test
	public void testDeeplyNested() throws Exception {
		String name = "deeply-nested";
		BeanDefinition def = context.getBeanDefinition(name);
		Collection<ComponentMetadata> metadata = ComponentMetadataFactory.buildNestedMetadata(def);
		assertEquals(3, metadata.size());
	}

	@Test
	public void testCollectionNested() throws Exception {
		String name = "nested-collection";
		BeanDefinition def = context.getBeanDefinition(name);
		Collection<ComponentMetadata> metadata = ComponentMetadataFactory.buildNestedMetadata(def);
		assertEquals(3, metadata.size());
	}

	@Test
	public void testNestedBeans() throws Exception {
		String name = "nested-beans";
		BeanDefinition def = context.getBeanDefinition(name);
		Collection<ComponentMetadata> metadata = ComponentMetadataFactory.buildNestedMetadata(def);
		assertEquals(4, metadata.size());
	}

	@Test
	public void testNestedServices() throws Exception {
		String name = "nested-references";
		BeanDefinition def = context.getBeanDefinition(name);
		Collection<ComponentMetadata> metadata = ComponentMetadataFactory.buildNestedMetadata(def);
		assertEquals(2, metadata.size());
	}

	@Test
	public void testOverallMetadata() throws Exception {

		BeanDefinition def = new GenericBeanDefinition();
		assertEquals(new SimpleComponentMetadata(null, def), new SimpleComponentMetadata("foo", def));

		Collection<ComponentMetadata> metadata = blueprintContainer.getMetadata(ComponentMetadata.class);

		for (ComponentMetadata componentMetadata : metadata) {
			if (componentMetadata instanceof ServiceReferenceMetadata) {
				System.out.println(componentMetadata.getId());
			}
		}
		// 1+1+3+4+4+5+3+1=22
		assertEquals(22, metadata.size());
		System.out.println(blueprintContainer.getComponentIds());
	}
}