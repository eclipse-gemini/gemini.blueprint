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

package org.eclipse.gemini.blueprint.iandt.io;

import org.springframework.core.io.Resource;

/**
 * Bundle jar tests.
 * 
 * @author Costin Leau
 * 
 */
public class BundleJarTest extends BaseIoTest {

	public void testResourceFromJarOnly() throws Exception {
		Resource[] res = patternLoader.getResources("osgibundlejar:/org/eclipse/gemini/blueprint/iandt/io/duplicate.file");
		assertEquals(1, res.length);
	}

	public void testResourceFromJarOnlyWithFolderLevelWildcard() throws Exception {
		Resource[] res = patternLoader.getResources("osgibundlejar:/**/duplicat*.file");
		assertEquals(1, res.length);
	}

	public void testResourceFromFragmentsIgnored() throws Exception {
		Resource[] res = patternLoader.getResources("osgibundlejar:/fragment*.file");
		assertEquals(0, res.length);

	}

	public void testResourceWithWildcardAtFileLevelFromFragmentsIgnored() throws Exception {
		Resource[] res = patternLoader.getResources("osgibundlejar:/*.file");
		assertEquals(0, res.length);
	}

	// same as above
	public void testResourceWithWildcardAtFolderLevelFromFragmentsIgnored() throws Exception {
		Resource[] res = patternLoader.getResources("osgibundlejar:/**/fragment*.file");
		assertEquals(0, res.length);
	}

	// check last modified
	public void testLastModifiedWhileUsingJustTheOSGiAPI() throws Exception {
		Resource resource = patternLoader.getResource("osgibundlejar:/org/eclipse/gemini/blueprint/iandt/io/duplicate.file");
		assertTrue(resource.lastModified() > 0);
	}

	// wild pattern matching
	public void testResourcesFromWildCardWithAndWithoutLeadingSlash() throws Exception {
		Resource[] res = patternLoader.getResources("osgibundlejar:**/*");
		Resource[] res2 = patternLoader.getResources("osgibundlejar:/**/*");
		assertEquals(res2.length, res.length);
	}
}
