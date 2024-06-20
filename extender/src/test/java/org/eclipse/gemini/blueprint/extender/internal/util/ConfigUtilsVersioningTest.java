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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Dictionary;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class ConfigUtilsVersioningTest {

	private Bundle bundle;

	private Dictionary props;

	private Version min, max, version;

	@Before
	public void setup() throws Exception {
		props = new Properties();
		bundle = new MockBundle(props);

		min = Version.parseVersion("1.2");
		max = Version.parseVersion("1.3");
		version = Version.parseVersion("1.2.5");
	}

	@After
	public void tearDown() throws Exception {
		props = null;
		bundle = null;
	}

	private void addVersion(String version) {
		props.put(ConfigUtils.EXTENDER_VERSION, version);
	}

	@Test
	public void testNoVersion() {
		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, Version.emptyVersion));
	}

	@Test
	public void testLeftOpenRange() {
		String ver = "(1.2, 1.3]";
		addVersion(ver);

		assertFalse(ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, min));
		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, version));
	}

	@Test
	public void testRightOpenRange() {
		String ver = "[1.2, 1.3)";
		addVersion(ver);

		assertFalse(ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, max));
		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, version));
	}

	@Test
	public void testLeftCloseRange() {
		String ver = "[1.2, 1.3]";
		addVersion(ver);

		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, min));
		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, version));
	}

	@Test
	public void testRightCloseRange() {
		String ver = "[1.2, 1.3]";
		addVersion(ver);

		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, max));
		assertTrue(ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, version));
	}

	@Test
	public void testTooManyCommas() {
		String ver = "[1.2, ,1.3]";
		addVersion(ver);

		try {
			ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, Version.emptyVersion);
			fail("should have thrown exception; invalid range");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	@Test
	public void testTooManyCommasAgain() {
		String ver = "[1,2 , 1.3)";
		addVersion(ver);

		try {
			ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, Version.emptyVersion);
			fail("should have thrown exception; invalid range");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	@Test
	public void testNoBracketsIntervalOnRight() {
		String ver = "[1.2, 1.3";
		addVersion(ver);

		try {
			ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, Version.emptyVersion);
			fail("should have thrown exception; invalid range");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

	}

	@Test
	public void testNoBracketsIntervalOnLeft() {
		String ver = "1.2, 1.3)";
		addVersion(ver);

		try {
			ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, Version.emptyVersion);
			fail("should have thrown exception; invalid range");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

	}

	@Test
	public void testNoCommaInterval() {
		String ver = "[1.2 1.3]";
		addVersion(ver);

		try {
			ConfigUtils.matchExtenderVersionRange(bundle, ConfigUtils.EXTENDER_VERSION, Version.emptyVersion);
			fail("should have thrown exception; invalid range");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}
}
