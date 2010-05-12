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

package org.eclipse.gemini.blueprint.iandt.context;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;

/**
 * Integration test on the functionality offered by OSGi app context.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiAppContextTest extends BaseIntegrationTest {

	private BundleContext bundleContext;


	public void testBundleContextAvailableAsBean() {
		ApplicationContext ctx = applicationContext;
		assertNotNull(ctx);
		assertTrue("bundleContext not available as a bean",
			applicationContext.containsBean(ConfigurableOsgiBundleApplicationContext.BUNDLE_CONTEXT_BEAN_NAME));
	}

	public void testBundleContextInjected() {
		assertNotNull("bundleContext hasn't been injected into the test", bundleContext);
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public void testBundleContextIsTheSame() {
		assertSame(bundleContext,
			applicationContext.getBean(ConfigurableOsgiBundleApplicationContext.BUNDLE_CONTEXT_BEAN_NAME));
	}
}
