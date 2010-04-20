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

package org.eclipse.gemini.blueprint.test.provisioning.internal;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class MavenArtifactFinderTest extends TestCase {

	private static final String GROUP_ID = "foo";
	private static final String PATH = "src/test/resources/org/eclipse/gemini/blueprint/test";


	public void testFindMyArtifact() throws IOException {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder(GROUP_ID, "test-artifact", "1.0-SNAPSHOT",
			"jar");
		File found = finder.findPackagedArtifact(new File(PATH));
		assertNotNull(found);
		assertTrue(found.exists());
	}

	public void testFindChildArtifact() throws IOException {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder(GROUP_ID, "test-child-artifact",
			"1.0-SNAPSHOT", "jar");
		File found = finder.findPackagedArtifact(new File(PATH));
		assertNotNull(found);
		assertTrue(found.exists());
	}

	public void testFindParentArtifact() throws IOException {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder(GROUP_ID, "test-artifact", "1.0-SNAPSHOT",
			"jar");
		File found = finder.findPackagedArtifact(new File(PATH + "/child"));
		assertNotNull(found);
		assertTrue(found.exists());
	}

	public void testSameArtifactIdInTwoDifferentGroupsWithGroup1() throws Exception {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder("group1", "artifact", "1.0", "jar");
		File found = finder.findPackagedArtifact(new File(PATH));
		assertNotNull(found);
		assertTrue(found.exists());
		assertTrue(found.getAbsolutePath().indexOf("group1") > -1);
		// make sure group2 is not selected
		assertFalse(found.getAbsolutePath().indexOf("group2") > -1);
	}

	public void testSameArtifactIdInTwoDifferentGroupsWithGroup2() throws Exception {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder("group2", "artifact", "1.0", "jar");
		File found = finder.findPackagedArtifact(new File(PATH));
		assertNotNull(found);
		assertTrue(found.exists());
		assertTrue(found.getAbsolutePath().indexOf("group2") > -1);
		// make sure group2 is not selected
		assertFalse(found.getAbsolutePath().indexOf("group1") > -1);
	}

	public void testPomWithoutAGroupId() throws Exception {
		MavenPackagedArtifactFinder finder = new MavenPackagedArtifactFinder("non-existing", "badpom", "1.0", "jar");
		try {
			File found = finder.findPackagedArtifact(new File(PATH));
			fail("expected exception");
		}
		catch (Exception ex) {
			//expected
		}
	}
}