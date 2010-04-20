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

package org.eclipse.gemini.blueprint.test.parsing.packageC;

import java.util.jar.Manifest;

import org.eclipse.gemini.blueprint.test.parsing.packageA.BaseClassFromAnotherPackageTest;
import org.eclipse.gemini.blueprint.test.parsing.packageB.BaseClassFromAnotherPackageAndBundleTest;

/**
 * Abstract since we don't want to execute the test per se.
 * 
 * @author Costin Leau
 * 
 */
// callback interface (no exception or custom method signature pulled in)
public abstract class TestInDifferentPackageThenItsParentsTest extends BaseClassFromAnotherPackageAndBundleTest {

	public void testCheckBaseClassesHierarchy() throws Exception {
		Manifest mf = getManifest();
		System.out.println(mf.getMainAttributes().entrySet());
	}

	public String[] getBundleContentPattern() {
		String pkg = TestInDifferentPackageThenItsParentsTest.class.getPackage().getName().replace('.', '/').concat("/");
		String[] patterns = new String[] { pkg,
			BaseClassFromAnotherPackageTest.class.getName().replace('.', '/').concat(".class") };
		return patterns;
	}
}
