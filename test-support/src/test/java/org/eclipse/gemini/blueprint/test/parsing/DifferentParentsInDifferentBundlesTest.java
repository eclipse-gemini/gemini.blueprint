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

package org.eclipse.gemini.blueprint.test.parsing;

import java.lang.reflect.Field;
import java.util.jar.Manifest;

import javax.print.event.PrintEvent;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.test.AbstractConfigurableBundleCreatorTests;
import org.eclipse.gemini.blueprint.test.parsing.packageA.BaseClassFromAnotherPackageTest;
import org.eclipse.gemini.blueprint.test.parsing.packageB.BaseClassFromAnotherPackageAndBundleTest;
import org.eclipse.gemini.blueprint.test.parsing.packageC.TestInDifferentPackageThenItsParentsTest;
import org.eclipse.gemini.blueprint.test.parsing.packageZ.FinalTestClassTest;
import org.osgi.framework.Constants;
import org.springframework.util.ObjectUtils;

/**
 * Integration that checks if the class hierarchy is properly parsed. Note this test doesn't run in OSGi, it just
 * invokes the bytecode parsing.
 * 
 * @author Costin Leau
 * 
 */
public class DifferentParentsInDifferentBundlesTest extends TestCase {

	public void testCheckBaseClassesHierarchy() throws Exception {
		// create class
		// make sure the packaging puts some of the tests parent in a different class
		TestInDifferentPackageThenItsParentsTest test = new TestInDifferentPackageThenItsParentsTest() {
		};

		String importPackage = getImportPackage(test);

		// check parent package
		// parent in a different bundle
		assertTrue("missing parent package not considered", contains(importPackage,
				BaseClassFromAnotherPackageAndBundleTest.class.getPackage().getName()));
		// parent in a different package but the same bundle (i.e. no import)
		assertFalse("contained parent not considered", contains(importPackage, BaseClassFromAnotherPackageTest.class
				.getPackage().getName()));
		// check present parent dependencies
		assertTrue("contained parent dependencies not considered", contains(importPackage, "javax.imageio"));
	}

	public void testSuperClassInterfacesConsidered() throws Exception {
		FinalTestClassTest test = new FinalTestClassTest() {
		};

		String importPackage = getImportPackage(test);
		// check test interface package
		assertTrue("interface present on the test class ignored", contains(importPackage, "javax.swing.text"));
		// check super class interface package
		assertTrue("interface present on the test class ignored", contains(importPackage,
				"javax.security.auth.callback"));
		// check super class interface package
		assertTrue("interface present on superclass ignored", contains(importPackage, "javax.print"));
	}

	public void testAnonymousInnerClasses() throws Exception {
		FinalTestClassTest test = new FinalTestClassTest() {

			@Override
			public int getOffset() {
				PrintEvent pe = new PrintEvent(new Object()) {
				};
				return pe.hashCode();
			}
		};

		String importPackage = getImportPackage(test);
	}

	private Manifest getParsedManifestFor(CaseWithVisibleMethodsBaseTest testCase) throws Exception {

		System.out.println(ObjectUtils.nullSafeToString(testCase.getBundleContentPattern()));
		Field jarSettings = AbstractConfigurableBundleCreatorTests.class.getDeclaredField("jarSettings");
		// initialize settings
		jarSettings.setAccessible(true);
		jarSettings.set(null, testCase.getSettings());

		Manifest mf = testCase.getManifest();

		return mf;
	}

	private String getImportPackage(CaseWithVisibleMethodsBaseTest test) throws Exception {
		Manifest mf = getParsedManifestFor(test);
		String importPackage = mf.getMainAttributes().getValue(Constants.IMPORT_PACKAGE);
		// System.out.println("import package value is " + importPackage);
		return importPackage;
	}

	private boolean contains(String text, String item) {
		return text.indexOf(item) > -1;
	}
}
