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

package org.eclipse.gemini.blueprint.test.platform;

import org.junit.After;
import org.junit.Before;

/**
 * @author Costin Leau
 * 
 */
public class KnopflerfishPlatformTest extends CommonPlatformTest {

	@Before
	@Override
	public void setUp() throws Exception {
		System.setProperty("org.knopflerfish.osgi.registerserviceurlhandler", "false");
		super.setUp();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		System.getProperties().remove("org.knopflerfish.osgi.registerserviceurlhandler");
		super.tearDown();
	}

	@Override
	AbstractOsgiPlatform createPlatform() {
		return new KnopflerfishPlatform();
	}
}
