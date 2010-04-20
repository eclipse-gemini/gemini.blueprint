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

package org.eclipse.gemini.blueprint.service.importer.support.internal.aop;

import java.util.Arrays;
import java.util.Properties;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * 
 * @author Costin Leau
 * 
 */
public class SwappingServiceReferenceProxyTest extends TestCase {

	private SwappingServiceReferenceProxy reference;
	private ServiceReference serviceReference;


	protected void setUp() throws Exception {
		Properties props = new Properties();
		props.setProperty("composer", "Rachmaninoff");
		reference = new SwappingServiceReferenceProxy();
		serviceReference = new MockServiceReference();
	}

	protected void tearDown() throws Exception {
		reference = null;
		serviceReference = null;
	}

	public void testHashCode() {
		assertTrue(reference.hashCode() != 0);
	}

	public void testGetBundle() {
		assertNull(reference.getBundle());
	}

	public void testGetProperty() {
		assertNull(reference.getProperty("foo"));
	}

	public void testGetPropertyKeys() {
		String[] array = reference.getPropertyKeys();
		assertNotNull(array);
		assertEquals(0, array.length);
	}

	public void testGetUsingBundles() {
		Bundle[] array = reference.getUsingBundles();
		assertNotNull(array);
		assertEquals(0, array.length);
	}

	public void testIsAssignableTo() {
		assertFalse(reference.isAssignableTo(new MockBundle(), "Object"));
	}

	public void testGetTargetServiceReference() {
		assertNull(reference.getTargetServiceReference());
		assertNull(reference.swapDelegates(serviceReference));
		assertSame(serviceReference, reference.getTargetServiceReference());
	}

	public void testEqualsObject() {
		SwappingServiceReferenceProxy anotherRef = new SwappingServiceReferenceProxy();
		assertEquals(anotherRef, reference);
		assertEquals(reference, anotherRef);
		anotherRef.swapDelegates(serviceReference);
		assertFalse(anotherRef.equals(reference));
		assertFalse(reference.equals(anotherRef));
		assertEquals(reference, reference);
	}

	public void testSwapDelegates() {
		int originalHashCode = reference.hashCode();
		assertNull(reference.swapDelegates(serviceReference));
		assertSame(serviceReference, reference.getTargetServiceReference());
		assertFalse(originalHashCode == reference.hashCode());
		assertSame(serviceReference, reference.getTargetServiceReference());
	}

	public void testHashCodeWithNotNullDelegate() {
		int originalHashCode = reference.hashCode();
		reference.swapDelegates(serviceReference);
		assertFalse(originalHashCode == reference.hashCode());
		assertEquals(reference.hashCode(), reference.hashCode());
	}

	public void testGetBundleWithNotNullDelegate() {
		reference.swapDelegates(serviceReference);
		assertSame(serviceReference.getBundle(), reference.getBundle());
	}

	public void testGetPropertyWithNotNullDelegate() {
		reference.swapDelegates(serviceReference);
		assertSame(serviceReference.getProperty("composer"), reference.getProperty("composer"));
		assertSame(serviceReference.getProperty("foo"), reference.getProperty("foo"));
	}

	public void testGetPropertyKeysWithNotNullDelegate() {
		reference.swapDelegates(serviceReference);
		assertTrue(Arrays.equals(serviceReference.getPropertyKeys(), reference.getPropertyKeys()));
	}

	public void testGetUsingBundlesWithNotNullDelegate() {
		reference.swapDelegates(serviceReference);
		assertTrue(Arrays.equals(serviceReference.getUsingBundles(), reference.getUsingBundles()));
	}

	public void testIsAssignableToWithNotNullDelegate() {
		MockBundle bundle = new MockBundle();
		String className = "Object";
		reference.swapDelegates(serviceReference);
		assertEquals(serviceReference.isAssignableTo(bundle, className), reference.isAssignableTo(bundle, className));
	}

	public void testEqualsObjectWithNotNullDelegate() {
		reference.swapDelegates(serviceReference);
		SwappingServiceReferenceProxy anotherRef = new SwappingServiceReferenceProxy();
		assertFalse(anotherRef.equals(reference));
		assertFalse(reference.equals(anotherRef));
		assertEquals(reference, reference);
		anotherRef.swapDelegates(serviceReference);
		assertEquals(anotherRef, reference);
		assertEquals(reference, anotherRef);
		assertEquals(reference, reference);
	}

	public void testCompareToItself() throws Exception {
		assertEquals(0, reference.compareTo(reference));
	}

	public void testDefaultCompareTo() throws Exception {
		assertEquals(0, reference.compareTo(new SwappingServiceReferenceProxy()));
	}

	public void testCompareToDifferentService() throws Exception {
		SwappingServiceReferenceProxy proxy = new SwappingServiceReferenceProxy();
		proxy.swapDelegates(new MockServiceReference());
		try {
			reference.compareTo(proxy);
			fail("expected CCE");
		}
		catch (Exception ex) {

		}
	}
}
