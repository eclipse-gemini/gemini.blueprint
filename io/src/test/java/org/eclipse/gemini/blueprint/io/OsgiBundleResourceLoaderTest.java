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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.eclipse.gemini.blueprint.mock.ArrayEnumerator;
import org.osgi.framework.Bundle;
import org.springframework.core.io.Resource;

/**
 * @author Costin Leau
 * 
 */
public class OsgiBundleResourceLoaderTest {

	OsgiBundleResourceLoader loader;

	Bundle bundle;

	@Before
	public void setup() throws Exception {
		bundle = createMock(Bundle.class);
		loader = new OsgiBundleResourceLoader(bundle);
	}

	@After
	public void tearDown() throws Exception {
		loader = null;
		bundle = null;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResourceLoader#getResource(java.lang.String)}.
	 */
	@Test
	public void testGetClasspathResource() throws Exception {
		String res = "foo.txt";
		URL expected = new URL("file://" + res);
		expect(bundle.getResource(res)).andReturn(expected);
		replay(bundle);

		Resource resource = loader.getResource("classpath:" + res);
		assertNotNull(resource);
		assertSame(expected, resource.getURL());
		verify(bundle);
	}

	@Test
	public void testGetBundleResource() throws Exception {
		String res = "foo.txt";
		URL url = new URL("file:/" + res);
		expect(bundle.findEntries("/", res, false)).andReturn(new ArrayEnumerator(new URL[] {url}));
		replay(bundle);

		Resource resource = loader.getResource("osgibundle:/" + res);
		assertNotNull(resource);
		assertSame(url, resource.getURL());
		verify(bundle);
	}

	@Test
	public void testGetRelativeResource() throws Exception {
		String res = "foo.txt";
		URL expected = new URL("file:/" + res);
		replay(bundle);

		Resource resource = loader.getResource("file:/" + res);
		assertNotNull(resource);
		assertEquals(expected, resource.getURL());
		verify(bundle);
	}

	@Test
	public void testGetFallbackResource() throws Exception {
		String res = "foo.txt";
		URL expected = new URL("http:/" + res);
		replay(bundle);

		Resource resource = loader.getResource("http:/" + res);
		assertNotNull(resource);
		assertEquals(expected, resource.getURL());
		verify(bundle);
	}

	@Test
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
