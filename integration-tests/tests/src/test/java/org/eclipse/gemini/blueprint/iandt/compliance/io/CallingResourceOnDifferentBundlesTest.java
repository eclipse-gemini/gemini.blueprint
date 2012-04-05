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

package org.eclipse.gemini.blueprint.iandt.compliance.io;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.eclipse.gemini.blueprint.test.platform.Platforms;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.springframework.util.ObjectUtils;

/**
 * 
 * http://sourceforge.net/tracker/index.php?func=detail&aid=1581187&group_id=82798&atid=567241 ClassCastException from
 * Bundle.getResource when called on Bundle passed to the caller
 * 
 * 
 * @author Costin Leau
 * 
 */
public class CallingResourceOnDifferentBundlesTest extends BaseIntegrationTest {

	private static final String LOCATION = "META-INF/";

	public void testCallGetResourceOnADifferentBundle() throws Exception {
		// find bundles
		Bundle[] bundles = bundleContext.getBundles();
		for (int i = 1; i < bundles.length; i++) {
			Bundle bundle = bundles[i];
			logger.debug("calling #getResource on bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle));
			URL url = bundle.getResource(LOCATION);
			if (!OsgiBundleUtils.isFragment(bundle))
				assertNotNull("bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle) + " contains no META-INF/",
						url);
		}
	}

	public void testCallGetResourcesOnADifferentBundle() throws Exception {
		// find bundles
		Bundle[] bundles = bundleContext.getBundles();
		for (int i = 1; i < bundles.length; i++) {
			Bundle bundle = bundles[i];
			logger.debug("calling #getResources on bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle));
			Enumeration enm = bundle.getResources(LOCATION);
			if (!OsgiBundleUtils.isFragment(bundle))
				assertNotNull("bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle) + " contains no META-INF/",
						enm);
		}
	}

	public void testCallGetResourceOnADifferentBundleRetrievedThroughBundleEvent() throws Exception {
		String EXTRA_BUNDLE = "spring-core";

		Bundle[] bundles = bundleContext.getBundles();
		Bundle bundle = null;
		// find cglib library as we don't use it
		for (int i = 1; bundle == null && i < bundles.length; i++) {
			String location = bundles[i].getLocation();
			if (location != null && location.indexOf(EXTRA_BUNDLE) > -1)
				bundle = bundles[i];
		}

		assertNotNull("no bundle found", bundle);
		final Bundle sampleBundle = bundle;

		final boolean[] listenerCalled = new boolean[] { false };

		// register listener
		bundleContext.addBundleListener(new SynchronousBundleListener() {

			public void bundleChanged(BundleEvent event) {
				// call getResource
				event.getBundle().getResource(LOCATION);
				// call getResources
				try {
					event.getBundle().getResources(LOCATION);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				listenerCalled[0] = true;
			}
		});

		// update
		sampleBundle.stop();

		assertTrue("bundle listener hasn't been called", listenerCalled[0]);
	}

	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return ("testCallGetResourceOnADifferentBundle".equals(testMethodName) || "testCallGetResourcesOnADifferentBundle"
				.equals(testMethodName))
				&& (isFelix() || isKF());
	}

	private boolean isFelix() {
		String platformName = getPlatformName();
		System.out.println("Platform name is " + platformName);
		return (platformName.indexOf("Felix") > -1);
	}

	private boolean isKF() {
		return (getPlatformName().indexOf("Knopflerfish") > -1);
	}

	protected List getTestPermissions() {
		List list = super.getTestPermissions();
		list.add(new AdminPermission("*", AdminPermission.METADATA));
		list.add(new AdminPermission("*", AdminPermission.LISTENER));
		list.add(new AdminPermission("*", AdminPermission.EXECUTE));
		list.add(new AdminPermission("*", AdminPermission.RESOURCE));
		return list;
	}
}