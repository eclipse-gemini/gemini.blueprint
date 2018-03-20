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

package org.eclipse.gemini.blueprint.iandt.configopt;

import java.awt.Shape;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;

/**
 * Simple test for bundles that provide a configuration file with dots.
 * 
 * @author Costin Leau
 * 
 */
public class ConfigFileWithDotsTest extends BaseIntegrationTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt, config-with-dots.bundle," + getGeminiBlueprintVersion() };
	}

	public void testShapeServicePublished() throws Exception {
		assertNotNull(bundleContext.getServiceReference(Shape.class.getName()));
	}
}
