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

package org.eclipse.gemini.blueprint.io;

import java.net.URL;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.eclipse.gemini.blueprint.mock.ArrayEnumerator;
import org.osgi.framework.Bundle;
import org.springframework.core.io.Resource;

/**
 * @author Costin Leau
 * 
 */
public class OsgiBundleResourceLoaderTest extends TestCase {

	OsgiBundleResourceLoader loader;

	MockControl control;

	Bundle bundle;

	protected void setUp() throws Exception {
		control = MockControl.createStrictControl(Bundle.class);
		bundle = (Bundle) control.getMock();
		loader = new OsgiBundleResourceLoader(bundle);
	}

	protected void tearDown() throws Exception {
		loader = null;
		bundle = null;
		control = null;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResourceLoader#getResource(java.lang.String)}.
	 */
	public void testGetClasspathResource() throws Exception {
		String res = "foo.txt";
		URL expected = new URL("file://" + res);
		control.expectAndReturn(bundle.getResource(res), expected);
		control.replay();

		Resource resource = loader.getResource("classpath:" + res);
		assertNotNull(resource);
		assertSame(expected, resource.getURL());
		control.verify();
	}

	public void testGetBundleResource() throws Exception {
		String res = "foo.txt";
		URL url = new URL("file:/" + res);
		control.expectAndReturn(bundle.findEntries("/", res, false), new ArrayEnumerator(new URL[] {url}));
		control.replay();

		Resource resource = loader.getResource("osgibundle:/" + res);
		assertNotNull(resource);
		assertSame(url, resource.getURL());
		control.verify();
	}

	public void testGetRelativeResource() throws Exception {
		String res = "foo.txt";
		URL expected = new URL("file:/" + res);
		control.replay();

		Resource resource = loader.getResource("file:/" + res);
		assertNotNull(resource);
		assertEquals(expected, resource.getURL());
		control.verify();
	}

	public void testGetFallbackResource() throws Exception {
		String res = "foo.txt";
		URL expected = new URL("http:/" + res);
		control.replay();

		Resource resource = loader.getResource("http:/" + res);
		assertNotNull(resource);
		assertEquals(expected, resource.getURL());
		control.verify();
	}

	public void testGetResourceByPath() throws Exception {
		try {
			loader.getResourceByPath(null);
			fail("should have thrown exception");
		}
		catch (Exception ex) {
			// expected
		}
		String path = "foo";
		Resource res = loader.getResourceByPath(path);
		assertNotNull(res);
		assertSame(OsgiBundleResource.class, res.getClass());
		assertEquals(path, ((OsgiBundleResource) res).getPath());
	}
}
