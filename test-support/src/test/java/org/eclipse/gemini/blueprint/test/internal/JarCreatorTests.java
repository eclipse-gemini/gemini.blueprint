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

package org.eclipse.gemini.blueprint.test.internal;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.test.internal.util.IOUtils;
import org.eclipse.gemini.blueprint.test.internal.util.jar.JarCreator;
import org.eclipse.gemini.blueprint.test.internal.util.jar.storage.MemoryStorage;
import org.eclipse.gemini.blueprint.test.internal.util.jar.storage.Storage;

/**
 * @author Costin Leau
 * 
 */
public class JarCreatorTests {

	private JarCreator creator;

	private Storage storage;

	@Before
	public void setup() throws Exception {
		creator = new JarCreator();
		storage = new MemoryStorage();
		creator.setStorage(storage);
	}

	@After
	public void tearDown() throws Exception {
		storage.dispose();
	}

	@Test
	public void testJarCreation() throws Exception {

		final Manifest mf = new Manifest();

		Map entries = mf.getEntries();
		Attributes attrs = new Attributes();

		attrs.putValue("rocco-ventrella", "winelight");
		entries.put("test", attrs);

		String location = JarCreatorTests.class.getName().replace('.', '/') + ".class";
		// get absolute file location
		// file:/...s/org/eclipse/gemini/blueprint/test/JarCreatorTests.class
		final URL clazzURL = getClass().getClassLoader().getResource(location);

		// go two folders above
		// ...s/org/springframework/
		String rootPath = new URL(clazzURL, "../../").toExternalForm();

		String firstLevel = new URL(clazzURL, "../").toExternalForm().substring(rootPath.length());
		// get file folder
		String secondLevel = new URL(clazzURL, ".").toExternalForm().substring(rootPath.length());

		// now determine the file relative to the root
		String file = clazzURL.toExternalForm().substring(rootPath.length());

		// create a simple jar from a given class and a manifest
		creator.setContentPattern(new String[] { file });
		creator.setRootPath(rootPath);
		creator.setAddFolders(true);

		System.out.println("creating jar with just one file " + file + " from root " + rootPath);

		// create the jar
		creator.createJar(mf);

		// start reading the jar
		JarInputStream jarStream = null;

		try {
			jarStream = new JarInputStream(storage.getInputStream());
			// get manifest
			assertEquals("original manifest not found", mf, jarStream.getManifest());

			// move the jar stream to the first entry (which should be META-INF/ folder)
			String entryName = jarStream.getNextEntry().getName();

			assertEquals("META-INF/ not found", "META-INF/", entryName);

			entryName = jarStream.getNextEntry().getName();
			assertEquals("folders above the file not included", firstLevel, entryName);

			entryName = jarStream.getNextEntry().getName();
			assertEquals("file folder not included", secondLevel, entryName);

			// now get the file
			jarStream.getNextEntry();
			// open the original file
			InputStream originalFile = clazzURL.openStream();

			int b;
			while ((b = originalFile.read()) != -1)
				assertEquals("incorrect jar content", b, jarStream.read());
		}
		finally {
			IOUtils.closeStream(jarStream);
		}
	}
}
