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

package org.eclipse.gemini.blueprint.io.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Properties;

import org.junit.Test;

import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * @author Costin Leau
 * 
 */
public class OsgiHeaderUtilsTest {

	private static final String DEFAULT_VERSION = "0.0.0";

	private static String PKG = "com.acme.facade";

	@Test
	public void testGetNoBundleClassPathDefined() {
		Properties props = new Properties();
		Bundle bundle = new MockBundle(props);
		String[] cp = OsgiHeaderUtils.getBundleClassPath(bundle);
		assertEquals(0, cp.length);
	}

	@Test
	public void testGetBundleClassPath() {
		Properties props = new Properties();
		String path1 = ".";
		String path2 = "WEB-INF/";
		props.setProperty(Constants.BUNDLE_CLASSPATH, path1 + "," + path2);
		Bundle bundle = new MockBundle(props);
		String[] cp = OsgiHeaderUtils.getBundleClassPath(bundle);
		assertEquals(2, cp.length);
		assertEquals(path1, cp[0]);
		assertEquals(path2, cp[1]);
	}

	@Test
	public void testGetBundleClassPathWithWhiteSpaces() {
		Properties props = new Properties();
		String path1 = ".";
		String path2 = "WEB-INF/";
		props.setProperty(Constants.BUNDLE_CLASSPATH, " " + path1 + " ,  " + path2 + "   ");
		Bundle bundle = new MockBundle(props);
		String[] cp = OsgiHeaderUtils.getBundleClassPath(bundle);

		// check for spaces
		assertSame(cp[0], cp[0].trim());
		assertSame(cp[1], cp[1].trim());
		// check result
		assertEquals(2, cp.length);
		assertEquals(path1, cp[0]);
		assertEquals(path2, cp[1]);
	}

	@Test
	public void testGetRequireBundleUndeclared() throws Exception {
		Properties props = new Properties();
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(0, rb.length);
	}

	@Test
	public void testGetRequireBundleWithMultipleBundlesAttributesAndWhitespaces() throws Exception {
		Properties props = new Properties();
		String pkg2 = "foo.bar";
		props.setProperty(Constants.REQUIRE_BUNDLE, "  " + PKG + ";visibility:=reexport;bundle-version=\"1.0\" ,\t  "
				+ pkg2 + "\n  ");
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);

		assertSame(rb[0], rb[0].trim());
		assertSame(rb[1], rb[1].trim());
	}

	@Test
	public void testGetRequireBundleWMultipleUnversionedEntries() throws Exception {
		Properties props = new Properties();
		String b1 = "foo";
		String b2 = "bar";
		props.setProperty(Constants.REQUIRE_BUNDLE, b1 + "," + b2);
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(2, rb.length);
		assertEquals(b1, rb[0]);
		assertEquals(b2, rb[1]);
	}

	@Test
	public void testRequireBundleWithSimpleVersions() throws Exception {
		Properties props = new Properties();
		String b1 = "foo;bundle-version=1.1.0";
		String b2 = "bar;bundle-version=2";
		props.setProperty(Constants.REQUIRE_BUNDLE, b1 + "," + b2);
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(2, rb.length);
		assertEquals(b1, rb[0]);
		assertEquals(b2, rb[1]);
	}

	@Test
	public void testRequireBundleWithRangeVersions() throws Exception {
		Properties props = new Properties();
		String b1 = "foo;bundle-version=\"[1.0,2.0)\"";
		String b2 = "bar;bundle-version=1.0.0";
		props.setProperty(Constants.REQUIRE_BUNDLE, b1 + "," + b2);
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(2, rb.length);
		assertEquals(b1, rb[0]);
		assertEquals(b2, rb[1]);
	}

	@Test
	public void testRequireBundleWithQuotes() throws Exception {
		Properties props = new Properties();
		String b1 = "foo;bundle-version=\"[1.0,2.0)\"";
		String b2 = "bar;bundle-version=\"1.0.0\"";
		props.setProperty(Constants.REQUIRE_BUNDLE, b1 + "," + b2);
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(2, rb.length);
		assertEquals(b1, rb[0]);
		assertEquals(b2, rb[1]);
	}

	@Test
	public void testRequireBundleWithVersionAndExtraAttributes() throws Exception {
		Properties props = new Properties();
		String b1 = "foo;bundle-version=\"[1.0,2.0)\";visibility:=reexport";
		String b2 = "bar;resolution:=optional;bundle-version=\"1.0.0\"";
		props.setProperty(Constants.REQUIRE_BUNDLE, b1 + "," + b2);
		Bundle bundle = new MockBundle(props);
		String[] rb = OsgiHeaderUtils.getRequireBundle(bundle);
		assertEquals(2, rb.length);
		assertEquals(b1, rb[0]);
		assertEquals(b2, rb[1]);
	}

	@Test
	public void testParseRequireBundleEntryWithNoVersion() throws Exception {
		String entry = PKG;
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(DEFAULT_VERSION, result[1]);
	}

	@Test
	public void testParseRequireBundleEntryWithSimpleUnquotedVersion() throws Exception {
		String version = "1.0.0.a";
		String entry = PKG + ";" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=" + version;
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	@Test
	public void testParseRequireBundleEntryWithSimpleQuotedVersion() throws Exception {
		String version = "1.2.3";
		String entry = PKG + ";" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"" + version + "\"";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	@Test
	public void testParseRequireBundleEntryWithVersionRange() throws Exception {
		String version = "[1.0.0,2.0.0a)";
		String entry = PKG + ";" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"" + version + "\"";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	@Test
	public void testParseRequireBundleEntryWithSimpleUnquotedVersionAndExtraAttributes() throws Exception {
		String version = "1.0.0.a";
		String entry = PKG + ";visibility:=reexport;" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=" + version
				+ ";resolution:=optional";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	@Test
	public void testParseRequireBundleEntryWithSimpleQuotedVersionAndExtraAttributes() throws Exception {
		String version = "1.0.0.a";
		String entry = PKG + ";visibility:=reexport;" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"" + version
				+ "\";resolution:=optional";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	@Test
	public void testParseRequireBundleEntryWithVersionRangeAndExtraAttributes() throws Exception {
		String version = "[1.0.0,2.0.0a)";
		String entry = PKG + ";visibility:=reexport;" + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"" + version
				+ "\";resolution:=optional";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(version, result[1]);
	}

	@Test
	public void testParseRequireBundleEntryWithNoVersionAndExtraAttributes() throws Exception {
		String entry = PKG + ";visibility:=reexport;resolution:=optional";
		String[] result = OsgiHeaderUtils.parseRequiredBundleString(entry);
		assertEquals(PKG, result[0]);
		assertEquals(DEFAULT_VERSION, result[1]);
	}

	//
	// old battery of tests
	//

	@Test
	public void testParseEntryWithAttribute() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG + ";visibility:=reexport");
		assertEquals(PKG, values[0]);
		assertEquals(DEFAULT_VERSION, values[1]);
	}

	@Test
	public void testParseSimpleEntry() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG);
		assertEquals(PKG, values[0]);
		assertEquals(DEFAULT_VERSION, values[1]);
	}

	@Test
	public void testParseEntryWithSingleVersion() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG + ";bundle-version=\"1.0\"");
		assertEquals(PKG, values[0]);
		assertEquals("1.0", values[1]);
	}

	@Test
	public void testParseEntryWithRangeVersion() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG + ";bundle-version=\"[1.0,2.0)\"");
		assertEquals(PKG, values[0]);
		assertEquals("[1.0,2.0)", values[1]);
	}

	@Test
	public void testParseEntryWithRangeVersionAndExtraHeader() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG
				+ ";bundle-version=\"[1.0,2.0)\";visibility:=reexport");
		assertEquals(PKG, values[0]);
		assertEquals("[1.0,2.0)", values[1]);
	}

	@Test
	public void testParseEntryWithExtraHeaderAndRangeVersion() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG
				+ ";visibility:=reexport;bundle-version=\"[1.0,2.0)\"");
		assertEquals(PKG, values[0]);
		assertEquals("[1.0,2.0)", values[1]);
	}

	@Test
	public void testParseEntryWithExtraHeaderAndSimpleVersion() throws Exception {
		String[] values = OsgiHeaderUtils.parseRequiredBundleString(PKG
				+ ";visibility:=reexport;bundle-version=\"1.0\"");
		assertEquals(PKG, values[0]);
		assertEquals("1.0", values[1]);
	}
}
