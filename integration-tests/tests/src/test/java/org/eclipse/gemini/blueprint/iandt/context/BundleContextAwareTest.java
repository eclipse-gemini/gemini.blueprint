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

import java.io.Serializable;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.BundleContext;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;

/**
 * Test injection of BundleContextAware.
 * 
 * @author Costin Leau
 * 
 */
public class BundleContextAwareTest extends BaseIntegrationTest {

	public static class BundleContextAwareHolder implements BundleContextAware {

		private BundleContext bundleContext;

		public BundleContext getBundleContext() {
			return bundleContext;
		}

		public void setBundleContext(BundleContext bundleContext) {
			this.bundleContext = bundleContext;
		}

		private static class AnotherInnerClass implements Serializable {

		}
	}

	protected String getManifestLocation() {
		return null;
	}

	protected String[] getConfigLocations() {
		return new String[] { "/org/eclipse/gemini/blueprint/iandt/context/bundleContextAwareTest.xml" };
	}

	public void testBundleContextAware() throws Exception {
		BundleContextAwareHolder holder = (BundleContextAwareHolder) applicationContext.getBean("bean");
		assertNotNull(holder.getBundleContext());
		assertSame(bundleContext, holder.getBundleContext());
		assertSame(applicationContext.getBean(ConfigurableOsgiBundleApplicationContext.BUNDLE_CONTEXT_BEAN_NAME),
			holder.getBundleContext());
	}
}
