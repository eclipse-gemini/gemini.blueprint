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

package org.eclipse.gemini.blueprint.util;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.util.DebugUtils;
import org.osgi.framework.Version;

/**
 * Test for the critical logging path in debug utils.
 * 
 * @author Costin Leau
 * 
 */
public class DebugUtilsTest extends TestCase {

	private Version getVersion(String statement, String pkg) {
		return (Version) TestUtils.invokeStaticMethod(DebugUtils.class, "getVersion", new String[] { statement, pkg });
	}

	public void testNoVersion() throws Exception {
		String pkg = "foo";
		assertEquals(Version.emptyVersion, getVersion(pkg, pkg));
	}

	public void testSingleVersion() throws Exception {
		String pkg = "foo";
		String version = "1.2";
		assertEquals(Version.parseVersion(version), getVersion(pkg + ";version=" + version, pkg));
	}

	public void testVersionRange() throws Exception {
		String pkg = "foo";
		String version = "1.2.0.bla";
		assertEquals(Version.parseVersion(version), getVersion(pkg + ";version=\"[" + version + ",3.4\")", pkg));
	}

	public void testVersionRangePlusExtraDirective() throws Exception {
		String pkg = "foo";
		String version = "1.2.0.bla";
		assertEquals(Version.parseVersion(version), getVersion(pkg + ";version=\"[" + version
				+ ",3.4\");resolution:=optional", pkg));
	}

	public void testNoVersionPlusExtraDirective() throws Exception {
		String pkg = "foo";
		assertEquals(Version.emptyVersion, getVersion(pkg + ";resolution:=optional", pkg));
	}

	public void testSingleVersionPlusExtraDirective() throws Exception {
		String pkg = "foo";
		String version = "1.2.0.bla";
		assertEquals(Version.parseVersion(version), getVersion(pkg + ";version=" + version + ";resolution:=optional",
			pkg));
	}

	public void testSingleVersionWithQuotes() throws Exception {
		String pkg = "foo";
		String version = "3.4.5.pausesti";
		assertEquals(Version.parseVersion(version), getVersion(pkg + ";version=\"" + version + "\"", pkg));
	}

}
