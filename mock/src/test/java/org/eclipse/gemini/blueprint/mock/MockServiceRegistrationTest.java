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

package org.eclipse.gemini.blueprint.mock;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;

import org.osgi.framework.ServiceReference;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * @author Costin Leau
 * 
 */
public class MockServiceRegistrationTest {

	MockServiceRegistration mock;

	@Before
	public void setup() throws Exception {
		mock = new MockServiceRegistration();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceRegistration#MockServiceRegistration()}.
	 */
	@Test
	public void testMockServiceRegistration() {
		assertNotNull(mock.getReference());
		assertNotNull(mock.getReference().getPropertyKeys());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceRegistration#MockServiceRegistration(java.util.Hashtable)}.
	 */
	@Test
	public void testMockServiceRegistrationHashtable() {
		Dictionary props = new Hashtable();
		Object value = new Object();
		props.put("foo", value);

		assertNotNull(mock.getReference());
		mock = new MockServiceRegistration(props);
		assertSame(value, mock.getReference().getProperty("foo"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceRegistration#getReference()}.
	 */
	@Test
	public void testGetReference() {
		assertNotNull(mock.getReference());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceRegistration#setReference(org.osgi.framework.ServiceReference)}.
	 */
	@Test
	public void testSetReference() {
		ServiceReference ref = new MockServiceReference();
		mock.setReference(ref);
		assertSame(ref, mock.getReference());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceRegistration#setProperties(java.util.Dictionary)}.
	 */
	@Test
	public void testSetProperties() {
		Dictionary props = new Hashtable();
		Object value = new Object();
		String key = "foo";
		props.put(key, value);

		assertNull(mock.getReference().getProperty(key));
		mock.setProperties(props);
		assertSame(value, mock.getReference().getProperty(key));
		mock.setReference(createMock(ServiceReference.class));

		try {
			mock.setProperties(props);
			fail("should have thrown exception");
		}
		catch (RuntimeException ex) {
			// expected
		}
	}

	@Test
	public void testHashCode() {
		MockServiceReference ref = new MockServiceReference();
		mock.setReference(ref);

		MockServiceRegistration other = new MockServiceRegistration();
		other.setReference(ref);

		assertEquals(mock.hashCode(), other.hashCode());

	}

	@Test
	public void testHashCodeWithDifferentServiceRef() {
		MockServiceRegistration other = new MockServiceRegistration();
		assertFalse(mock.hashCode() == other.hashCode());
	}

	@Test
	public void testHashCodeSelf() {
		assertEquals(mock.hashCode(), mock.hashCode());

		mock.setReference(new MockServiceReference());
		assertEquals(mock.hashCode(), mock.hashCode());
	}

	@Test
	public void testEqualsTrue() {
		MockServiceReference ref = new MockServiceReference();
		mock.setReference(ref);

		MockServiceRegistration other = new MockServiceRegistration();
		other.setReference(ref);

		assertEquals(mock, other);
	}

	@Test
	public void testEqualsFalse() {
		MockServiceRegistration other = new MockServiceRegistration();
		assertFalse(mock.equals(other));
	}

	@Test
	public void testEqualsThis() {
		assertEquals(mock, mock);
	}
}
