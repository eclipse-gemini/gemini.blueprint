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

package org.eclipse.gemini.blueprint.context.internal.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.context.support.internal.classloader.ChainedClassLoader;
import org.osgi.framework.Bundle;

/**
 * @author Costin Leau
 */
public class ChainedClassLoaderTest extends TestCase {

	private ChainedClassLoader chainedLoader;
	private ClassLoader emptyCL;


	protected void setUp() throws Exception {
		emptyCL = new URLClassLoader(new URL[0], null) {

			public Class<?> loadClass(String name) throws ClassNotFoundException {
				throw new ClassNotFoundException(name);
			}

			public URL getResource(String name) {
				return null;
			}
		};

		chainedLoader = new ChainedClassLoader(new ClassLoader[] { emptyCL }, emptyCL);
	}

	protected void tearDown() throws Exception {
		chainedLoader = null;
		emptyCL = null;
	}

	public void testChainedClassLoaderClassLoaderArray() throws Exception {
		String className = "java.lang.Object";
		try {
			emptyCL.loadClass(className);
			fail("should not be able to load classes");
		}
		catch (ClassNotFoundException cnfe) {
			// expected
		}

		chainedLoader = new ChainedClassLoader(new ClassLoader[] { emptyCL });
		chainedLoader.loadClass(className);
	}

	public void testParentClassLoader() throws Exception {
		chainedLoader = new ChainedClassLoader(new ClassLoader[] { emptyCL });
		ClassLoader parent = chainedLoader.getParent();
		assertNotNull(parent);
		// fragile check (might fail on non SUN VMs)
		assertTrue("does the test run on a SUN VM or is it embedded?", parent.getClass().getName().indexOf("App") >= 0);
	}

	public void testChainedClassLoaderClassLoaderArrayClassLoader() throws Exception {
		String className = "java.lang.Object";

		try {
			emptyCL.loadClass(className);
			fail("should not be able to load classes");
		}
		catch (ClassNotFoundException cnfe) {
			// expected
		}

		try {
			chainedLoader.loadClass(className);
			fail("should not be able to load classes");
		}
		catch (ClassNotFoundException cnfe) {
			// expected
		}
	}

	public void testGetResourceString() throws Exception {
		assertNull(chainedLoader.getResource("java/lang/Object.class"));
		chainedLoader.addClassLoader(Object.class);
		assertNotNull(chainedLoader.getResource("java/lang/Object.class"));
	}

	public void testAddClassLoaderClass() throws Exception {
		chainedLoader.addClassLoader(Object.class);
		chainedLoader.loadClass("java.lang.Object");
	}

	public void testAddClassLoaderClassLoader() throws Exception {
		chainedLoader.addClassLoader(Bundle.class.getClassLoader());
		chainedLoader.loadClass("org.osgi.framework.Bundle");
	}

	public void testNonOSGiClassLoaderInsertOrder() throws Exception {
		ClassLoader appLoader = ClassLoader.getSystemClassLoader();
		ClassLoader extLoader = appLoader.getParent();

		chainedLoader.addClassLoader(extLoader);
		chainedLoader.addClassLoader(appLoader);

		// read the internal array
		List list = (List) TestUtils.getFieldValue(chainedLoader, "nonOsgiLoaders");

		// the loaders should be inserted based on their inheritance
		assertSame(appLoader, list.get(0));
		assertSame(extLoader, list.get(1));
	}
}