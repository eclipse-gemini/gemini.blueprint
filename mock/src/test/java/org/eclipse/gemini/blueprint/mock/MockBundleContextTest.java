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

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

/**
 * @author Costin Leau
 * 
 */
public class MockBundleContextTest {

	MockBundleContext mock;

	@Before
	public void setup() throws Exception {
		mock = new MockBundleContext();
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#MockBundleContext()}.
	 */
	@Test
	public void testMockBundleContext() {
		assertNotNull(mock.getBundle());
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VENDOR));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#MockBundleContext(org.osgi.framework.Bundle)}.
	 */
	@Test
	public void testMockBundleContextBundle() {
		Bundle bundle = new MockBundle();
		mock = new MockBundleContext(bundle);

		assertSame(bundle, mock.getBundle());
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VENDOR));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#MockBundleContext(org.osgi.framework.Bundle, java.util.Properties)}.
	 */
	@Test
	public void testMockBundleContextBundleProperties() {
		Bundle bundle = new MockBundle();
		Properties props = new Properties();

		mock = new MockBundleContext(bundle, props);
		assertSame(bundle, mock.getBundle());
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VENDOR));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#createFilter(java.lang.String)}.
	 */
	@Test
	public void testCreateFilter() throws Exception {
		assertNotNull(mock.createFilter("foo-bar"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#getAllServiceReferences(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetAllServiceReferences() throws Exception {
		assertNotNull(mock.getAllServiceReferences(getClass().getName(), "*"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#getBundle()}.
	 */
	@Test
	public void testGetBundle() {
		assertNotNull(mock.getBundle());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#getBundle(long)}.
	 */
	@Test
	public void testGetBundleLong() {
		assertNotNull(mock.getBundle(123));
		assertSame(mock.getBundle(), mock.getBundle(321));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#getBundles()}.
	 */
	@Test
	public void testGetBundles() {
		assertNotNull(mock.getBundles());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#getDataFile(java.lang.String)}.
	 */
	@Test
	public void testGetDataFile() {
		assertNull(mock.getDataFile(""));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#getProperty(java.lang.String)}.
	 */
	@Test
	public void testGetProperty() {
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VERSION));
		assertNull(mock.getProperty("foo"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#getService(org.osgi.framework.ServiceReference)}.
	 */
	@Test
	public void testGetService() {
		assertNotNull(mock.getService(new MockServiceReference()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#getServiceReference(java.lang.String)}.
	 */
	@Test
	public void testGetServiceReference() throws Exception {
		assertNotNull(mock.getServiceReference(getClass().getName()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#getServiceReferences(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetServiceReferences() throws Exception {
		assertNotNull(mock.getServiceReferences(getClass().getName(), "*"));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#installBundle(java.lang.String)}.
	 */
	@Test
	public void testInstallBundleString() throws Exception {
		String location = "location";
		assertSame(location, mock.installBundle(location).getLocation());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#installBundle(java.lang.String, java.io.InputStream)}.
	 */
	@Test
	public void testInstallBundleStringInputStream() throws Exception {
		String location = "location";
		assertSame(location, mock.installBundle(location, new ByteArrayInputStream(new byte[0])).getLocation());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#registerService(java.lang.String[], java.lang.Object, java.util.Dictionary)}.
	 */
	@Test
	public void testRegisterServiceStringArrayObjectDictionary() {
		assertNotNull(mock.registerService(new String[] { "foo" }, new Object(), new Hashtable()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#registerService(java.lang.String, java.lang.Object, java.util.Dictionary)}.
	 */
	@Test
	public void testRegisterServiceStringObjectDictionary() {
		assertNotNull(mock.registerService("foo", new Object(), new Hashtable()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.mock.MockBundleContext#ungetService(org.osgi.framework.ServiceReference)}.
	 */
	@Test
	public void testUngetService() {
		assertFalse(mock.ungetService(new MockServiceReference()));
	}

	@Test
	public void testMandatoryPropertiesAvailable() {
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VERSION));
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_VENDOR));
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_LANGUAGE));
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_OS_NAME));
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_OS_VERSION));
		assertNotNull(mock.getProperty(Constants.FRAMEWORK_PROCESSOR));
	}

	@Test
	public void testAddServiceListener() throws Exception {
		ServiceListener listener = new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
			}
		};
		mock.addServiceListener(listener);

		assertEquals(1, mock.getServiceListeners().size());
		assertSame(listener, mock.getServiceListeners().iterator().next());
	}

	@Test
	public void testRemoveServiceListener() throws Exception {
		ServiceListener listener = new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
			}
		};

		Set listeners = mock.getServiceListeners();

		mock.removeServiceListener(null);
		
		assertEquals(0, listeners.size());
		
		mock.removeServiceListener(listener);
		assertEquals(0, listeners.size());
		
		mock.addServiceListener(listener);
		assertEquals(1, listeners.size());
		
		mock.removeServiceListener(listener);
		assertEquals(0, listeners.size());
	}
}
