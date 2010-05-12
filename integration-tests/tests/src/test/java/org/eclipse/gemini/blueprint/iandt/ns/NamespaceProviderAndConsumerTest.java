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

package org.eclipse.gemini.blueprint.iandt.ns;

import java.awt.Shape;
import java.net.URL;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;

/**
 * Integration test that provides a namespace that is also used internally.
 * 
 * @author Costin Leau
 */
public class NamespaceProviderAndConsumerTest extends BaseIntegrationTest {

	private Shape nsBean;

	private static final String BND_SYM_NAME = "org.eclipse.gemini.blueprint.iandt.ns.own.provider";


	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt, ns.own.consumer," + getSpringDMVersion() };
	}

	protected String[] getConfigLocations() {
		return new String[] { "org/eclipse/gemini/blueprint/iandt/ns/context.xml" };
	}

	public void testApplicationContextWasProperlyStarted() throws Exception {
		assertNotNull(applicationContext);
		assertNotNull(applicationContext.getBean("nsDate"));
		assertNotNull(applicationContext.getBean("nsBean"));
	}

	public void testTestAutowiring() throws Exception {
		assertNotNull(nsBean);
	}

	public void tstNamespaceFilesOnTheClassPath() throws Exception {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, BND_SYM_NAME);
		assertNotNull("cannot find handler bundle", bundle);
		URL handlers = bundle.getResource("META-INF/spring.handlers");
		URL schemas = bundle.getResource("META-INF/spring.schemas");

		assertNotNull("cannot find a handler inside the custom bundle", handlers);
		assertNotNull("cannot find a schema inside the custom bundle", schemas);
	}

	public void testNSBundlePublishedOkay() throws Exception {
		ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(bundleContext,
			ApplicationContext.class.getName(), "(" + Constants.BUNDLE_SYMBOLICNAME + "=" + BND_SYM_NAME + ")");
		assertNotNull(ref);
		ApplicationContext ctx = (ApplicationContext) bundleContext.getService(ref);
		assertNotNull(ctx);
		assertNotNull(ctx.getBean("nsBean"));
		assertNotNull(ctx.getBean("nsDate"));

	}

	/**
	 * @param nsBean The nsBean to set.
	 */
	public void setNsBean(Shape nsBean) {
		this.nsBean = nsBean;
	}
}