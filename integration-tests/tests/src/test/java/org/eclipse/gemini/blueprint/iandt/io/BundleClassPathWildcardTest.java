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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourcePatternResolver;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.junit.Test;
import org.springframework.util.ObjectUtils;

/**
 * Test wildcard matching on bundles with a defined bundle classpath. This is
 * one of the heaviest IO tests as it involves both a bundle classpath and
 * fragments.
 * 
 * @author Costin Leau
 * 
 */
public class BundleClassPathWildcardTest extends BaseIoTest {

	private static boolean noRootCPTestBundleInstalled = false;
	private static final String NO_ROOT_BND_SYM = "org.eclipse.gemini.blueprint.bundle.osgi.io.test.no.root.classpath";


	protected Manifest getManifest() {
		Manifest mf = super.getManifest();
		// add bundle classpath
		mf.getMainAttributes().putValue(Constants.BUNDLE_CLASSPATH,
			".,bundleclasspath/folder/,bundleclasspath/simple.jar,foo");
		return mf;
	}

	protected void preProcessBundleContext(BundleContext platformBundleContext) throws Exception {
		super.preProcessBundleContext(platformBundleContext);
		if (!noRootCPTestBundleInstalled) {
			logger.info("Installing no root cp bundle...");
			InputStream stream = getClass().getResourceAsStream("/norootcpbundle.jar");
			Bundle bundle = platformBundleContext.installBundle("norootcpbundle", stream);
			bundle.start();
			noRootCPTestBundleInstalled = true;
		}
	}

	protected String[] getBundleContentPattern() {
		return (String[]) ObjectUtils.addObjectToArray(super.getBundleContentPattern(), "bundleclasspath/**/*");
	}

	@Test
	public void testClassPathFilesOnBundleClassPath() throws Exception {
		// use org to make sure the bundle class is properly considered (especially for folder based classpath)
		Resource[] res = patternLoader.getResources("classpath:org/**/*.file");
		System.out.println("array count is " + res.length);
		System.out.println(ObjectUtils.nullSafeToString(res));
		printPathWithinContext(res);
		assertTrue("bundle classpath jar not considered", containsString(res, "jar-folder.file"));
	}

	@Test
	public void testAllClassPathFilesOnBundleClassPath() throws Exception {
		// use org to make sure the bundle class is properly considered (especially for folder based classpath)
		Resource[] res = patternLoader.getResources("classpath*:org/**/*.file");
		System.out.println("array count is " + res.length);
		System.out.println(ObjectUtils.nullSafeToString(res));
		printPathWithinContext(res);
		assertTrue("bundle classpath jar not considered", containsString(res, "jar-folder.file"));
	}

	@Test
	public void testRootFileOnBundleClassPath() throws Exception {
		// use org to make sure the bundle class is properly considered (especially for folder based classpath)
		Resource[] res = patternLoader.getResources("classpath:*.file");
		System.out.println("array count is " + res.length);
		System.out.println(ObjectUtils.nullSafeToString(res));
		printPathWithinContext(res);
		assertTrue("bundle classpath jar not considered", containsString(res, "jar.file"));
	}

	@Test
	public void testRootFileOnAllBundleClassPath() throws Exception {
		// use org to make sure the bundle class is properly considered (especially for folder based classpath)
		Resource[] res = patternLoader.getResources("classpath:*.file");
		System.out.println("array count is " + res.length);

		System.out.println(ObjectUtils.nullSafeToString(res));
		printPathWithinContext(res);
		assertTrue("bundle classpath jar not considered", containsString(res, "jar.file"));
	}

	private boolean containsString(Resource[] array, String str) throws IOException {
		for (int i = 0; i < array.length; i++) {
			Resource resource = array[i];
			if (resource.getURL().toExternalForm().indexOf(str) > -1)
				return true;
		}
		return false;
	}

	@Test
	public void testURLConnectionToJarInsideBundle() throws Exception {
		Resource jar = patternLoader.getResource("bundleclasspath/simple.jar");
		testJarConnectionOn(jar);
	}

	@Test
	private void testJarConnectionOn(Resource jar) throws Exception {
		String toString = jar.getURL().toExternalForm();
		// force JarURLConnection
		String urlString = "jar:" + toString + "!/";
		URL newURL = new URL(urlString);
		System.out.println(newURL);
		System.out.println(newURL.toExternalForm());
		URLConnection con = newURL.openConnection();
		System.out.println(con);
		System.out.println(con instanceof JarURLConnection);
		JarURLConnection jarCon = (JarURLConnection) con;

		JarFile jarFile = jarCon.getJarFile();
		System.out.println(jarFile.getName());
		Enumeration enm = jarFile.entries();
		while (enm.hasMoreElements())
			System.out.println(enm.nextElement());
	}

	@Test
	public void testResourceAvailableOnlyInsideJarClasspath() throws Exception {
		Resource[] resources = patternLoader.getResources("classpath*:jar.file");
		assertNotNull(resources);
		System.out.println("Arrays inside the jar");
		printPathWithinContext(resources);
		assertEquals(1, resources.length);
		assertTrue(resources[0].exists());
	}

	@Test
	public void testResourceAvailableOnlyInsideFolderClasspath() throws Exception {
		Resource[] resources = patternLoader.getResources("classpath*:org/eclipse/gemini/blueprint/iandt/compliance/io/folder-test.file");
		assertNotNull(resources);
		assertEquals(1, resources.length);
		assertTrue(resources[0].exists());
		System.out.println("Arrays inside the classpath folder");
		System.out.println(ObjectUtils.nullSafeToString((resources)));
		printPathWithinContext(resources);
	}

	@Test
	public void testResourceAvailableWithPatternOnPathsOnlyInsideFolderClasspath() throws Exception {
		Resource[] resources = patternLoader.getResources("classpath*:org/eclipse/gemini/blueprint/iandt/**/folder-test.file");
		assertNotNull(resources);
		assertEquals(1, resources.length);
		assertTrue(resources[0].exists());
		System.out.println(ObjectUtils.nullSafeToString((resources)));
		printPathWithinContext(resources);
	}

	@Test
	public void testResourceAvailableWithPatternOnlyInsideFolderClasspath() throws Exception {
		Resource[] resources = patternLoader.getResources("classpath:org/eclipse/gemini/blueprint/iandt/**/folder-test.file");
		assertNotNull(resources);
		assertEquals(1, resources.length);
		assertTrue(resources[0].exists());
		System.out.println(ObjectUtils.nullSafeToString((resources)));
		printPathWithinContext(resources);
	}

	private ResourcePatternResolver getNoRootCpBundleResourceResolver() {
		Bundle bnd = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, NO_ROOT_BND_SYM);
		assertNotNull("noRootClassPath bundle was not found", bnd);
		return new OsgiBundleResourcePatternResolver(bnd);
	}

	@Test
	public void testNoRootCpBundleResourceInRootNotFound() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath:root.file");
		// since there is no pattern matching, the loader will return a non-existing resource
		assertFalse("resource should not be found since / is not in the classpath", res[0].exists());
		System.out.println("classpath:root.file resources");
		printPathWithinContext(res);
	}

	@Test
	public void testNoRootCpBundleResourceInRootNotFoundOnAllClasspaths() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:root.file");
		assertTrue("resource should not be found since / is not in the classpath", ObjectUtils.isEmpty(res));
		System.out.println("root.file resources");
		System.out.println("classpath*:root.file resources");
		printPathWithinContext(res);
	}

	@Test
	public void testNoRootCpBundleResourceInClassPathFound() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:cp.file");
		assertFalse("resource should be found since it's on the classpath", ObjectUtils.isEmpty(res));
		assertTrue("resource should be found since it's on the classpath", res[0].exists());
		System.out.println("classpath*:cp.file resources");
		printPathWithinContext(res);
	}

	@Test
	public void testNoRootCpBundleResourceNestedInClassPathFound() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:/some/nested/nested.file");
		assertFalse("resource should be found since it's on the classpath", ObjectUtils.isEmpty(res));
		assertTrue("resource should be found since it's on the classpath", res[0].exists());
		System.out.println("classpath*:/some/nested/nested.file resources");
		printPathWithinContext(res);
	}

	@Test
	public void testNoRootCpBundleResourceNestedInPkgInClassPathFound() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:/some/nested/pkg/pkg.file");
		assertFalse("resource should be found since it's on the classpath", ObjectUtils.isEmpty(res));
		assertTrue("resource should be found since it's on the classpath", res[0].exists());
	}

	@Test
	public void testNoRootCpBundleResourcePatternMatching() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath:/**/*.file");
		assertEquals("incorrect number of resources found", 3, res.length);
	}

	@Test
	public void testNoRootCpBundleResourceMultipleRoots() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:/**/*.file");
		assertEquals("incorrect number of resources found", 3, res.length);
	}

	@Test
	public void testNoRootCpBundleResourcePatternMatchingWithSpecifiedFolder() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath:/some/**/*.file");
		assertEquals("incorrect number of resources found", 2, res.length);
	}

	@Test
	public void testNoRootCpBundleResourceMultipleRootsSpecifiedFolder() throws Exception {
		ResourcePatternResolver resolver = getNoRootCpBundleResourceResolver();
		Resource[] res = resolver.getResources("classpath*:/some/**/*.file");
		assertEquals("incorrect number of resources found", 2, res.length);
	}

	protected List getTestPermissions() {
		List list = super.getTestPermissions();
		list.add(new AdminPermission("(name=" + NO_ROOT_BND_SYM + ")", AdminPermission.METADATA));
		list.add(new AdminPermission("(name=" + NO_ROOT_BND_SYM + ")", AdminPermission.RESOURCE));
		return list;
	}
}
