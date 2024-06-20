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

package org.eclipse.gemini.blueprint.test.internal.util;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Costin Leau
 * 
 */
public class PropertiesUtilTest {

	private static final String VALUE = "bar";

	private Properties props;

	@Before
	public void setup() throws Exception {
		props = new Properties();
		props.load(getClass().getResourceAsStream("test.properties"));
		props = PropertiesUtil.expandProperties(props);
	}

	@After
	public void tearDown() throws Exception {
		props = null;
	}

	@Test
	public void testSimpleProperties() {
		assertEquals(VALUE, props.get("foo"));
	}

	@Test
	public void testSimpleKeyExpansion() {
		String key = "expanded." + VALUE;
		assertEquals(key, props.get(key));
	}

	@Test
	public void testDoubleKeyExpansion() {
		String key = VALUE + VALUE;
		assertEquals(key, props.get(key));
	}

	@Test
	public void testSimpleValueExpansion() {
		String key = "expanded.foo";
		assertEquals(key, props.get(key));
	}

	@Test
	public void testDoubleValueExpansion() {
		String key = "foofoo";
		assertEquals(key, props.get(key));
	}

	@Test
	public void testKeyWithIncludeValue() {
		Properties properties = new Properties();
		String sign = "+";
		properties.put("include", sign);
		assertEquals(properties, PropertiesUtil.filterValuesStartingWith(props, sign));
	}
}
