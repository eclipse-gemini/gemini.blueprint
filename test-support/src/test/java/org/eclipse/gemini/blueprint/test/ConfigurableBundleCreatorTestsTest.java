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

package org.eclipse.gemini.blueprint.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ReflectionUtils;

/**
 * Test Case for AbstractConfigurableBundleCreatorTests.
 * 
 * @author Costin Leau
 * 
 */
public class ConfigurableBundleCreatorTestsTest {

	private AbstractConfigurableBundleCreatorTests bundleCreator;

	@Before
	public void setup() throws Exception {
		bundleCreator = new AbstractConfigurableBundleCreatorTests() {
		};
	}

	@After
	public void tearDown() throws Exception {
		bundleCreator = null;
	}

	@Test
	public void testGetSettingsLocation() throws Exception {

		assertEquals(bundleCreator.getClass().getPackage().getName().replace('.', '/')
				+ "/ConfigurableBundleCreatorTestsTest$1-bundle.properties", bundleCreator.getSettingsLocation());
	}

	@Test
	public void testDefaultJarSettings() throws Exception {

		Properties defaultSettings = bundleCreator.getSettings();
		Field field = ReflectionUtils.findField(AbstractConfigurableBundleCreatorTests.class, "jarSettings" , Properties.class);
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, null, defaultSettings);
		assertNotNull(defaultSettings);
		assertNotNull(bundleCreator.getRootPath());
		assertNotNull(bundleCreator.getBundleContentPattern());
		assertNotNull(bundleCreator.getManifestLocation());
	}

	@Test
	public void testPropertiesLoading() throws Exception {
		Properties testSettings = bundleCreator.getSettings();

		Properties props = new Properties();
		props.load(new ClassPathResource(
				"org/eclipse/gemini/blueprint/test/ConfigurableBundleCreatorTestsTest$1-bundle.properties").getInputStream());

		assertEquals(props.getProperty(AbstractConfigurableBundleCreatorTests.INCLUDE_PATTERNS),
			testSettings.getProperty(AbstractConfigurableBundleCreatorTests.INCLUDE_PATTERNS));
		assertEquals(props.getProperty(AbstractConfigurableBundleCreatorTests.MANIFEST),
			testSettings.getProperty(AbstractConfigurableBundleCreatorTests.MANIFEST));
	}

}
