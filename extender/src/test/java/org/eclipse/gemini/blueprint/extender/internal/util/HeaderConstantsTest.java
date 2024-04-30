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

package org.eclipse.gemini.blueprint.extender.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Properties;

import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Costin Leau
 * 
 */
public class HeaderConstantsTest {

	private Dictionary headers;

	@Before
	public void setup() throws Exception {
		headers = new Properties();
		headers.put("some key", new Object());
		headers.put("another header", new Object());
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER + "1", new Object());
	}

	@After
	public void tearDown() throws Exception {
		headers = null;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.internal.ConfigUtils#getSpringContextHeader(java.util.Dictionary)}.
	 */
	@Test
	public void testGetServiceContextHeader() {
		assertNull(ConfigUtils.getSpringContextHeader(headers));
		String headerValue = "correct header";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, headerValue);
		assertSame(headerValue, ConfigUtils.getSpringContextHeader(headers));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.internal.ConfigUtils#getDirectiveValue(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetDirectiveValue() {
		String header = "bla bla";
		assertNull(ConfigUtils.getDirectiveValue(header, "bla"));
		assertNull(ConfigUtils.getDirectiveValue(header, header));
		String directive = "directive";
		String value = "value";
		header = directive + ConfigUtils.EQUALS + value;
		assertEquals(value, ConfigUtils.getDirectiveValue(header, directive));
		assertNull(ConfigUtils.getDirectiveValue(header, value));
		assertNull(ConfigUtils.getDirectiveValue(header, "directiv"));
		header = directive + ConfigUtils.EQUALS + value + ConfigUtils.EQUALS + value;
		assertNull(ConfigUtils.getDirectiveValue(header, directive));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.internal.ConfigUtils#getPublishContext(java.util.Dictionary)}.
	 */
	@Test
	public void testGetDontPublishContext() {
		String header = "nothing";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertTrue(ConfigUtils.getPublishContext(headers));

		header = ConfigUtils.DIRECTIVE_PUBLISH_CONTEXT + ConfigUtils.EQUALS + true;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertTrue(ConfigUtils.getPublishContext(headers));

		header = ConfigUtils.DIRECTIVE_PUBLISH_CONTEXT + ConfigUtils.EQUALS + false;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getPublishContext(headers));

		header = ConfigUtils.DIRECTIVE_PUBLISH_CONTEXT + ConfigUtils.EQUALS + "bla";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getPublishContext(headers));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.internal.ConfigUtils#getCreateAsync(java.util.Dictionary)}.
	 */
	@Test
	public void testGetCreateAsync() {
		String header = "nothing";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertTrue(ConfigUtils.getPublishContext(headers));

		header = ConfigUtils.DIRECTIVE_CREATE_ASYNCHRONOUSLY + ConfigUtils.EQUALS + true;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertTrue(ConfigUtils.getCreateAsync(headers));

		header = ConfigUtils.DIRECTIVE_CREATE_ASYNCHRONOUSLY + ConfigUtils.EQUALS + false;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getCreateAsync(headers));

		header = ConfigUtils.DIRECTIVE_CREATE_ASYNCHRONOUSLY + ConfigUtils.EQUALS + "bla";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertFalse(ConfigUtils.getCreateAsync(headers));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.internal.ConfigUtils#getTimeout(java.util.Dictionary)}.
	 */
	@Test
	public void testGetTimeout() {
		String header = "nothing";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertEquals(Long.valueOf(ConfigUtils.DIRECTIVE_TIMEOUT_DEFAULT), Long.valueOf(ConfigUtils.getTimeOut(headers)));

        header = ConfigUtils.DIRECTIVE_TIMEOUT + ConfigUtils.EQUALS + "500";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertEquals(Long.valueOf(500), Long.valueOf(ConfigUtils.getTimeOut(headers)));

		header = ConfigUtils.DIRECTIVE_TIMEOUT + ConfigUtils.EQUALS + ConfigUtils.DIRECTIVE_TIMEOUT_VALUE_NONE;
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, header);
		assertEquals(Long.valueOf(-2), Long.valueOf(ConfigUtils.getTimeOut(headers)));
	}

}
