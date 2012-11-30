/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.; 2012 Elastic Path, Inc.
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
 *   Elastic Path, Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.blueprint.container;

import java.awt.Point;
import java.awt.Shape;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gemini.blueprint.blueprint.MyCustomDictionary;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.service.blueprint.container.ReifiedType;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Costin Leau
 */
public class TypeFactoryTest {

	private static class TestSet<A> {

		public void rawList(List arg) {
		}

		public void primitive(int arg) {
		}

		public void integer(Integer arg) {
		}

		public void typedList(LinkedList<Point> arg) {
		}

		public void array(Integer[] arg) {
		}

		public void extendsList(LinkedList<? extends Shape> arg) {
		}

		public void superList(LinkedList<? super Shape> arg) {
		}

		public void typedMap(TreeMap<Integer, Double> arg) {
		}

		public void pointMap(TreeMap<String, Point> arg) {
		}

		public void typedReference(AtomicReference<Boolean> arg) {
		}

		public void objectTypedReference(AtomicReference<Object> arg) {
		}

		public void wildcardReference(AtomicReference<?> arg) {
		}

		public void superTypedReference(AtomicReference<? super Properties> arg) {
		}

		public void extendsTypedReference(AtomicReference<? extends Properties> arg) {
		}

		public void typeVariable(AtomicReference<A> arg) {
		}

		public void customDictionary(MyCustomDictionary customDict) {
		}
	}

	private static class RecursiveGenericType<T extends Comparable<T>> {
	}

	private static class SingleAndRecursiveGenericType<S extends String, T extends Comparable<S>> {
	}

	private static class MultipleRecursiveGenericType<T extends Comparable<T>, U extends T> {
	}

	private static class MutuallyRecursiveGenericType<T extends Comparable<U>, U extends Comparable<T>> {
		// Must T always equal U?
	}

	private static class ComplexRecursiveGenericType<T extends Comparable<? super T>> {
	}

	private static class MultiBoundedRecursiveGenericType<T extends Comparable<T> & Cloneable> {
		// Cloneable could be replaced by any interface
	}
	
	private static class MutuallyRecursiveThroughSecondBoundGenericType<T extends Comparable<S> & X<U>, U extends Comparable<S> & X<T>, S> {
    }
	
	private static interface X<T>{}

	private static interface Ice {}
	private static interface Juice {}
	private static class A<T extends Ice & Juice> {}
	private static class B<T extends Juice & Ice> {}

	@Test
	public void testJdk4Classes() throws Exception {
		ReifiedType tp = getReifiedTypeFor("rawList");
		assertEquals(1, tp.size());
		assertEquals(List.class, tp.getRawClass());
	}

	@Test
	public void testPrimitive() throws Exception {
		ReifiedType tp = getReifiedTypeFor("primitive");
		assertEquals(0, tp.size());
		assertEquals(Integer.class, tp.getRawClass());
	}

	@Test
	public void testArray() throws Exception {
		ReifiedType tp = getReifiedTypeFor("array");
		assertEquals(1, tp.size());
		assertEquals(Integer[].class, tp.getRawClass());
		assertEquals(Integer.class, tp.getActualTypeArgument(0).getRawClass());
	}

	@Test
	public void testInteger() throws Exception {
		ReifiedType tp = getReifiedTypeFor("integer");
		assertEquals(0, tp.size());
		assertEquals(Integer.class, tp.getRawClass());
	}

	@Test
	public void testTypedObjectList() throws Exception {
		ReifiedType tp = getReifiedTypeFor("typedList");
		assertEquals(1, tp.size());
		assertEquals(LinkedList.class, tp.getRawClass());
		assertEquals(Point.class, tp.getActualTypeArgument(0).getRawClass());
	}

	@Test
	public void testExtendsList() throws Exception {
		ReifiedType tp = getReifiedTypeFor("extendsList");
		assertEquals(1, tp.size());
		assertEquals(LinkedList.class, tp.getRawClass());
		assertEquals(Shape.class, tp.getActualTypeArgument(0).getRawClass());
	}

	@Test
	public void testSuperList() throws Exception {
		ReifiedType tp = getReifiedTypeFor("superList");
		assertEquals(1, tp.size());
		assertEquals(LinkedList.class, tp.getRawClass());
		assertEquals(Shape.class, tp.getActualTypeArgument(0).getRawClass());
	}

	@Test
	public void testTypedMap() throws Exception {
		ReifiedType tp = getReifiedTypeFor("typedMap");
		assertEquals(2, tp.size());
		assertEquals(TreeMap.class, tp.getRawClass());
		assertEquals(Integer.class, tp.getActualTypeArgument(0).getRawClass());
		assertEquals(Double.class, tp.getActualTypeArgument(1).getRawClass());
	}

	@Test
	public void testPointMap() throws Exception {
		ReifiedType tp = getReifiedTypeFor("pointMap");
		assertEquals(2, tp.size());
		assertEquals(TreeMap.class, tp.getRawClass());
		assertEquals(String.class, tp.getActualTypeArgument(0).getRawClass());
		assertEquals(Point.class, tp.getActualTypeArgument(1).getRawClass());
	}

	@Test
	public void testObjectTypedReference() throws Exception {
		ReifiedType tp = getReifiedTypeFor("objectTypedReference");
		assertEquals(AtomicReference.class, tp.getRawClass());
		assertEquals(1, tp.size());
		assertEquals(Object.class, tp.getActualTypeArgument(0).getRawClass());
	}

	@Test
	public void testWildcardReference() throws Exception {
		ReifiedType tp = getReifiedTypeFor("wildcardReference");
		assertEquals(AtomicReference.class, tp.getRawClass());
		assertEquals(1, tp.size());
		assertEquals(Object.class, tp.getActualTypeArgument(0).getRawClass());
	}

	@Test
	public void testTypeVariable() throws Exception {
		ReifiedType tp = getReifiedTypeFor("typeVariable");
		assertEquals(1, tp.size());
		assertEquals(AtomicReference.class, tp.getRawClass());
		assertEquals(Object.class, tp.getActualTypeArgument(0).getRawClass());
	}

	@Test
	public void testCustomDictionary() throws Exception {
		ReifiedType tp = getReifiedTypeFor("customDictionary");
		assertEquals(2, tp.size());
		assertEquals(MyCustomDictionary.class, tp.getRawClass());
		assertEquals(Object.class, tp.getActualTypeArgument(0).getRawClass());
		assertEquals(Object.class, tp.getActualTypeArgument(1).getRawClass());
	}

	@Test
	public void testUnknownType() throws Exception {
		ReifiedType type = TypeFactory.getType(TypeDescriptor.forObject(null));
		assertEquals(Object.class, type.getRawClass());
	}

	@Test
	public void testRecursiveGenericType() throws Exception {
		assertNotNull(TypeFactory.getType(TypeDescriptor.valueOf(RecursiveGenericType.class)));
	}

	@Test
	public void testSingleAndRecursiveGenericType() throws Exception {
		assertNotNull(TypeFactory.getType(TypeDescriptor.valueOf(SingleAndRecursiveGenericType.class)));
	}

	@Test
	public void testMultipleRecursiveGenericType() throws Exception {
		assertNotNull(TypeFactory.getType(TypeDescriptor.valueOf(MultipleRecursiveGenericType.class)));
	}

	@Test
	public void testMutuallyRecursiveGenericType() throws Exception {
		assertNotNull(TypeFactory.getType(TypeDescriptor.valueOf(MutuallyRecursiveGenericType.class)));
	}

	@Test
	public void testComplexRecursiveGenericType() throws Exception {
		assertNotNull(TypeFactory.getType(TypeDescriptor.valueOf(ComplexRecursiveGenericType.class)));
	}

	@Test
	public void testMultiBoundedRecursiveGenericType() throws Exception {
		assertNotNull(TypeFactory.getType(TypeDescriptor.valueOf(MultiBoundedRecursiveGenericType.class)));
	}

	@Test
	public void testMultiBoundedGenericType() throws Exception {
		ReifiedType reifiedA = TypeFactory.getType(TypeDescriptor.valueOf(A.class));
		assertNotNull(reifiedA);
		ReifiedType reifiedB = TypeFactory.getType(TypeDescriptor.valueOf(B.class));
		assertNotNull(reifiedB);
	}
	
	@Test
	public void testMutuallyRecursiveThroughSecondBoundGenericType() throws Exception {
	    assertNotNull(TypeFactory.getType(TypeDescriptor.valueOf(MutuallyRecursiveThroughSecondBoundGenericType.class)));
	}

	private ReifiedType getReifiedTypeFor(String methodName) {
		Method mt = BeanUtils.findDeclaredMethodWithMinimalParameters(TestSet.class, methodName);
		TypeDescriptor td = new TypeDescriptor(new MethodParameter(mt, 0));
		return TypeFactory.getType(td);
	}
}