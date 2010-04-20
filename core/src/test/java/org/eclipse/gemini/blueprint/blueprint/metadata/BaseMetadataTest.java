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

package org.eclipse.gemini.blueprint.blueprint.metadata;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.blueprint.container.SpringBlueprintContainer;
import org.eclipse.gemini.blueprint.blueprint.container.support.BlueprintEditorRegistrar;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.eclipse.gemini.blueprint.context.support.PublicBlueprintDocumentLoader;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * Base class for metadata tests.
 * 
 * @author Costin Leau
 */
public abstract class BaseMetadataTest extends TestCase {

	protected GenericApplicationContext applicationContext;
	protected BlueprintContainer blueprintContainer;
	private XmlBeanDefinitionReader reader;
	protected MockBundleContext bundleContext;

	protected void setUp() throws Exception {
		bundleContext = new MockBundleContext();
		applicationContext = new GenericApplicationContext();
		applicationContext.setClassLoader(getClass().getClassLoader());
		applicationContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		applicationContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {

			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				beanFactory.addPropertyEditorRegistrar(new BlueprintEditorRegistrar());
			}
		});
		reader = new XmlBeanDefinitionReader(applicationContext);
		reader.setDocumentLoader(new PublicBlueprintDocumentLoader());
		reader.loadBeanDefinitions(new ClassPathResource(getConfig(), getClass()));
		applicationContext.refresh();
		blueprintContainer = new SpringBlueprintContainer(applicationContext);
	}

	protected abstract String getConfig();

	protected void tearDown() throws Exception {
		applicationContext.close();
		applicationContext = null;
		blueprintContainer = null;
		bundleContext = null;
	}
}