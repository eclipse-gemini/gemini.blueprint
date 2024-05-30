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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ObjectUtils;

/**
 * 
 * @author Costin Leau
 * 
 */
public class SpringRootConfigTest {

	private static final String CONFIG = "spring-root-config.xml";

	private GenericApplicationContext context;
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
		assertTrue("not enough beans found", context.getBeanDefinitionCount() == 1);
	}
}
