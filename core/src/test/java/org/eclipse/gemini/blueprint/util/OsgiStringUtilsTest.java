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

package org.eclipse.gemini.blueprint.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

/**
 * @author Costin Leau
 * 
 */
public class OsgiStringUtilsTest {

	private static int state;

	private Bundle bundle;

	@Before
	public void setup() throws Exception {
		OsgiStringUtilsTest.state = Bundle.UNINSTALLED;
		bundle = new MockBundle() {

			public int getState() {
				return state;
			}
		};
	}

	@Test
	public void testGetBundleEventAsString() {
		assertEquals("INSTALLED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.INSTALLED));
		assertEquals("STARTING", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.STARTING));
		assertEquals("UNINSTALLED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.UNINSTALLED));
		assertEquals("UPDATED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.UPDATED));
		assertTrue(OsgiStringUtils.nullSafeBundleEventToString(-1324).startsWith("UNKNOWN"));
	}

	@Test
	public void testGetBundleStateAsName() throws Exception {
		OsgiStringUtilsTest.state = Bundle.ACTIVE;
		assertEquals("ACTIVE", OsgiStringUtils.bundleStateAsString(bundle));
		OsgiStringUtilsTest.state = Bundle.STARTING;
		assertEquals("STARTING", OsgiStringUtils.bundleStateAsString(bundle));
		OsgiStringUtilsTest.state = Bundle.STOPPING;
		assertEquals("STOPPING", OsgiStringUtils.bundleStateAsString(bundle));
		OsgiStringUtilsTest.state = -123;
		assertEquals("UNKNOWN STATE", OsgiStringUtils.bundleStateAsString(bundle));
	}

	@Test
	public void testNullSafeToStringBundleEvent() throws Exception {
		assertEquals("INSTALLED", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.INSTALLED, bundle)));
		assertEquals("UPDATED", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.UPDATED, bundle)));
		assertEquals("STOPPING", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.STOPPING, bundle)));
	}

	@Test
	public void testNullSafeToStringBundleEventNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((BundleEvent) null));
	}

	@Test
	public void testNullSafeToStringBundleEventInvalidType() throws Exception {
		assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new BundleEvent(-123, bundle)));
	}

	@Test
	public void testNullSafeToStringServiceEvent() throws Exception {
		ServiceReference ref = new MockServiceReference();
		assertEquals("REGISTERED", OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.REGISTERED, ref)));
		assertEquals("MODIFIED", OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.MODIFIED, ref)));
		assertEquals("UNREGISTERING",
			OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.UNREGISTERING, ref)));
	}

	@Test
	public void testNullSafeToStringServiceEventNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((ServiceEvent) null));
	}

	@Test
	public void testNullSafeToStringServiceEventInvalidType() throws Exception {
		assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new ServiceEvent(-123,
			new MockServiceReference())));
	}

	@Test
	public void testNullSafeToStringFrameworkEvent() throws Exception {
		Bundle bundle = new MockBundle();
		Throwable th = new Exception();
		assertEquals("STARTED",
			OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.STARTED, bundle, th)));
		assertEquals("ERROR", OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.ERROR, bundle, th)));

		assertEquals("WARNING",
			OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.WARNING, bundle, th)));

		assertEquals("INFO", OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.INFO, bundle, th)));

		assertEquals("PACKAGES_REFRESHED", OsgiStringUtils.nullSafeToString(new FrameworkEvent(
			FrameworkEvent.PACKAGES_REFRESHED, bundle, th)));

		assertEquals("STARTLEVEL_CHANGED", OsgiStringUtils.nullSafeToString(new FrameworkEvent(
			FrameworkEvent.STARTLEVEL_CHANGED, bundle, th)));
	}

	@Test
	public void testNullSafeToStringFrameworkEventNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((FrameworkEvent) null));
	}

	@Test
	public void testNullSafeToStringFrameworkEventInvalidType() throws Exception {
		assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new FrameworkEvent(-123, bundle,
			new Exception())));
	}

	@Test
	public void testNullSafeToStringServiceReference() throws Exception {
		String symName = "symName";

		MockBundle bundle = new MockBundle(symName);
		Properties props = new Properties();
		String header = "HEADER";
		String value = "VALUE";
		props.put(header, value);
		MockServiceReference ref = new MockServiceReference(bundle, props, null);
		String out = OsgiStringUtils.nullSafeToString(ref);
		assertTrue(out.indexOf(symName) > -1);
		assertTrue(out.indexOf(header) > -1);
		assertTrue(out.indexOf(value) > -1);
	}

	@Test
	public void testNullSafeToStringServiceReferenceNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((ServiceReference) null));
	}
}
