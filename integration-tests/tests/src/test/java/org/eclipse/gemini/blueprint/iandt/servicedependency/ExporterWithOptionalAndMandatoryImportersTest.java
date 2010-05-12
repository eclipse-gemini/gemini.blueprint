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

package org.eclipse.gemini.blueprint.iandt.servicedependency;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.Assert;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.core.io.Resource;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;

/**
 * @author Costin Leau
 */
public class ExporterWithOptionalAndMandatoryImportersTest extends BaseIntegrationTest {

	private static final String DEP_SYN_NAME = "org.eclipse.gemini.blueprint.iandt.dependency.exporter.importer";

	private ServiceRegistration optional, mandatory;

	protected void postProcessBundleContext(BundleContext context) throws Exception {
		super.postProcessBundleContext(context);
		installTestBundle(context);
	}

	protected void onSetUp() throws Exception {
		registerOptional();
		registerMandatory();
	}

	protected void onTearDown() throws Exception {
		Bundle bnd = getDependencyBundle();
		bnd.stop();
		OsgiServiceUtils.unregisterService(mandatory);
		OsgiServiceUtils.unregisterService(optional);
	}

	public void testInjectedDependencies() throws Exception {
		Bundle bnd = getDependencyBundle();
		bnd.start();

		logger.info("Waiting for the test bundle to start up...");
		waitOnContextCreation(DEP_SYN_NAME);
		logger.info("Test bundle context created - starting test...");
		
		assertTrue("exporter not alive on startup", isInjectedExporterAlive());
		optional.unregister();
		assertTrue("exporter affected by the optional dependency", isInjectedExporterAlive());
		mandatory.unregister();
		assertFalse("exporter not affected by the optional dependency", isInjectedExporterAlive());
		registerOptional();
		assertFalse("exporter affected by the optional dependency", isInjectedExporterAlive());
		registerMandatory();
		assertTrue("exporter not affected by the optional dependency", isInjectedExporterAlive());
		optional.unregister();
		assertTrue("exporter affected by the optional dependency", isInjectedExporterAlive());
	}

	public void testDependsOnDependencies() throws Exception {
		Bundle bnd = getDependencyBundle();
		bnd.start();

		logger.info("Waiting for the test bundle to start up...");
		waitOnContextCreation(DEP_SYN_NAME);
		logger.info("Test bundle context created - starting test...");
		
		assertTrue("exporter not alive on startup", isDependsOnExporterAlive());
		optional.unregister();
		assertTrue("exporter affected by the optional dependency", isDependsOnExporterAlive());
		mandatory.unregister();
		assertFalse("exporter not affected by the optional dependency", isDependsOnExporterAlive());
		registerOptional();
		assertFalse("exporter affected by the optional dependency", isDependsOnExporterAlive());
		registerMandatory();
		assertTrue("exporter not affected by the optional dependency", isDependsOnExporterAlive());
		optional.unregister();
		assertTrue("exporter affected by the optional dependency", isInjectedExporterAlive());
	}

	private boolean isExporterAlive(String name) {
		String filter = OsgiFilterUtils.unifyFilter(new Class[] { Serializable.class, Cloneable.class },
				"(org.eclipse.gemini.blueprint.bean.name=" + name + ")");
		ServiceReference reference = OsgiServiceReferenceUtils.getServiceReference(bundleContext, filter);
		if (reference != null) {
			Object service = bundleContext.getService(reference);
			return service != null;
		}
		return false;
	}

	private boolean isInjectedExporterAlive() {
		return isExporterAlive("injected-export");
	}

	private boolean isDependsOnExporterAlive() {
		return isExporterAlive("depends-on-export");
	}

	private void registerOptional() {
		optional = bundleContext.registerService(SortedSet.class.getName(), new TreeSet(), null);
	}

	private void registerMandatory() {
		mandatory = bundleContext.registerService(SortedMap.class.getName(), new TreeMap(), null);
	}

	Bundle installTestBundle(BundleContext context) throws Exception {
		Resource res = getLocator().locateArtifact("org.eclipse.gemini.blueprint.iandt", "export-import-dependency-bundle",
				getSpringDMVersion());
		return context.installBundle("test-bundle", res.getInputStream());
	}

	protected Bundle getDependencyBundle() {
		return OsgiBundleUtils.findBundleBySymbolicName(bundleContext, DEP_SYN_NAME);
	}
}