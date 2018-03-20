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

import java.util.Properties;

import junit.framework.TestCase;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * @author Costin Leau
 * 
 */
public class OsgiStringUtilsTest extends TestCase {

	private static int state;

	private Bundle bundle;


	protected void setUp() throws Exception {
		OsgiStringUtilsTest.state = Bundle.UNINSTALLED;
		bundle = new MockBundle() {

			public int getState() {
				return state;
			}
		};
	}

	public void testGetBundleEventAsString() {
		assertEquals("INSTALLED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.INSTALLED));
		assertEquals("STARTING", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.STARTING));
		assertEquals("UNINSTALLED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.UNINSTALLED));
		assertEquals("UPDATED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.UPDATED));
		assertTrue(OsgiStringUtils.nullSafeBundleEventToString(-1324).startsWith("UNKNOWN"));
	}

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

	public void testNullSafeToStringBundleEvent() throws Exception {
		assertEquals("INSTALLED", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.INSTALLED, bundle)));
		assertEquals("UPDATED", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.UPDATED, bundle)));
		assertEquals("STOPPING", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.STOPPING, bundle)));
	}

	public void testNullSafeToStringBundleEventNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((BundleEvent) null));
	}

	public void testNullSafeToStringBundleEventInvalidType() throws Exception {
		assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new BundleEvent(-123, bundle)));
	}

	public void testNullSafeToStringServiceEvent() throws Exception {
		ServiceReference ref = new MockServiceReference();
		assertEquals("REGISTERED", OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.REGISTERED, ref)));
		assertEquals("MODIFIED", OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.MODIFIED, ref)));
		assertEquals("UNREGISTERING",
			OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.UNREGISTERING, ref)));
	}

	public void testNullSafeToStringServiceEventNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((ServiceEvent) null));
	}

	public void testNullSafeToStringServiceEventInvalidType() throws Exception {
		assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new ServiceEvent(-123,
			new MockServiceReference())));
	}

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

	public void testNullSafeToStringFrameworkEventNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((FrameworkEvent) null));
	}

	public void testNullSafeToStringFrameworkEventInvalidType() throws Exception {
		assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new FrameworkEvent(-123, bundle,
			new Exception())));
	}

	public void testNullSafeToStringServiceReference() throws Exception {
		String symName = "symName";

		MockBundle bundle = new MockBundle(symName);
		Properties props = new Properties();
		String header = "HEADER";
		String value = "VALUE";
		props.put(header, value);
		MockServiceReference ref = new MockServiceReference(bundle, props);
		String out = OsgiStringUtils.nullSafeToString(ref);
		assertTrue(out.indexOf(symName) > -1);
		assertTrue(out.indexOf(header) > -1);
		assertTrue(out.indexOf(value) > -1);
	}

	public void testNullSafeToStringServiceReferenceNull() throws Exception {
		assertNotNull(OsgiStringUtils.nullSafeToString((ServiceReference) null));
	}
}
