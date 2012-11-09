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

package org.eclipse.gemini.blueprint.context.support;

import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;

/**
 * 
 * @author Costin Leau
 */
public class ConfigLocationsTest extends TestCase {

	private OsgiBundleXmlApplicationContext context;

	@Override
	protected void setUp() throws Exception {
		context = new OsgiBundleXmlApplicationContext();
	}

	@Override
	protected void tearDown() throws Exception {
		context = null;
	}

	public void testExpandConfigFolders() throws Exception {
		String[] cfgs = new String[] { "cnf/", "/cnf/" };
		context.setConfigLocations(cfgs);
		String[] returned =
				(String[]) invokeMethod("expandLocations", new Class[] { String[].class }, new Object[] { cfgs });
		assertTrue(Arrays.equals(new String[] { "cnf/*.xml", "/cnf/*.xml" }, returned));
	}

	private Object invokeMethod(String name, Class[] types, Object[] args) {
		try {
			Method mt = context.getClass().getDeclaredMethod(name, types);
			mt.setAccessible(true);
			return mt.invoke(context, args);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
