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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.gemini.blueprint.mock.ArrayEnumerator;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * @author Costin Leau
 * 
 */
public class OsgiBundleResourceTest {

	private OsgiBundleResource resource;

	private Bundle bundle;

	private String path;

	@Before
	public void setup() throws Exception {
		path = OsgiBundleResourceTest.class.getName().replace('.', '/').concat(".class");
		bundle = new MockBundle();
		resource = new OsgiBundleResource(bundle, path);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		assertEquals(path.hashCode(), resource.hashCode());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#OsgiBundleResource(org.osgi.framework.Bundle, java.lang.String)}.
	 */
	@Test
	public void testOsgiBundleResource() {
		assertSame(bundle, resource.getBundle());
		assertEquals(path, resource.getPath());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#getPath()}.
	 */
	@Test
	public void testGetPath() {
		assertEquals(path, resource.getPath());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#getBundle()}.
	 */
	@Test
	public void testGetBundle() {
		assertSame(bundle, resource.getBundle());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#getInputStream()}.
	 */
	@Test
	public void testGetInputStream() throws Exception {
		InputStream stream = resource.getInputStream();
		assertNotNull(stream);
		stream.close();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#getURL()}.
	 */
	@Test
	public void testGetURL() throws Exception {
		assertNotNull(resource.getURL());

		resource = new OsgiBundleResource(bundle, "osgibundle:foo" + path);
		try {
			resource.getURL();
			fail("should have thrown exception");
		}
		catch (Exception ex) {
			// expected
		}
	}

	@Test
	public void testNonBundleUrlWhichExists() throws Exception {
		File tmp = File.createTempFile("foo", "bar");
		tmp.deleteOnExit();
		resource = new OsgiBundleResource(bundle, "file:" + tmp.toString());
		assertNotNull(resource.getURL());
		assertTrue(resource.exists());
		tmp.delete();
	}

	@Test
	public void testNonBundleUrlWhichDoesNotExist() throws Exception {
		resource = new OsgiBundleResource(bundle, "file:foo123123");
		resource.getURL();
		assertFalse(resource.exists());
	}

	@Test
	public void testFileWithSpecialCharsInTheNameBeingResolved() throws Exception {
        String name = Thread.currentThread().getContextClassLoader().getResource( "test-file" ).toString();
		FileSystemResourceLoader fileLoader = new FileSystemResourceLoader();
		fileLoader.setClassLoader(getClass().getClassLoader());

		Resource fileRes = fileLoader.getResource(name);
		resource = new OsgiBundleResource(bundle, name);

		testFileVsOsgiFileResolution(fileRes, resource);
	}

	@Test
	public void testFileWithEmptyCharsInTheNameBeingResolved() throws Exception {
		String name = Thread.currentThread().getContextClassLoader().getResource( "test file" ).toString();
		FileSystemResourceLoader fileLoader = new FileSystemResourceLoader();
		fileLoader.setClassLoader(getClass().getClassLoader());

		Resource fileRes = fileLoader.getResource(name);
		resource = new OsgiBundleResource(bundle, name);

		testFileVsOsgiFileResolution(fileRes, resource);
	}

	@Test
	public void testFileWithNormalCharsInTheNameBeingResolved() throws Exception {
		String name = Thread.currentThread().getContextClassLoader().getResource( "normal" ).toString();
		FileSystemResourceLoader fileLoader = new FileSystemResourceLoader();
		fileLoader.setClassLoader(getClass().getClassLoader());

		Resource fileRes = fileLoader.getResource(name);

		resource = new OsgiBundleResource(bundle, name);
		testFileVsOsgiFileResolution(fileRes, resource);
	}

	private void testFileVsOsgiFileResolution(Resource fileRes, Resource otherRes) throws Exception {
		assertNotNull(fileRes.getURL());
		assertNotNull(fileRes.getFile());
		assertTrue(fileRes.getFile().exists());

		assertNotNull(otherRes.getURL());
		assertNotNull(otherRes.getFile());
		assertTrue(StringUtils.pathEquals(fileRes.getFile().getAbsolutePath(), otherRes.getFile().getAbsolutePath()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#getResourceFromBundleSpace(java.lang.String)}.
	 */
	@Test
	public void testGetResourceFromBundle() throws Exception {
		Bundle mock = createMock(Bundle.class);

		String location = "foo";
		URL result = new URL("file:/" + location);

		expect(mock.findEntries("/", "foo", false)).andReturn(new ArrayEnumerator(new URL[] { result }));
		replay(mock);

		resource = new OsgiBundleResource(mock, location);

		assertEquals(result, resource.getResourceFromBundleSpace(location).getURL());
		verify(mock);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#getResourceFromBundleClasspath(java.lang.String)}.
	 */
	@Test
	public void testGetResourceFromBundleClasspath() throws Exception {
		Bundle mock = createMock(Bundle.class);

		String location = "file://foo";
		URL result = new URL(location);

		expect(mock.getResource(location)).andReturn(result);
		replay(mock);

		resource = new OsgiBundleResource(mock, location);

		assertSame(result, resource.getResourceFromBundleClasspath(location));
		verify(mock);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#isRelativePath(java.lang.String)}.
	 */
	@Test
	public void testIsRelativePath() {
		assertTrue(resource.isRelativePath("foo"));
		assertFalse(resource.isRelativePath("/foo"));
		assertFalse(resource.isRelativePath(":foo"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#createRelative(java.lang.String)}.
	 */
	@Test
	public void testCreateRelativeString() {
		String location = "foo";
		Resource res = resource.createRelative(location);
		assertSame(OsgiBundleResource.class, res.getClass());
		assertEquals("org/eclipse/gemini/blueprint/io/" + location, ((OsgiBundleResource) res).getPath());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#getFilename()}.
	 */
	@Test
	public void testGetFilename() {
		assertNotNull(resource.getFilename());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#getDescription()}.
	 */
	@Test
	public void testGetDescription() {
		assertNotNull(resource.getDescription());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.io.OsgiBundleResource#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		assertEquals(resource, new OsgiBundleResource(bundle, path));
		assertEquals(resource, resource);
		assertFalse(resource.equals(new OsgiBundleResource(bundle, "")));
		assertFalse(resource.equals(new OsgiBundleResource(new MockBundle(), path)));
	}

	@Test
	public void testDefaultPathWithinContext() throws Exception {
		assertEquals(path, resource.getPathWithinContext());
	}

	@Test
	public void testPathWithinBundleSpace() throws Exception {
		String contextPath = "folder/resource";
		resource = new OsgiBundleResource(bundle, "osgibundle:" + contextPath);
		assertEquals(contextPath, resource.getPathWithinContext());
	}

	@Test
	public void testPathWithinClassSpace() throws Exception {
		String contextPath = "folder/resource";
		resource = new OsgiBundleResource(bundle, "classpath:" + contextPath);
		assertEquals(contextPath, resource.getPathWithinContext());
	}

	@Test
	public void testPathWithinJarSpace() throws Exception {
		String contextPath = "folder/resource";
		resource = new OsgiBundleResource(bundle, "osgibundlejar:" + contextPath);
		assertEquals(contextPath, resource.getPathWithinContext());
	}

	@Test
	public void testPathOutsideContext() throws Exception {
		String contextPath = "folder/resource";
		resource = new OsgiBundleResource(bundle, "file:" + contextPath);
		assertNull(resource.getPathWithinContext());
	}

	@Test
	public void testLastModified() throws Exception {
		assertTrue("last modified should be non zero", resource.lastModified() > 0);
	}
	
	@Test
	public void testNonExistingFile() throws Exception {
		resource = new OsgiBundleResource(bundle, "file:/some/non.existing.file");
		File file = resource.getFile();
		assertNotNull(file);
		assertFalse(file.exists());
	}
}