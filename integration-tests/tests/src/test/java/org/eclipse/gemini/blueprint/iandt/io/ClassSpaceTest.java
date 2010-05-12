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

package org.eclipse.gemini.blueprint.iandt.io;

import org.springframework.core.io.Resource;

/**
 * Classpath tests.
 * 
 * @author Costin Leau
 * 
 */
public class ClassSpaceTest extends BaseIoTest {

	public void testFolder() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/org/eclipse/gemini/blueprint");
		// EQ returns the fragments paths also
		assertTrue(res.length >= 1);
	}

	// META-INF seems to be a special case, since the manifest is added
	// automatically by the jar stream
	// but it's the JarCreator which creates the META-INF folder
	public void testMetaInfFolder() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/META-INF");
		// Equinox returns more entries (bootpath delegation)
		assertTrue(res.length >= 1);
	}

	public void testClass() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/org/eclipse/gemini/blueprint/iandt/io/ClassSpaceTest.class");
		assertEquals(1, res.length);
	}

}
