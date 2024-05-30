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

package org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.support;

import java.util.Dictionary;
import java.util.Properties;

import org.junit.Test;

import org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.support.BlueprintConfigUtils;
import org.osgi.framework.Constants;

/**
 * @author Costin Leau
 */
public class BlueprintConfigUtilsTest {
	@Test
	public void testNoWaitForDependencies() throws Exception {
		Dictionary props = new Properties();
		props.put(Constants.BUNDLE_SYMBOLICNAME, "foo.bar; " + BlueprintConfigUtils.BLUEPRINT_GRACE_PERIOD + ":=false");
		assertFalse(BlueprintConfigUtils.getWaitForDependencies(props));

	}

	@Test
	public void testWaitForDependencies() throws Exception {
		Dictionary props = new Properties();
		props.put(Constants.BUNDLE_SYMBOLICNAME, "foo.bar; " + BlueprintConfigUtils.BLUEPRINT_GRACE_PERIOD + ":=true");
		assertTrue(BlueprintConfigUtils.getWaitForDependencies(props));
	}

	@Test
	public void testNoWaitDefinedForDependencies() throws Exception {
		Dictionary props = new Properties();
		props.put(Constants.BUNDLE_SYMBOLICNAME, "foo.bar");
		assertTrue(BlueprintConfigUtils.getWaitForDependencies(props));
	}
}
