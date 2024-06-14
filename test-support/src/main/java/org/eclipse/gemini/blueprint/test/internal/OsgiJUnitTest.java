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

package org.eclipse.gemini.blueprint.test.internal;

import org.osgi.framework.BundleContext;

/**
 * JUnit contract for OSGi environments. It wraps some of TestCase methods as
 * well as adds some to allow flexible access to the test instance by the
 * TestRunnerService implementation.
 * 
 * @author Costin Leau
 * 
 */
public interface OsgiJUnitTest {

	/**
	 * Replacement for the 'traditional' setUp. Called by TestRunnerService.
	 * 
	 * @see junit.framework.TestCase#setUp
	 * @throws Exception
	 */
	void osgiSetUp() throws Exception;

	/**
	 * Replacement for the 'traditional' tearDown. Called by TestRunnerService.
	 * 
	 * @see junit.framework.TestCase#tearDown
	 * @throws Exception
	 */
	void osgiTearDown() throws Exception;

	/**
	 * Provides the OSGi bundle context to the test
	 * 
	 * @param bundleContext
	 */
	void injectBundleContext(BundleContext bundleContext);

}
