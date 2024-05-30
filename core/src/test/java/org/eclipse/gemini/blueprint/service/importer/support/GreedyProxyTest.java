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

package org.eclipse.gemini.blueprint.service.importer.support;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.osgi.framework.BundleContext;
import org.springframework.util.CollectionUtils;

import javax.print.attribute.SupportedValuesAttribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Unit test regarding the importing of services inside a collection.
 * 
 * @author Costin Leau
 */
public class GreedyProxyTest {

	private StaticServiceProxyCreator proxyCreator;

	private String[] classesAsStrings = new String[] { Serializable.class.getName(), Comparable.class.getName() };

	@Before
	public void setup() throws Exception {
		Class<?>[] classes = new Class<?>[] { Serializable.class, Comparable.class };

		proxyCreator = createProxyCreator(classes);
	}

	private StaticServiceProxyCreator createProxyCreator(Class<?>[] classes) {
		ClassLoader cl = getClass().getClassLoader();
		BundleContext ctx = new MockBundleContext();
		return new StaticServiceProxyCreator(classes, cl, cl, ctx, ImportContextClassLoaderEnum.UNMANAGED, true, false);
	}

	@After
	public void tearDown() throws Exception {
		proxyCreator = null;
	}

	private String[] addExtraIntfs(String[] extraIntfs) {
		List list = new ArrayList();
		CollectionUtils.mergeArrayIntoCollection(extraIntfs, list);
		CollectionUtils.mergeArrayIntoCollection(classesAsStrings, list);
		return (String[]) list.toArray(new String[list.size()]);
	}

	private boolean containsClass(Class<?>[] classes, Class<?> clazz) {
		for (int i = 0; i < classes.length; i++) {
			if (clazz.equals(classes[i]))
				return true;
		}
		return false;
	}

	@Test
	public void testMoreInterfacesAvailable() throws Exception {
		String[] extraClasses = new String[] { Cloneable.class.getName(), Runnable.class.getName() };

		MockServiceReference ref = new MockServiceReference(addExtraIntfs(extraClasses));

		Class<?>[] clazzes = proxyCreator.discoverProxyClasses(ref);
		assertTrue(containsClass(clazzes, Cloneable.class));
		assertTrue(containsClass(clazzes, Runnable.class));
		assertTrue(containsClass(clazzes, Serializable.class));
	}

	@Test
	public void testNonVisibleOrInvalidInterfacesFound() throws Exception {
		String[] extraClasses = new String[] { "a", "nonExistingClass" };

		MockServiceReference ref = new MockServiceReference(addExtraIntfs(extraClasses));

		Class<?>[] clazzes = proxyCreator.discoverProxyClasses(ref);
		assertEquals(2, clazzes.length);
		assertTrue(containsClass(clazzes, Serializable.class));
		assertTrue(containsClass(clazzes, Comparable.class));
	}

	@Test
	public void testParentInterfaces() throws Exception {
		String[] extraClasses = new String[] { SupportedValuesAttribute.class.getName()};

		MockServiceReference ref = new MockServiceReference(addExtraIntfs(extraClasses));
		Class<?>[] clazzes = proxyCreator.discoverProxyClasses(ref);
		assertEquals(2, clazzes.length);
		assertTrue(containsClass(clazzes, Comparable.class));
		assertTrue(containsClass(clazzes, SupportedValuesAttribute.class));
	}

	@Test
	public void testExcludeFinalClass() throws Exception {
		String[] extraClasses = new String[] { Object.class.getName(), Byte.class.getName() };
		MockServiceReference ref = new MockServiceReference(addExtraIntfs(extraClasses));
		Class<?>[] clazzes = proxyCreator.discoverProxyClasses(ref);
		assertEquals(2, clazzes.length);
		assertFalse(containsClass(clazzes, Byte.class));
		assertTrue(containsClass(clazzes, Comparable.class));
		assertTrue(containsClass(clazzes, Serializable.class));
	}

	@Test
	public void testInterfacesOnlyAllowed() throws Exception {
		String[] extraClasses = new String[] { Object.class.getName() };

		MockServiceReference ref = new MockServiceReference(addExtraIntfs(extraClasses));
		Class<?>[] clazzes = proxyCreator.discoverProxyClasses(ref);
		assertEquals(2, clazzes.length);
		assertFalse(containsClass(clazzes, Object.class));
	}

	@Test
	public void testAllowConcreteClasses() throws Exception {
		Class<?>[] classes = new Class<?>[] { Serializable.class, Comparable.class, Date.class };
		proxyCreator = createProxyCreator(classes);

		String[] extraClasses = new String[] { LinkedHashMap.class.getName(), Date.class.getName() };

		MockServiceReference ref = new MockServiceReference(addExtraIntfs(extraClasses));
		Class<?>[] clazzes = proxyCreator.discoverProxyClasses(ref);
		assertEquals(2, clazzes.length);
		assertTrue(containsClass(clazzes, LinkedHashMap.class));
		assertTrue(containsClass(clazzes, Date.class));
	}

	@Test
	public void testRemoveParentsWithClassesAndInterfaces() throws Exception {
		Class<?>[] classes = new Class<?>[] { Serializable.class, Comparable.class, Date.class };
		proxyCreator = createProxyCreator(classes);
		String[] extraClasses = new String[] { Time.class.getName(), Cloneable.class.getName() };
		MockServiceReference ref = new MockServiceReference(addExtraIntfs(extraClasses));

		Class<?>[] clazzes = proxyCreator.discoverProxyClasses(ref);
		assertEquals(1, clazzes.length);
		assertTrue(containsClass(clazzes, Time.class));
	}
}