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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourcePatternResolver;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.junit.Test;

/**
 * @author Costin Leau
 * 
 */
public class EquinoxFileTests extends BaseIoTest {

	private static final String REFERENCE_PROTOCOL = "reference:file:";
	private static final String EXPANDED_BUNDLE_SYM_NAME = "org.eclipse.gemini.blueprint.iandt.io.expanded.bundle";

	@Test
	public void testResolveResourceWithFilePrefix() throws Exception {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, EXPANDED_BUNDLE_SYM_NAME);
		assertTrue(bundle.getLocation().startsWith(REFERENCE_PROTOCOL));
		ResourcePatternResolver resolver = new OsgiBundleResourcePatternResolver(bundle);
		Resource res = resolver.getResource("resource.res");
		assertTrue(res.getFile().exists());
	}

	@Test
	public void testResolveResourceWithReferenceFilePrefix() throws Exception {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, EXPANDED_BUNDLE_SYM_NAME);
		assertTrue(bundle.getLocation().startsWith(REFERENCE_PROTOCOL));
		assertNotNull(new URL(bundle.getLocation()).getFile());
		ResourcePatternResolver resolver = new OsgiBundleResourcePatternResolver(bundle);
		Resource res = resolver.getResource("/META-INF/MANIFEST.MF");
		assertTrue(res.getFile().exists());
	}

	protected void postProcessBundleContext(BundleContext context) throws Exception {
		super.postProcessBundleContext(context);
		File expandedBundle = new File(".", "target/test-classes/expanded-bundle.jar");
		System.out.println("Installing expanded bundle from " + expandedBundle.getCanonicalPath());
		context.installBundle(REFERENCE_PROTOCOL + expandedBundle.getCanonicalPath());
	}

	public boolean isDisabledInThisEnvironment(String testMethodName) {
		return !isEquinox();
	}
}
