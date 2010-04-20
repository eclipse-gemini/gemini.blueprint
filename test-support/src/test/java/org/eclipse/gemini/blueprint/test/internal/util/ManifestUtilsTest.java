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

package org.eclipse.gemini.blueprint.test.internal.util;

import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.test.internal.util.jar.JarUtils;
import org.eclipse.gemini.blueprint.test.internal.util.jar.ManifestUtils;
import org.eclipse.gemini.blueprint.test.internal.util.jar.storage.MemoryStorage;
import org.eclipse.gemini.blueprint.test.internal.util.jar.storage.Storage;
import org.osgi.framework.Constants;
import org.springframework.core.io.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 
 * @author Costin Leau
 */
public class ManifestUtilsTest extends TestCase {

	private Storage storage;

	private JarInputStream in;

	protected void setUp() throws Exception {
		storage = new MemoryStorage();
	}

	protected void tearDown() throws Exception {
		storage.dispose();
		IOUtils.closeStream(in);
	}

	public void testEmptyManifest() throws Exception {
		Manifest mf = new Manifest();
		mf.getMainAttributes().putValue("foo", "bar");
		createJar(mf);
		in = new JarInputStream(storage.getInputStream());
		assertEquals(mf, in.getManifest());
	}

	public void testJarUtilsReadResource() throws Exception {
		Manifest mf = new Manifest();
		mf.getMainAttributes().putValue("foo", "bar");
		createJar(mf);
		assertEquals(mf, JarUtils.getManifest(storage.getResource()));
	}

	public void testExportEntries() throws Exception {
		Manifest mf = new Manifest();
		Attributes attrs = mf.getMainAttributes();
		String[] packages = new String[] { "foo.bar; version:=1", "bar.foo", "hop.trop" };
		attrs.putValue(Constants.EXPORT_PACKAGE, StringUtils.arrayToCommaDelimitedString(packages));
		createJar(mf);
		String[] entries = ManifestUtils.determineImportPackages(new Resource[] { storage.getResource(),
				storage.getResource() });
		assertEquals(3, entries.length);
		ObjectUtils.nullSafeEquals(packages, entries);
	}

	private void createJar(Manifest mf) throws Exception {
		mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		JarOutputStream out = new JarOutputStream(storage.getOutputStream(), mf);
		out.flush();
		out.close();
	}
}
