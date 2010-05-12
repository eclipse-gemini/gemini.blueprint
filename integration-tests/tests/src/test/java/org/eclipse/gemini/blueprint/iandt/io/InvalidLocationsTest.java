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

import java.io.IOException;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourceLoader;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourcePatternResolver;
import org.springframework.util.ObjectUtils;

/**
 * @author Costin Leau
 * 
 */
public class InvalidLocationsTest extends BaseIoTest {
	private static final String NON_EXISTING = "/non-existing";

	private ResourceLoader osgiRL;

	private ResourcePatternResolver osgiRPR;

	protected void onSetUp() throws Exception {
		super.onSetUp();
		osgiRL = new OsgiBundleResourceLoader(bundle);
		osgiRPR = new OsgiBundleResourcePatternResolver(bundle);
	}

	protected void onTearDown() throws Exception {
		super.onTearDown();
		osgiRL = null;
		osgiRPR = null;
	}

	protected String getManifestLocation() {
		return null;
	}

	protected String[] getTestBundlesNames() {
		return null;
	}

	public void testDefaultClassLoader() throws Exception {
		testOneResource(new DefaultResourceLoader());
	}

	public void testOsgiResourceLoader() throws Exception {
		testOneResource(osgiRL);
	}

	private void testOneResource(ResourceLoader loader) {
		Resource res = loader.getResource(NON_EXISTING);
		assertFalse(res.exists());
	}

	public void testDefaultPatternResourceLoader() throws Exception {
		testMultipleResources(new PathMatchingResourcePatternResolver());
	}

	public void testPatternResourceLoader() throws Exception {
		testMultipleResources(osgiRPR);
	}

	private void testMultipleResources(ResourcePatternResolver loader) throws IOException {
		Resource[] res = loader.getResources(NON_EXISTING);
		assertEquals("invalid resource array " + ObjectUtils.nullSafeToString(res), 1, res.length);
	}

}
