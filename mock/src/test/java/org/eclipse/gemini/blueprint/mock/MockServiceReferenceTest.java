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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * @author Costin Leau
 * 
 */
public class MockServiceReferenceTest {

	MockServiceReference mock;

	@Before
	public void setup() throws Exception {
		mock = new MockServiceReference();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceReference#MockServiceReference()}.
	 */
	@Test
	public void testMockServiceReference() {
		assertNotNull(mock.getBundle());
		assertNotNull(mock.getPropertyKeys());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceReference#MockServiceReference(org.osgi.framework.Bundle)}.
	 */
	@Test
	public void testMockServiceReferenceBundle() {
		Bundle bundle = new MockBundle();
		mock = new MockServiceReference(bundle);

		assertSame(bundle, mock.getBundle());
		assertNotNull(mock.getPropertyKeys());

	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceReference#MockServiceReference(org.osgi.framework.Bundle, java.util.Hashtable, org.osgi.framework.ServiceRegistration)}.
	 */
	@Test
	public void testMockServiceReferenceBundleHashtable() {
		mock = new MockServiceReference(null, null, null);
		assertNotNull(mock.getBundle());
		assertNotNull(mock.getPropertyKeys());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceReference#getBundle()}.
	 */
	@Test
	public void testGetBundle() {
		assertNotNull(mock.getBundle());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceReference#getProperty(java.lang.String)}.
	 */
	@Test
	public void testGetProperty() {
		assertNull(mock.getProperty("foo"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceReference#getPropertyKeys()}.
	 */
	@Test
	public void testGetPropertyKeys() {
		assertNotNull(mock.getPropertyKeys());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceReference#getUsingBundles()}.
	 */
	@Test
	public void testGetUsingBundles() {
		assertNotNull(mock.getUsingBundles());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceReference#isAssignableTo(org.osgi.framework.Bundle, java.lang.String)}.
	 */
	@Test
	public void testIsAssignableTo() {
		assertFalse(mock.isAssignableTo(new MockBundle(), "foo"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockServiceReference#setProperties(java.util.Dictionary)}.
	 */
	@Test
	public void testSetProperties() {
		Dictionary props = new Hashtable();
		String key = "foo";
		Object value = new Object();
		props.put(key, value);

		assertNull(mock.getProperty(key));
		mock.setProperties(props);
		assertSame(value, mock.getProperty(key));
	}

	@Test
	public void testMandatoryProperties() {
		Object serviceId = mock.getProperty(Constants.SERVICE_ID);
		assertNotNull(serviceId);
		assertTrue(serviceId instanceof Long);
		Object objectClass = mock.getProperty(Constants.OBJECTCLASS);
		assertNotNull(objectClass);
		assertTrue(objectClass instanceof String[]);
	}

	@Test
	public void testMandatoryPropertiesDontChange() {
		Object serviceId = mock.getProperty(Constants.SERVICE_ID);
		Object objectClass = mock.getProperty(Constants.OBJECTCLASS);

		mock.setProperties(new Hashtable());
		assertSame(serviceId, mock.getProperty(Constants.SERVICE_ID));
		assertSame(objectClass, mock.getProperty(Constants.OBJECTCLASS));

		Dictionary anotherDict = new Hashtable();
		anotherDict.put(Constants.SERVICE_ID, new Long(1234));
		anotherDict.put(Constants.OBJECTCLASS, new String[] { Date.class.getName() });
		mock.setProperties(anotherDict);

		assertSame(serviceId, mock.getProperty(Constants.SERVICE_ID));
		assertSame(objectClass, mock.getProperty(Constants.OBJECTCLASS));
	}

	@Test
	public void testCompareReferencesWithTheSameId() throws Exception {
		MockServiceReference refA = createReference(new Long(1), null);
		MockServiceReference refB = createReference(new Long(1), null);

		// refA is higher then refB
		assertEquals(0, refA.compareTo(refB));
		assertEquals(0, refB.compareTo(refA));
	}

	@Test
	public void testServiceRefsWithDifferentIdAndNoRanking() throws Exception {
		MockServiceReference refA = createReference(new Long(1), null);
		MockServiceReference refB = createReference(new Long(2), null);

		// refA is higher then refB
		// default ranking is equal
		assertTrue(refA.compareTo(refB) > 0);
		assertTrue(refB.compareTo(refA) < 0);
	}

	@Test
	public void testServiceRefsWithDifferentIdAndDifferentRanking() throws Exception {
		MockServiceReference refA = createReference(new Long(1), Integer.valueOf(0));
		MockServiceReference refB = createReference(new Long(2), Integer.valueOf(1));

		// refB is higher then refA (due to ranking)
		assertTrue(refA.compareTo(refB) < 0);
		assertTrue(refB.compareTo(refA) > 0);
	}

	@Test
	public void testServiceRefsWithSameRankAndDifId() throws Exception {
		MockServiceReference refA = createReference(new Long(1), Integer.valueOf(5));
		MockServiceReference refB = createReference(new Long(2), Integer.valueOf(5));

		// same ranking, means id equality applies
		assertTrue(refA.compareTo(refB) > 0);
		assertTrue(refB.compareTo(refA) < 0);
	}

	private MockServiceReference createReference(Long id, Integer ranking) {
		Dictionary dict = new Properties();
		dict.put(Constants.SERVICE_ID, id);
		if (ranking != null)
			dict.put(Constants.SERVICE_RANKING, ranking);

		return new MockServiceReference(null, dict, null);
	}
}
