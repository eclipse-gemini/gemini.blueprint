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

import java.net.URL;
import java.util.Enumeration;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;
import org.osgi.framework.Bundle;
import org.springframework.aop.framework.ProxyFactory;

/**
 * @author Costin Leau
 * 
 */
public class BundleDelegatingClassLoaderTest extends TestCase {

	private BundleDelegatingClassLoader classLoader;

	private MockControl bundleCtrl;

	private Bundle bundle;

	protected void setUp() throws Exception {
		bundleCtrl = MockControl.createStrictControl(Bundle.class);
		bundle = (Bundle) bundleCtrl.getMock();
		classLoader =
				BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle, ProxyFactory.class.getClassLoader());
		bundleCtrl.reset();
	}

	protected void tearDown() throws Exception {
		bundleCtrl.verify();
		classLoader = null;
		bundleCtrl = null;
		bundle = null;
	}

	public void tstEquals() {
		bundleCtrl.replay();

		assertFalse(classLoader.equals(new Object()));
		assertEquals(classLoader, classLoader);
		assertTrue(classLoader.equals(BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle, ProxyFactory.class
				.getClassLoader())));

		// assertEquals(bundle.hashCode(), clientClassLoader.hashCode());
	}

	public void testFindClass() throws Exception {
		String className = "foo.bar";
		String anotherClassName = "bar.foo";
		bundleCtrl.expectAndReturn(bundle.loadClass(className), Object.class);
		bundleCtrl.expectAndThrow(bundle.loadClass(anotherClassName), new ClassNotFoundException());
		bundleCtrl.expectAndReturn(bundle.getSymbolicName(), "Test Bundle Symbolic Name");
		//bundleCtrl.expectAndReturn(bundle.getHeaders(), new Properties());
		bundleCtrl.replay();

		assertSame(Object.class, classLoader.findClass(className));

		try {
			classLoader.findClass(anotherClassName);
		} catch (ClassNotFoundException ex) {
			// expected
		}
	}

	public void testFindResource() throws Exception {
		String resource = "file://bla-bla";
		URL url = new URL(resource);

		bundleCtrl.expectAndReturn(bundle.getResource(resource), url);
		bundleCtrl.replay();

		assertSame(url, classLoader.findResource(resource));
	}

	public void testFindResources() throws Exception {
		String resource = "bla-bla";
		MockControl enumCtrl = MockControl.createStrictControl(Enumeration.class);
		Enumeration enumeration = (Enumeration) enumCtrl.getMock();

		bundleCtrl.expectAndReturn(bundle.getResources(resource), enumeration);
		bundleCtrl.replay();

		assertSame(enumeration, classLoader.findResources(resource));
	}
}