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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

/**
 * @author Costin Leau
 * 
 */
public class MockBundleTest {

	MockBundle mock;

	@Before
	public void setup() throws Exception {
		mock = new MockBundle();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#MockBundle()}.
	 */
	@Test
	public void testMockBundle() {
		assertNotNull(mock.getLocation());
		assertNotNull(mock.getHeaders());
		assertNotNull(mock.getSymbolicName());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#MockBundle(java.util.Dictionary)}.
	 */
	@Test
	public void testMockBundleDictionary() {
		Dictionary headers = new Hashtable();

		mock = new MockBundle(headers);

		assertNotNull(mock.getLocation());
		assertSame(headers, mock.getHeaders());
		assertNotNull(mock.getSymbolicName());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#MockBundle(java.lang.String)}.
	 */
	@Test
	public void testMockBundleString() {
		String location = "some location";
		String symName = "symName";
		mock = new MockBundle(symName);
		((MockBundle) mock).setLocation(location);

		assertSame(location, mock.getLocation());
		assertNotNull(mock.getHeaders());
		assertEquals(symName, mock.getSymbolicName());
		assertEquals(location, mock.getLocation());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#MockBundle(org.osgi.framework.BundleContext)}.
	 */
	@Test
	public void testMockBundleBundleContext() {
		BundleContext context = new MockBundleContext();

		mock = new MockBundle(context);
		assertNotNull(mock.getLocation());
		assertNotNull(mock.getHeaders());
		assertNotNull(mock.getSymbolicName());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#MockBundle(java.lang.String, java.util.Dictionary, org.osgi.framework.BundleContext)}.
	 */
	@Test
	public void testMockBundleStringDictionaryBundleContext() {
		BundleContext context = new MockBundleContext();
		String location = "some location";
		Dictionary headers = new Hashtable();

		mock = new MockBundle(location, headers, context);

		assertNotNull(mock.getLocation());
		assertNotNull(mock.getHeaders());
		assertNotNull(mock.getSymbolicName());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#findEntries(java.lang.String, java.lang.String, boolean)}.
	 */
	@Test
	public void testFindEntries() {
		Enumeration enm = mock.findEntries("", null, true);
		assertNotNull(enm);
		assertFalse(enm.hasMoreElements());

		try {
			enm.nextElement();
			fail("expected exception");
		}
		catch (NoSuchElementException ex) {
			// expected
		}

	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getBundleId()}.
	 */
	@Test
	public void testGetBundleId() {
		assertTrue(mock.getBundleId() != 0);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getEntry(java.lang.String)}.
	 */
	@Test
	public void testGetEntry() throws Exception {
		assertNotNull(mock.getEntry(MockBundleTest.class.getPackage().getName().replace('.', '/')
				+ "/MockBundleTest.class"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getEntryPaths(java.lang.String)}.
	 */
	@Test
	public void testGetEntryPaths() {
		assertNotNull(mock.getEntryPaths(null));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getHeaders()}.
	 */
	@Test
	public void testGetHeaders() {
		assertNotNull(mock.getHeaders());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getHeaders(java.lang.String)}.
	 */
	@Test
	public void testGetHeadersString() {
		assertNotNull(mock.getHeaders("GB"));
		assertSame(mock.getHeaders(), mock.getHeaders("RO"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getLastModified()}.
	 */
	@Test
	public void testGetLastModified() {
		assertEquals(0, mock.getLastModified());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getLocation()}.
	 */
	@Test
	public void testGetLocation() {
		assertNotNull(mock.getLocation());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getRegisteredServices()}.
	 */
	@Test
	public void testGetRegisteredServices() {
		assertNotNull(mock.getRegisteredServices());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getResource(java.lang.String)}.
	 */
	@Test
	public void testGetResource() {
		assertNotNull(mock.getResource(MockBundleTest.class.getPackage().getName().replace('.', '/')
				+ "/MockBundleTest.class"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getResources(java.lang.String)}.
	 */
	@Test
	public void testGetResources() throws Exception {
		assertNotNull(mock.getResources(MockBundleTest.class.getPackage().getName().replace('.', '/')));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getServicesInUse()}.
	 */
	@Test
	public void testGetServicesInUse() {
		assertNotNull(mock.getServicesInUse());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getState()}.
	 */
	@Test
	public void testGetState() {
		assertEquals(Bundle.ACTIVE, mock.getState());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#getSymbolicName()}.
	 */
	@Test
	public void testGetSymbolicName() {
		assertNotNull(mock.getSymbolicName());

		Dictionary headers = new Hashtable();
		String symbolic = "symbolicName";
		headers.put(Constants.BUNDLE_SYMBOLICNAME, symbolic);
		mock = new MockBundle(headers);

		assertSame(mock.getSymbolicName(), symbolic);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#hasPermission(java.lang.Object)}.
	 */
	@Test
	public void testHasPermission() {
		assertEquals(true, mock.hasPermission(new Object()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundle#loadClass(java.lang.String)}.
	 */
	@Test
	public void testLoadClass() throws Exception {
		assertSame(getClass(), mock.loadClass(MockBundleTest.class.getName()));
	}

	@Test
	public void testGetEmptyEnumerator() {
		Enumeration enm = mock.getEntryPaths("bla");
		assertFalse(enm.hasMoreElements());

		try {
			enm.nextElement();
			fail("should have thrown exception");
		}
		catch (NoSuchElementException nsee) {
			// expected
		}
	}

	@Test
	public void testDefaultStart() throws Exception {
		mock = new MockBundle() {

			public void start(int options) throws BundleException {
				assertEquals(0, options);
			}
		};
		mock.start();
	}

	@Test
	public void testStartWithOptions() throws Exception {
		mock = new MockBundle() {

			public void start(int options) throws BundleException {
				assertEquals(3, options);
			}
		};

		mock.start(3);
	}

	@Test
	public void testDefaultStop() throws Exception {
		mock = new MockBundle() {

			public void stop(int options) throws BundleException {
				assertEquals(0, options);
			}
		};
		mock.stop();
	}

	@Test
	public void testStopWithOptions() throws Exception {
		mock = new MockBundle() {

			public void stop(int options) throws BundleException {
				assertEquals(3, options);
			}
		};
		mock.stop(3);
	}

	@Test
	public void testDefaultGetBundleContext() throws Exception {
		assertNotNull(mock.getBundleContext());
	}

	@Test
	public void testBundleContextSpecified() throws Exception {
		BundleContext ctx = new MockBundleContext();
		mock = new MockBundle(ctx);
		assertSame(ctx, mock.getBundleContext());
	}
}
