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

import static org.junit.Assert.assertEquals;

import org.eclipse.gemini.blueprint.context.support.PublicBlueprintDocumentLoader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Test for xml files that specify the blueprint schema w/o a location.
 * 
 * @author Costin Leau
 */
public class SchemaWithLocationTest {

	private static final String CONFIG = "schemaWithoutLocation.xml";

	private GenericApplicationContext context;
	private XmlBeanDefinitionReader reader;

	@Before
	public void setup() throws Exception {
		context = new GenericApplicationContext();
		context.setClassLoader(getClass().getClassLoader());
		reader = new XmlBeanDefinitionReader(context);
		reader.setDocumentLoader(new PublicBlueprintDocumentLoader());
		reader.loadBeanDefinitions(new ClassPathResource(CONFIG, getClass()));
		context.refresh();
	}

	@After
	public void tearDown() throws Exception {
		context.close();
		context = null;
	}

	@Test
	public void testContainerSanity() throws Exception {
		assertEquals(1, context.getBeanDefinitionCount());
	}
}
