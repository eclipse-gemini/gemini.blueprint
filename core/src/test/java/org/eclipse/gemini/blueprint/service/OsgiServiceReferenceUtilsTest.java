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

package org.eclipse.gemini.blueprint.service;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

import org.easymock.IMocksControl;
import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * @author Costin Leau
 * 
 */
public class OsgiServiceReferenceUtilsTest extends TestCase {

	private String[] classes;

	// private Dictionary dict;

	private BundleContext context;

	private ServiceReference ref1, ref2, ref3;

	IMocksControl ctrl;

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		classes = new String[] { Object.class.getName(), Cloneable.class.getName(), Serializable.class.getName() };

		// lowest service reference
		Dictionary dict1 = new Hashtable();
		dict1.put(Constants.SERVICE_RANKING, Integer.MIN_VALUE);
		ref1 = new MockServiceReference(null, dict1);

		// neutral service reference
		Dictionary dict2 = new Hashtable();
		dict2.put(Constants.SERVICE_ID, (long) 20);

		ref2 = new MockServiceReference(null, dict2);

		// neutral service reference
		Dictionary dict3 = new Hashtable();
		dict3.put(Constants.SERVICE_ID, (long) 30);

		ref3 = new MockServiceReference(null, dict3);

		ctrl = createStrictControl();
		context = ctrl.createMock(BundleContext.class);

	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		classes = null;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils#getServiceReference(org.osgi.framework.BundleContext, java.lang.String[])}.
	 */
	public void testServiceSortingAlgorithm() throws Exception {
		String filter = OsgiFilterUtils.unifyFilter(classes, null);
		expect(context.getServiceReferences(classes[0], filter)).andReturn(new ServiceReference[] { ref1 });

		ctrl.replay();
		ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(context, classes, null);
		assertSame("wrong service reference picked up", ref1, ref);
		ctrl.verify();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils#getServiceReference(org.osgi.framework.BundleContext, java.lang.String, java.lang.String)}.
	 */
	public void testGetServiceReferenceBundleContextStringString() throws Exception {

		expect(context.getServiceReferences(Object.class.getName(), null)).andReturn(new ServiceReference[] { ref1, ref3, ref2 });

		ctrl.replay();
		// ref2 should win since it has the highest ranking and the lowest id
		ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(context, Object.class.getName(), null);
		assertSame("wrong service reference picked up", ref2, ref);
		ctrl.verify();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils#getServiceReference(org.osgi.framework.BundleContext, java.lang.String[], java.lang.String)}.
	 */
	public void testGetServiceReferenceBundleContextStringArrayString() throws Exception {
		String smallFilter = "(cn=John)";
		String filter = OsgiFilterUtils.unifyFilter(classes, smallFilter);
		expect(context.getServiceReferences(Object.class.getName(), filter)).andReturn(new ServiceReference[] { ref1 });

		ctrl.replay();
		ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(context, classes, smallFilter);
		assertSame("wrong service reference picked up", ref1, ref);
		ctrl.verify();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils#getServiceReference(org.osgi.framework.BundleContext, java.lang.String)}.
	 */
	public void testAlwaysGetAnArrayOfServiceReferences() throws Exception {
		expect(context.getServiceReferences(Object.class.getName(), null)).andReturn(null);
		ctrl.replay();
		ServiceReference[] refs = OsgiServiceReferenceUtils.getServiceReferences(context, Object.class.getName(), null);
		assertNotNull(refs);
		assertEquals(0, refs.length);
		ctrl.verify();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils#getServiceId(org.osgi.framework.ServiceReference)}.
	 */
	public void testGetServiceId() {
		long id = 12345;
		Dictionary dict = new Hashtable();
		dict.put(Constants.SERVICE_ID, id);
		ServiceReference ref = new MockServiceReference(null, dict);
		assertEquals(id, OsgiServiceReferenceUtils.getServiceId(ref));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils#getServiceRanking(org.osgi.framework.ServiceReference)}.
	 */
	public void testGetServiceRankingAvailable() {
		int ranking = 12345;
		Dictionary dict = new Hashtable();
		dict.put(Constants.SERVICE_RANKING, ranking);
		ServiceReference ref = new MockServiceReference(null, dict);
		assertEquals(ranking, OsgiServiceReferenceUtils.getServiceRanking(ref));
	}

	public void testGetServiceRankingWithInvalidClass() {
		int ranking = 12345;
		Dictionary dict = new Hashtable();
		dict.put(Constants.SERVICE_RANKING, (long) ranking);
		ServiceReference ref = new MockServiceReference(null, dict);
		assertEquals(0, OsgiServiceReferenceUtils.getServiceRanking(ref));
	}

	public void testGetServiceRankingWithNonExistingRanking() {
		Dictionary dict = new Hashtable() {
			// forbid adding the service ranking
			public synchronized Object put(Object key, Object value) {
				if (!Constants.SERVICE_RANKING.equals(key))
					return super.put(key, value);
				return null;
			}
		};

		ServiceReference ref = new MockServiceReference(null, dict);

		assertNull(ref.getProperty(Constants.SERVICE_RANKING));
		assertEquals(0, OsgiServiceReferenceUtils.getServiceRanking(ref));
	}

}
