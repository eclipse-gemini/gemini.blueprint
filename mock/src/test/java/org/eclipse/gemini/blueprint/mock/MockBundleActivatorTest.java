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

package org.eclipse.gemini.blueprint.mock;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.mock.MockBundleActivator;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.osgi.framework.BundleActivator;

/**
 * @author Costin Leau
 * 
 */
public class MockBundleActivatorTest extends TestCase {

	BundleActivator mock;

	protected void setUp() throws Exception {
		mock = new MockBundleActivator();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleActivator#start(org.osgi.framework.BundleContext)}.
	 */
	public void testStart() throws Exception {
		mock.start(new MockBundleContext());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleActivator#stop(org.osgi.framework.BundleContext)}.
	 */
	public void testStop() throws Exception {
		mock.stop(new MockBundleContext());
	}

}
