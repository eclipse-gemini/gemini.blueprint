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

package org.eclipse.gemini.blueprint.internal.service.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Comparator;
import java.util.Dictionary;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.service.importer.support.internal.util.ServiceReferenceComparator;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * @author Costin Leau
 * 
 */
public class ServiceReferenceComparatorTest {

	private Comparator comparator;

	@Before
	public void setup() throws Exception {
		comparator = new ServiceReferenceComparator();
	}

	@After
	public void tearDown() throws Exception {
		comparator = null;
	}

	@Test
	public void testServiceRefsWithTheSameId() throws Exception {
		ServiceReference refA = createReference(Long.valueOf(1), null);
		ServiceReference refB = createReference(Long.valueOf(1), null);

		// refA is higher then refB
		assertEquals(0, comparator.compare(refA, refB));
	}

	@Test
	public void testServiceRefsWithDifferentIdAndNoRanking() throws Exception {
		ServiceReference refA = createReference(Long.valueOf(1), null);
		ServiceReference refB = createReference(Long.valueOf(2), null);

		// refA is higher then refB
		// default ranking is equal
		assertTrue(comparator.compare(refA, refB) > 0);
	}

	@Test
	public void testServiceRefsWithDifferentIdAndDifferentRanking() throws Exception {
		ServiceReference refA = createReference(Long.valueOf(1), Integer.valueOf(0));
		ServiceReference refB = createReference(Long.valueOf(2), Integer.valueOf(1));

		// refB is higher then refA (due to ranking)
		assertTrue(comparator.compare(refA, refB) < 0);
	}

	@Test
	public void testServiceRefsWithSameRankAndDifId() throws Exception {
		ServiceReference refA = createReference(Long.valueOf(1), Integer.valueOf(5));
		ServiceReference refB = createReference(Long.valueOf(2), Integer.valueOf(5));

		// same ranking, means id equality applies
		assertTrue(comparator.compare(refA, refB) > 0);
	}

	@Test
	public void testNullObjects() throws Exception {
		assertEquals(0, comparator.compare(null, null));
	}

	@Test
	public void testNonNullWithNull() throws Exception {
		try {
			comparator.compare(new MockServiceReference(), null);
			fail("should have thrown exception");
		}
		catch (ClassCastException cce) {
		}
	}

	private ServiceReference createReference(Long id, Integer ranking) {
		Dictionary dict = new Properties();
		dict.put(Constants.SERVICE_ID, id);
		if (ranking != null)
			dict.put(Constants.SERVICE_RANKING, ranking);

		return new MockServiceReference(null, dict, null);
	}
}
