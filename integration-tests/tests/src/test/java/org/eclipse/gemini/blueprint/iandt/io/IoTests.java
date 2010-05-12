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
 * Test to check if loading of files outside of the OSGi world (directly from
 * the filesystem is possible).
 * 
 * @author Costin Leau
 * 
 */
public class IoTests extends BaseIoTest {

	protected String getManifestLocation() {
		return null;
	}

	/**
	 * Add a bundle fragment.
	 */
	protected String[] getTestBundlesNames() {
		return null;
	}

	public void testFileOutsideOSGi() throws Exception {
		String fileLocation = "file:///" + thisClass.getFile().getAbsolutePath();
		// use file system resource defaultLoader
		Resource fileResource = defaultLoader.getResource(fileLocation);
		assertTrue(fileResource.exists());

		// try loading the file using OsgiBundleResourceLoader
		Resource osgiResource = resourceLoader.getResource(fileLocation);
		// check existence of the same file when loading through the
		// OsgiBundleRL
		// NOTE andyp -- we want this to work!!
		assertTrue(osgiResource.exists());

		assertEquals(fileResource.getURL(), osgiResource.getURL());
	}

	public void testNonExistentFileOutsideOSGi() throws Exception {
		String nonExistingLocation = thisClass.getURL().toExternalForm().concat("-bogus-extension");

		Resource nonExistingFile = defaultLoader.getResource(nonExistingLocation);
		assertNotNull(nonExistingFile);
		assertFalse(nonExistingFile.exists());

		Resource nonExistingFileOutsideOsgi = resourceLoader.getResource(nonExistingLocation);
		assertNotNull(nonExistingFileOutsideOsgi);
		assertFalse(nonExistingFileOutsideOsgi.exists());
	}

}
