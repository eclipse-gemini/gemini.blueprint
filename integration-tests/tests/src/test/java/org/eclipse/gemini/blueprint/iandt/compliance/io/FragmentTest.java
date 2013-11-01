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

import org.eclipse.gemini.blueprint.iandt.io.BaseIoTest;
import org.osgi.framework.Bundle;

/**
 * Raw test for discovering the fragments support for each platform. The native
 * {@link Bundle#findEntries(String, String, boolean)} method is being used to
 * see if files from the attached fragments are attached.
 * 
 * 
 * @author Costin Leau
 * 
 */
public class FragmentTest extends BaseIoTest {

	//
	// Folder tests
	//

	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return isFelix();
	}

	protected String[] getBundleContentPattern() {
		return new String[] { "**/*" };
	}

	/**
	 * Check META-INF folders.
	 * 
	 */
	public void testRootFoldersInFragmentsAndOwner() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "META-INF", false));
		assertResourceArray(res, 3);
	}

	public void testRootFolderCommonInFragmentsAlone() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment.folder", false));
		assertResourceArray(res, 2);
	}

	public void testRootFolderOnlyInFragment1() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment1.folder", false));
		assertResourceArray(res, 1);
	}

	public void testNestedFolderOnlyInFragmentsRecursively() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "nested.folder", true));
		assertResourceArray(res, 2);
	}

	public void testNestedFolderOnlyInFragments() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment.folder", "nested.folder", false));
		assertResourceArray(res, 2);
	}

	public void testRootFolderOnlyInFragment2() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment2.folder", false));
		assertResourceArray(res, 1);
	}

	public void testNestedFolderOnlyInFragment1() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment1.folder", "nested.folder.1", false));
		assertResourceArray(res, 1);
	}

	public void testNestedFolderOnlyInFragment2() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment2.folder", "nested.folder.2", false));
		assertResourceArray(res, 1);
	}

	public void testCommonFolderOnlyInFragmentsButNotInHost() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment.folder", "nested.folder", false));
		assertResourceArray(res, 2);
	}

	public void testCommonFolderInFragmentsAndHost() {
		Object[] res = copyEnumeration(bundle.findEntries("/org/eclipse", "gemini", false));
		assertResourceArray(res, 3);
	}

	public void testFolderOnlyInHost() {
		Object[] res = copyEnumeration(bundle.findEntries("/org/eclipse/gemini/blueprint/iandt", "bundleScope", false));
		assertResourceArray(res, 1);
	}

	//
	// File tests
	//

	public void testRootFileInBothFragmentsButNotInHost() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment.file", false));
		assertResourceArray(res, 2);
	}

	public void testRootFileOnlyInFragment1() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment1.file", false));
		assertResourceArray(res, 1);
	}

	public void testRootFileOnlyInFragment2() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "fragment2.file", false));
		assertResourceArray(res, 1);
	}

	public void testRootFileOnlyInHostBundle() {
		Object[] res = copyEnumeration(bundle.findEntries("/", "logback.xml", false));
		assertResourceArray(res, 1);
	}

	public void testNestedFileOnlyInFragments() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment.folder/nested.folder", "nested.file", false));
		assertResourceArray(res, 2);
	}

	public void testNestedFileOnlyInFragment1() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment1.folder/nested.folder.1", "nested.file.1.1", false));
		assertResourceArray(res, 1);
	}

	public void testNestedFileOnlyInFragment2() {
		Object[] res = copyEnumeration(bundle.findEntries("/fragment2.folder/nested.folder.2", "nested.file.2.2", false));
		assertResourceArray(res, 1);
	}

	public void testDuplicateFilesInHostAndFragments() {
		Object[] res = copyEnumeration(bundle.findEntries("/org/eclipse/gemini/blueprint/iandt/io", "duplicate.file", false));
		assertResourceArray(res, 3);
	}

	//
	// Classpath tests
	//

	public void testGetResourceOnRootDir() throws Exception {
		URL root = bundle.getResource("/");
		System.out.println(root);
		assertNotNull("root path not considered", root);
	}

	public void testGetResourceSOnRootDir() throws Exception {
		Object[] res = copyEnumeration(bundle.getResources("/"));
		// 3 paths should be found (1 host + 2 fragments)
		assertResourceArray(res, 3);
	}
}
