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

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.osgi.framework.Bundle;
import org.springframework.aop.framework.ProxyFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import static org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader.createBundleClassLoaderFor;

/**
 * @author Costin Leau
 * 
 */
public class BundleDelegatingClassLoaderTest extends TestCase {

	private BundleDelegatingClassLoader classLoader;

	private Bundle bundle;

    private ClassLoader bridge;

	protected void setUp() throws Exception {
		bundle = createMock(Bundle.class);
		classLoader = createBundleClassLoaderFor(bundle, ProxyFactory.class.getClassLoader());
        bridge = getClass().getClassLoader();
	}

	protected void tearDown() throws Exception {
		verify(bundle);
		classLoader = null;
		bundle = null;
	}

	public void tstEquals() {
		replay(bundle);

		assertFalse(classLoader.equals(new Object()));
		assertEquals(classLoader, classLoader);
		assertTrue(classLoader.equals(createBundleClassLoaderFor(bundle, ProxyFactory.class
                .getClassLoader())));

		// assertEquals(bundle.hashCode(), clientClassLoader.hashCode());
	}

	public void testFindClass() throws Exception {
		String className = "foo.bar";
		String anotherClassName = "bar.foo";
		expect(bundle.loadClass(className)).andReturn((Class)Object.class);
        
		expect(bundle.loadClass(anotherClassName)).andThrow(new ClassNotFoundException());
	    expect(bundle.getSymbolicName()).andReturn("Test Bundle Symbolic Name");
		replay(bundle);

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

		expect(bundle.getResource(resource)).andReturn(url);
		replay(bundle);

		assertSame(url, classLoader.findResource(resource));
	}

	public void testFindResources() throws Exception {
		String resource = "bla-bla";
        Enumeration enumeration = createMock(Enumeration.class);

		expect(bundle.getResources(resource)).andReturn(enumeration);
		replay(bundle);

		assertSame(enumeration, classLoader.findResources(resource));
	}

    public void testGetResourcesFromBundleAndBridge() throws Exception {
        final String resourceName = "org/eclipse/gemini/blueprint/util/internal/resource.txt";
        final URL bundleURL = new URL("file://bundle/resourceName");

        Enumeration bundleResources = createMock(Enumeration.class);
        
        expect(bundle.getResources(resourceName)).andReturn(bundleResources);
        expect(bundleResources.hasMoreElements()).andReturn(true).times(2);
        expect(bundleResources.nextElement()).andReturn(bundleURL);
        expect(bundleResources.hasMoreElements()).andReturn(false).times(2);
        
        replay(bundleResources, bundle);

        Enumeration<URL> resources = createBundleClassLoaderFor(bundle, bridge).getResources(resourceName);

        assertTrue(resources.hasMoreElements());
        assertSame(bundleURL, resources.nextElement());

        assertTrue(resources.hasMoreElements());
        URL resource = resources.nextElement();
        assertNotNull(resource);
        assertTrue(resource.getFile().endsWith(resourceName));
    }

    public void testGetResourcesFromBundleOnly() throws Exception {
        final String resourceName = "org/eclipse/gemini/blueprint/util/internal/resource.txt";
        final URL bundleURL = new URL("file://bundle/resourceName");

        Enumeration bundleResources = createMock(Enumeration.class);

        expect(bundle.getResources(resourceName)).andReturn( bundleResources);
        expect(bundleResources.hasMoreElements()).andReturn(true);
        expect(bundleResources.nextElement()).andReturn( bundleURL);
        expect(bundleResources.hasMoreElements()).andReturn( false);

        replay(bundleResources, bundle);

        Enumeration<URL> resources = createBundleClassLoaderFor(bundle, null).getResources(resourceName);

        assertTrue(resources.hasMoreElements());
        assertSame(bundleURL, resources.nextElement());
        assertFalse(resources.hasMoreElements());
    }

    public void testGetResourcesFromBridgeOnly() throws Exception {
        final String resourceName = "org/eclipse/gemini/blueprint/util/internal/resource.txt";

        expect(bundle.getResources(resourceName)).andReturn(null);
        replay(bundle);

        Enumeration<URL> resources = createBundleClassLoaderFor(bundle, bridge).getResources(resourceName);
        assertTrue(resources.hasMoreElements());
        URL resource = resources.nextElement();
        assertNotNull(resource);
        assertTrue(resource.getFile().endsWith(resourceName));
        assertFalse(resources.hasMoreElements());
    }

    public void testGetResourcesIsNullSafe() throws IOException {
        final String resourceName = "org/eclipse/gemini/blueprint/util/internal/resource.txt";

        expect(bundle.getResources(resourceName)).andReturn(null);
        replay(bundle);

        Enumeration<URL> resources = createBundleClassLoaderFor(bundle, null).getResources(resourceName);

        assertNotNull(resources);
        assertFalse(resources.hasMoreElements());
    }
}