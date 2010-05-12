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

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourceLoader;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourcePatternResolver;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.springframework.util.ObjectUtils;

/**
 * Integration test based on the bug report OSGI-799 regarding discovery of imported packages from the exporting bundle
 * w/o considering their bundle classpath.
 * 
 * The test will install two bundle, one with a custom classpath exporting a package and the other importing that
 * particular package.
 * 
 * @author Costin Leau
 */
public class OSGI799Test extends BaseIoTest {

	private static boolean customBundlesInstalled = false;
	private static final String EXPORT_BND = "org.eclipse.gemini.blueprint.bundle.osgi.io.test.osgi799.exp";
	private static final String IMPORT_BND = "org.eclipse.gemini.blueprint.bundle.osgi.io.test.osgi799.imp";

	/**
	 * No dependencies installed (we'll do this manually for this test).
	 */
	protected String[] getTestBundlesNames() {
		return new String[0];
	}

	protected String getManifestLocation() {
		return null;
	}

	protected void preProcessBundleContext(BundleContext platformBundleContext) throws Exception {
		super.preProcessBundleContext(platformBundleContext);
		if (!customBundlesInstalled) {
			logger.info("Installing custom bundles...");
			InputStream stream = getClass().getResourceAsStream("/osgi-799-exp.jar");
			assertNotNull(stream);
			Bundle bundle = platformBundleContext.installBundle("osgi-799-exp", stream);
			bundle.start();

			stream = getClass().getResourceAsStream("/osgi-799-imp.jar");
			bundle = platformBundleContext.installBundle("osgi-799-imp", stream);
			bundle.start();

			customBundlesInstalled = true;
		}
	}

	protected String[] getBundleContentPattern() {
		return (String[]) ObjectUtils.addObjectToArray(super.getBundleContentPattern(), "bundleclasspath/**/*");
	}

	/**
	 * Resolves the pattern resolved for the exporting bundle using a custom classpath.
	 * 
	 * @return
	 */
	protected ResourcePatternResolver getExporterPatternLoader() {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, EXPORT_BND);
		ResourceLoader loader = new OsgiBundleResourceLoader(bundle);
		return new OsgiBundleResourcePatternResolver(loader);
	}

	protected ResourcePatternResolver getImporterPatternLoader() {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, IMPORT_BND);
		ResourceLoader loader = new OsgiBundleResourceLoader(bundle);
		return new OsgiBundleResourcePatternResolver(loader);
	}

	public void testExportedCustomCP() throws Exception {
		ResourcePatternResolver resolver = getExporterPatternLoader();
		Resource[] resources = resolver.getResources("classpath:/some/**/*.res");
		System.out.println(Arrays.toString(resources));
		assertEquals(3, resources.length);
	}

	public void testImportedCustomCP() throws Exception {
		ResourcePatternResolver resolver = getImporterPatternLoader();
		Resource[] resources = resolver.getResources("classpath:some/**/*.res");
		System.out.println(Arrays.toString(resources));
		assertEquals(3, resources.length);
	}
	
	public void testExportedCustomFoldersCP() throws Exception {
		ResourcePatternResolver resolver = getExporterPatternLoader();
		Resource[] resources = resolver.getResources("classpath:/**/path/**/*");
		System.out.println(Arrays.toString(resources));
		assertEquals(8, resources.length);
	}
	
	public void testImporterCustomFoldersCP() throws Exception {
		ResourcePatternResolver resolver = getImporterPatternLoader();
		Resource[] resources = resolver.getResources("classpath:/**/path/**/*");
		System.out.println(Arrays.toString(resources));
		assertEquals(5, resources.length);
	}

	public void testExportedCustomPatternFoldersCP() throws Exception {
		ResourcePatternResolver resolver = getExporterPatternLoader();
		Resource[] resources = resolver.getResources("classpath:/**/p?th/**/*");
		System.out.println(Arrays.toString(resources));
		assertEquals(8, resources.length);
	}
	
	public void testImporterCustomPatternFoldersCP() throws Exception {
		ResourcePatternResolver resolver = getImporterPatternLoader();
		Resource[] resources = resolver.getResources("classpath:/**/p?th/**/*");
		System.out.println(Arrays.toString(resources));
		assertEquals(5, resources.length);
	}
}