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

package org.eclipse.gemini.blueprint.iandt.testingFramework;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.Constants;
import org.eclipse.gemini.blueprint.test.AbstractConfigurableBundleCreatorTests;

/**
 * This test might log exceptions since the OSGi platform may try to register an
 * URLStreamFactory every time they start (during {@link #setName(String)}).
 * 
 * Basically, this just reads the underlying platform and checks its vendor to
 * make sure the mapping is correct.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiPlatformTest extends BaseIntegrationTest {

	private String platform;


	protected void onSetUp() {
		platform = getPlatformName();
	}

	public void testOsgiPlatform() throws Exception {
		String vendor = bundleContext.getProperty(Constants.FRAMEWORK_VENDOR);

		if ("Eclipse".equals(vendor))
			assertTrue(platform.indexOf("Equinox") >= 0);
		if ("Apache Software Foundation".equals(vendor))
			assertTrue(platform.indexOf("Felix") >= 0);
		if ("Knopflerfish".equals(vendor))
			assertTrue(platform.indexOf("Knopflerfish") >= 0);
	}
}
