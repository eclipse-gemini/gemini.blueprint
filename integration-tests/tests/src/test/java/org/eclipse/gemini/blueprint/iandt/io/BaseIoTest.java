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

import java.io.FilePermission;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourceLoader;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourcePatternResolver;
import org.springframework.util.ObjectUtils;

/**
 * Common base test class for IO integration testing.
 * 
 * @author Costin Leau
 * 
 */
public abstract class BaseIoTest extends BaseIntegrationTest {

	protected final static String PACKAGE = "org/eclipse/gemini/blueprint/iandt/io/";
	private static final String FRAGMENT_1 = "org.eclipse.gemini.blueprint.iandt.io.fragment.1";
	private static final String FRAGMENT_2 = "org.eclipse.gemini.blueprint.iandt.io.fragment.2";

	protected Resource thisClass;

	protected ResourceLoader loader, defaultLoader;

	protected ResourcePatternResolver patternLoader;

	protected Bundle bundle;


	protected String[] getBundleContentPattern() {
		return (String[]) ObjectUtils.addObjectToArray(super.getBundleContentPattern(),
			"org/eclipse/gemini/blueprint/iandt/io/BaseIoTest.class");
	}

	protected void onSetUp() throws Exception {
		// load file using absolute path
		defaultLoader = new DefaultResourceLoader();
		thisClass = defaultLoader.getResource(getClass().getName().replace('.', '/').concat(".class"));
		bundle = bundleContext.getBundle();
		loader = new OsgiBundleResourceLoader(bundle);
		patternLoader = new OsgiBundleResourcePatternResolver(loader);

	}

	protected void onTearDown() throws Exception {
		thisClass = null;
	}

	protected String getManifestLocation() {
		// reuse the manifest from Fragment Io Tests
		return "org/eclipse/gemini/blueprint/iandt/io/FragmentIoTests.MF";
	}

	/**
	 * Add a bundle fragment.
	 */
	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt,io.fragment.1.bundle," + getSpringDMVersion(),
			"org.eclipse.gemini.blueprint.iandt,io.fragment.2.bundle," + getSpringDMVersion() };
	}

	protected Object[] copyEnumeration(Enumeration enm) {
		List list = new ArrayList();
		while (enm != null && enm.hasMoreElements())
			list.add(enm.nextElement());
		return list.toArray();
	}

	protected void assertResourceArray(Object[] array, int expectedSize) {
		System.out.println(ObjectUtils.nullSafeToString(array));
		assertTrue("found only " + ObjectUtils.nullSafeToString(array), array.length == expectedSize);
	}

	protected boolean isKF() {
		return (createPlatform().toString().startsWith("Knopflerfish"));
	}

	protected boolean isEquinox() {
		return (createPlatform().toString().startsWith("Equinox"));
	}

	protected boolean isFelix() {
		return (createPlatform().toString().startsWith("Felix"));
	}

	protected List getTestPermissions() {
		List list = super.getTestPermissions();
		list.add(new FilePermission("<<ALL FILES>>", "read"));
		// log files
		list.add(new FilePermission("<<ALL FILES>>", "delete"));
		list.add(new FilePermission("<<ALL FILES>>", "write"));
		list.add(new AdminPermission("*", AdminPermission.LISTENER));
		list.add(new AdminPermission("(name=" + FRAGMENT_1 + ")", AdminPermission.RESOURCE));
		list.add(new AdminPermission("(name=" + FRAGMENT_2 + ")", AdminPermission.RESOURCE));
		return list;
	}

	protected void printPathWithinContext(Resource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			Resource resource = resources[i];
			assertTrue(resource instanceof ContextResource);
			// Disabled print out
			//System.out.println("Path within context " + ((ContextResource) resource).getPathWithinContext());
		}
	}
}