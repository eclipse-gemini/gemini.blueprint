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

import junit.framework.TestCase;
import org.eclipse.gemini.blueprint.blueprint.container.support.BlueprintEditorRegistrar;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.context.support.PublicBlueprintDocumentLoader;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.util.Collection;

/**
 * @author Costin Leau
 */
public class TestLazyBeansTest extends TestCase {

	private static final String CONFIG = "lazy-beans.xml";

	private GenericApplicationContext context;
	private XmlBeanDefinitionReader reader;
	protected MockBundleContext bundleContext;
	private BlueprintContainer blueprintContainer;

	protected void setUp() throws Exception {
		bundleContext = new MockBundleContext();

		context = new GenericApplicationContext();
		context.setClassLoader(getClass().getClassLoader());
		context.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {

			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				beanFactory.addPropertyEditorRegistrar(new BlueprintEditorRegistrar());
				beanFactory.registerSingleton("blueprintContainer",
						new SpringBlueprintContainer(context.getBeanFactory()));
			}
		});

		reader = new XmlBeanDefinitionReader(context);
		reader.setDocumentLoader(new PublicBlueprintDocumentLoader());
		reader.loadBeanDefinitions(new ClassPathResource(CONFIG, getClass()));
		context.refresh();

		blueprintContainer = new SpringBlueprintContainer(context.getBeanFactory());
	}

	protected void tearDown() throws Exception {
		context.close();
		context = null;
	}

	public void testConvertersAvailable() throws Exception {
		System.out.println(blueprintContainer.getComponentIds());
		blueprintContainer.getComponentInstance("lazyCollection");
	}

	public void testBeanCount() throws Exception {
		Collection<ComponentMetadata> metadata = blueprintContainer.getMetadata(ComponentMetadata.class);
		System.out.println(metadata.size());
		for (ComponentMetadata componentMetadata : metadata) {
			System.out.println(componentMetadata.getId());
		}
	}
}