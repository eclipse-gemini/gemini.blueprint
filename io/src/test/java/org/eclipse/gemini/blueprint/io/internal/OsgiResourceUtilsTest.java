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

import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.mock.ArrayEnumerator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;

/**
 * 
 * @author Costin Leau
 */
public class OsgiResourceUtilsTest extends TestCase {

	public void testNullGetPrefix() throws Exception {
		assertNotNull(OsgiResourceUtils.getPrefix(null));
		assertEquals(OsgiResourceUtils.EMPTY_PREFIX, OsgiResourceUtils.getPrefix(null));
	}

	public void testGetPrefix() {
		String prefix = "foo" + OsgiResourceUtils.PREFIX_DELIMITER;
		String suffix = "bar";
		String path = prefix + suffix;
		assertEquals(prefix, OsgiResourceUtils.getPrefix(path));
	}

	public void testDoublePrefix() throws Exception {
		String path = "noSuffix";
		assertEquals(OsgiResourceUtils.EMPTY_PREFIX, OsgiResourceUtils.getPrefix(path));
	}

	public void testGetSearchTypeUnknown() {
		assertEquals(OsgiResourceUtils.PREFIX_TYPE_UNKNOWN, OsgiResourceUtils.getSearchType("xxx:path"));
	}

	public void testGetSearchTypeUnspecified() {
		assertEquals(OsgiResourceUtils.PREFIX_TYPE_NOT_SPECIFIED, OsgiResourceUtils.getSearchType("path"));
	}

	public void testGetSearchTypeBundleSpace() {
		assertEquals(OsgiResourceUtils.PREFIX_TYPE_BUNDLE_SPACE, OsgiResourceUtils.getSearchType("osgibundle:path"));
	}

	public void testGetSearchTypeBundleJar() {
		assertEquals(OsgiResourceUtils.PREFIX_TYPE_BUNDLE_JAR, OsgiResourceUtils.getSearchType("osgibundlejar:path"));
	}

	public void testGetSearchTypeBundleClassSpace() {
		assertEquals(OsgiResourceUtils.PREFIX_TYPE_CLASS_SPACE,
			OsgiResourceUtils.getSearchType(ResourceUtils.CLASSPATH_URL_PREFIX + "path"));
	}

	public void testGetSearchTypeBundleClassAllSpace() {
		assertEquals(OsgiResourceUtils.PREFIX_TYPE_CLASS_ALL_SPACE, OsgiResourceUtils.getSearchType("classpath*:path"));
	}

	public void testIsClassPathType() {
		assertTrue(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_CLASS_ALL_SPACE));
		assertTrue(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_CLASS_SPACE));
		assertFalse(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_BUNDLE_JAR));
		assertFalse(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_BUNDLE_SPACE));
		assertFalse(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_NOT_SPECIFIED));
		assertFalse(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_UNKNOWN));
	}

	public void testStripPrefixWithNoPrefix() {
		String path = "path";
		assertEquals(path, OsgiResourceUtils.stripPrefix(path));
	}

	public void testStripPrefix() throws Exception {
		String prefix = "xxx:";
		String path = "path";
		assertEquals(path, OsgiResourceUtils.stripPrefix(prefix + path));
	}

	public void testConvertURLArraytoResourceArray() throws Exception {
		URL[] urls = new URL[] { new URL("file:///"),
			getClass().getResource("/" + getClass().getName().replace('.', '/') + ".class") };
		Resource[] resources = OsgiResourceUtils.convertURLArraytoResourceArray(urls);
		assertNotNull(resources);
		assertEquals(2, resources.length);

		for (int i = 0; i < resources.length; i++) {
			assertTrue(resources[i] instanceof UrlResource);
			assertEquals(urls[i], resources[i].getURL());
		}
	}

	public void testConvertNullURLArraytoResourceArray() {
		assertNotNull(OsgiResourceUtils.convertURLArraytoResourceArray(null));
		assertEquals(0, OsgiResourceUtils.convertURLArraytoResourceArray(null).length);
	}

	public void testConvertURLEnumerationToResourceArray() throws Exception {
		URL[] urls = new URL[] { new URL("file:///"),
			getClass().getResource("/" + getClass().getName().replace('.', '/') + ".class") };

		ArrayEnumerator enm = new ArrayEnumerator(urls);
		Resource[] resources = OsgiResourceUtils.convertURLEnumerationToResourceArray(enm);

		assertNotNull(resources);
		assertEquals(2, resources.length);

		for (int i = 0; i < resources.length; i++) {
			assertTrue(resources[i] instanceof UrlResource);
			assertEquals(urls[i], resources[i].getURL());
		}
	}

	public void testConvertNullURLEnumerationToResourceArray() {
		assertNotNull(OsgiResourceUtils.convertURLEnumerationToResourceArray(null));
		assertEquals(0, OsgiResourceUtils.convertURLEnumerationToResourceArray(null).length);
	}

	public void testFindUpperFolderWOAFolder() throws Exception {
		String path = "path";
		assertEquals(path, OsgiResourceUtils.findUpperFolder(path));
	}

	public void testFindUpperFolderWOAProperString() throws Exception {
		String path = "p";
		assertEquals(path, OsgiResourceUtils.findUpperFolder(path));
	}

	public void testFindUpperFolderWRootFolder() throws Exception {
		String path = "/";
		assertEquals(path, OsgiResourceUtils.findUpperFolder(path));
	}

	public void testFindUpperFolderWDoubleFolders() throws Exception {
		String path = "/path1/path2/";
		assertEquals("/path1/", OsgiResourceUtils.findUpperFolder(path));
	}

	public void testFindUpperFolderWFileInsideFolder() throws Exception {
		String path = "/path/file";
		assertEquals("/path/", OsgiResourceUtils.findUpperFolder(path));
	}

	public void testFindUpperFolderWRelativePath() throws Exception {
		String path = "path/file";
		assertEquals("path/", OsgiResourceUtils.findUpperFolder(path));
	}
}
