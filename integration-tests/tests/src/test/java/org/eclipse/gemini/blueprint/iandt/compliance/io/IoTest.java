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

package org.eclipse.gemini.blueprint.iandt.compliance.io;

import java.net.URL;
import java.util.Enumeration;

import org.eclipse.gemini.blueprint.iandt.io.BaseIoTest;
import org.eclipse.gemini.blueprint.test.platform.Platforms;
import org.springframework.util.ObjectUtils;

/**
 * Low level access API used for discovering the underlying platform capabilities since there are subtle yet major
 * differences between each implementation.
 * 
 * @author Costin Leau
 * 
 */
public class IoTest extends BaseIoTest {

	protected String[] getBundleContentPattern() {
		return super.getBundleContentPattern();
	}

	// don't use any extra bundles - just the test jar
	protected String[] getTestBundlesNames() {
		return null;
	}

	// using /META-INF fails on KF 3.0.0.x
	public void testGetResourceOnMetaInf() throws Exception {
		URL url = bundle.getResource("/META-INF/");
		System.out.println(url);
		assertNotNull(url);
	}

	// fails on Felix 1.0.1 (fixed in 1.0.3 and KF 2.0.3)
	public void testGetResourceOnRoot() throws Exception {
		URL url = bundle.getResource("/");
		System.out.println("getResource('/') = " + url);
		assertNotNull("root folder not validated " + url);
	}

	// fails on Felix 1.0.1 (fixed in 1.0.3 and KF 2.0.3)
	public void testGetResourceSOnRoot() throws Exception {
		Enumeration enm = bundle.getResources("/");
		Object[] res = copyEnumeration(enm);
		System.out.println("getResources('/') = " + ObjectUtils.nullSafeToString(res));
		assertEquals("root folder not validated" + ObjectUtils.nullSafeToString(res), 1, res.length);
	}

	public void testFindEntriesOnFolders() throws Exception {
		Enumeration enm = bundle.findEntries("/", null, false);
		// should get 3 entries - META-INF/, org/ and log4j.properties

		Object[] res = copyEnumeration(enm);
		assertEquals("folders ignored; found " + ObjectUtils.nullSafeToString(res), 2, res.length);
	}

	public void testFindEntriesOnSubFolders() throws Exception {
		Enumeration enm = bundle.findEntries("/META-INF", null, false);
		Object[] res = copyEnumeration(enm);
		// should get 1 entry - META-INF/
		assertEquals("folders ignored; found " + ObjectUtils.nullSafeToString(res), 1, res.length);
	}

	// Valid jars do not have entries for root folder / - in fact it doesn't
	// even exist
	public void testGetEntryOnRoot() throws Exception {
		URL url = bundle.getEntry("/");
		assertNotNull(url);
	}

	// get folders
	public void testGetEntriesShouldReturnFoldersOnRoot() throws Exception {
		Enumeration enm = bundle.getEntryPaths("/");
		Object[] res = copyEnumeration(enm);
		assertEquals("folders ignored; found " + ObjectUtils.nullSafeToString(res), 2, res.length);
	}

	public void testGetFolderEntry() throws Exception {
		URL url = bundle.getEntry("META-INF/");
		assertNotNull(url);
	}

	public void testGetFolderEntries() throws Exception {
		Enumeration enm = bundle.getEntryPaths("META-INF/");
		Object[] res = copyEnumeration(enm);
		assertEquals("folders ignored; found " + ObjectUtils.nullSafeToString(res), 1, res.length);
	}

	public void testURLFolderReturnsProperPathForFolders() throws Exception {
		Enumeration enm = bundle.findEntries("/", "META-INF", false);
		assertNotNull(enm);
		assertTrue(enm.hasMoreElements());
		assertTrue(((URL) enm.nextElement()).getPath().endsWith("/"));
	}
}