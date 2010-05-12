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

import org.osgi.framework.Constants;
import org.springframework.core.io.Resource;

/**
 * @author Costin Leau
 * 
 */
public class ClassSpaceWildcardTest extends BaseIoTest {

	//
	// Wild-card tests
	//

	public void testBundleClassPath() throws Exception {
		System.out.println("*** Bundle-ClassPath is " + bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH));
		Resource res[] = patternLoader.getResources("classpath*:/org/eclipse/gemini/blueprint/iandt/io/ClassSpaceWildcardTest.class");
		assertEquals("invalid bundle-classpath entries should be skipped", 1, res.length);
		printPathWithinContext(res);
	}

	// finds all files at root level
	public void testWildcardAtRootFileLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/*");
		// only the bundle and its fragments should be considered (since no other META-INF/ is available on the classpath)
		assertEquals("not enough packages found", 3, res.length);
		printPathWithinContext(res);
	}

	// similar as the root test but inside META-INF
	public void testWildcardAtFolderLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/META-INF/*");
		assertEquals("not enough packages found", 1, res.length);
		printPathWithinContext(res);
	}

	public void testSingleClassWithWildcardAtFileLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/org/eclipse/gemini/blueprint/iandt/io/Class*Test.class");
		assertTrue("not enough packages found", res.length >= 1);
		printPathWithinContext(res);
	}

	public void testClassPathRootWildcard() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/**/blueprint/iandt/io/Class*Test.class");
		assertTrue("not enough packages found", res.length >= 1);
	}

	public void testAllClassPathWildcardAtFolderLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:/META-INF/*");
		// only the bundle and its fragments should be considered (since no other META-INF/ is available on the classpath)
		assertEquals("not enough packages found", 3, res.length);
		printPathWithinContext(res);
	}

	public void testAllClassPathWOWildcardAtFolderLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:META-INF/");
		// only the bundle and its fragments should be considered (since no other META-INF/ is available on the classpath)
		assertEquals("not enough packages found", 3, res.length);
		printPathWithinContext(res);
	}

	public void testAllClassPathWithWildcardAtFileLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/org/eclipse/gemini/blueprint/iandt/io/Class*WildcardTest.class");
		assertEquals("not enough packages found", 1, res.length);
		printPathWithinContext(res);
	}

	public void testAllClassPathWithWithWildcardAtFileLevel() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:/org/eclipse/gemini/blueprint/iandt/io/Class*WildcardTest.class");
		assertEquals("not enough packages found", 1, res.length);
		printPathWithinContext(res);
	}

	public void testAllClassPathRootWildcard() throws Exception {
		Resource res[] = patternLoader.getResources("classpath:/**/gemini/blueprint/**/ClassSpaceWildcardTest.class");
		assertEquals("not enough packages found", 1, res.length);
		printPathWithinContext(res);
	}

	public void testAllClassPathRootWithWildcard() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:/**/gemini/blueprint/**/ClassSpaceWildcardTest.class");
		assertEquals("not enough packages found", 1, res.length);
		printPathWithinContext(res);
	}

	//
	// Stress tests (as they pull a lot of content)
	//

	public void testMatchingALotOfClasses() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:/**/gemini/blueprint/iandt/io/*.class");
		// at least 2 classes should be in there
		assertTrue("not enough packages found", res.length > 1);
		printPathWithinContext(res);
	}

	// EQ = 48
	// KF = 48
	// FX = 38
	public void testMatchingALotOfFolders() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:/**/gemini/blueprint/**");
		System.out.println("resources count " + res.length);
		assertTrue("not enough packages found", res.length > 10);
		printPathWithinContext(res);
	}

	// ask for everything springframework :)
	// EQ = 147
	// KF = 147
	// FX = 135 (no fragment support)
	public void testMatchingABulkOfResources() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:**/springframework/**");
		Resource resWitSlash[] = patternLoader.getResources("classpath*:/**/springframework/**");
		System.out.println("resources w/o slash count " + res.length);
		System.out.println("resources w/ slash count " + resWitSlash.length);
		assertEquals("slash should not make a difference", res.length, resWitSlash.length);
		assertTrue("not enough packages found", res.length > 50);
		printPathWithinContext(res);
	}

	// ask for everything org :)
	// EQ = 271 (since it considers the system bundle also)
	// KF = 147 (doesn't consider system bundle)
	// FX = 135
	public void testMatchingAHugeSetOfResources() throws Exception {
		Resource res[] = patternLoader.getResources("classpath*:org/**");
		Resource resWitSlash[] = patternLoader.getResources("classpath*:/org/**");
		System.out.println("resources w/o slash count " + res.length);
		System.out.println("resources w/ slash count " + resWitSlash.length);
		assertEquals("slash should not make a difference", res.length, resWitSlash.length);
		assertTrue("not enough packages found", res.length > 100);
		printPathWithinContext(res);
	}

	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		// felix doesn't support fragments yet
		return (isFelix() && (testMethodName.equals("testAllClassPathWildcardAtFolderLevel")
				|| testMethodName.equals("testWildcardAtRootFileLevel") || testMethodName.equals("testAllClassPathWOWildcardAtFolderLevel")));
	}
}