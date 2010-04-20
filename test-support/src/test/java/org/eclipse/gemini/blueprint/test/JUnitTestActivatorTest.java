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

package org.eclipse.gemini.blueprint.test;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.eclipse.gemini.blueprint.test.internal.OsgiJUnitTest;
import org.eclipse.gemini.blueprint.test.internal.TestRunnerService;
import org.eclipse.gemini.blueprint.test.internal.holder.OsgiTestInfoHolder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class JUnitTestActivatorTest extends TestCase {

	private JUnitTestActivator activator;


	// private ServiceRegistration registration;
	// private ServiceReference reference;

	public static class TestExample extends TestCase implements OsgiJUnitTest {

		private static BundleContext context;


		public void osgiSetUp() throws Exception {
		}

		public void osgiTearDown() throws Exception {
		}

		public void osgiRunTest() throws Throwable {
		}

		public void injectBundleContext(BundleContext bundleContext) {
			context = bundleContext;
		}

		public BundleContext getBundleContext() {
			return context;
		}

		public void setName(String name) {
		}

		public Bundle findBundleByLocation(String bundleLocation) {
			return null;
		}

		public Bundle findBundleBySymbolicName(String bundleSymbolicName) {
			return null;
		}

		public TestCase getTestCase() {
			return this;
		}

	}


	protected void setUp() throws Exception {
		activator = new JUnitTestActivator();
		// reference = new MockServiceReference();
		// registration = new MockServiceRegistration();
	}

	protected void tearDown() throws Exception {

	}

	public void testStart() throws Exception {
		MockControl ctxCtrl = MockControl.createControl(BundleContext.class);
		BundleContext ctx = (BundleContext) ctxCtrl.getMock();

		MockControl servCtrl = MockControl.createControl(TestRunnerService.class);
		TestRunnerService runner = (TestRunnerService) servCtrl.getMock();

		ServiceReference ref = new MockServiceReference();

		ctxCtrl.expectAndReturn(ctx.getServiceReference(TestRunnerService.class.getName()), ref);
		ctxCtrl.expectAndReturn(ctx.getService(ref), runner);

		ctx.registerService((String) null, null, null);
		ctxCtrl.setMatcher(MockControl.ALWAYS_MATCHER);
		ctxCtrl.setReturnValue(null);

		ctxCtrl.replay();
		servCtrl.replay();

		activator.start(ctx);

		ctxCtrl.verify();
	}

	public void testStop() throws Exception {
		ServiceReference ref = new MockServiceReference();
		MockControl regCtrl = MockControl.createControl(ServiceRegistration.class);
		ServiceRegistration reg = (ServiceRegistration) regCtrl.getMock();

		MockControl ctxCtrl = MockControl.createControl(BundleContext.class);
		BundleContext ctx = (BundleContext) ctxCtrl.getMock();

		reg.unregister();

		ctxCtrl.replay();
		regCtrl.replay();

		setActivatorField("reference", ref);
		setActivatorField("registration", reg);

		activator.stop(ctx);

		regCtrl.verify();
		ctxCtrl.verify();
	}

	public void testLoadTest() throws Exception {
		BundleContext ctx = new MockBundleContext();

		MockControl servCtrl = MockControl.createControl(TestRunnerService.class);
		TestRunnerService runner = (TestRunnerService) servCtrl.getMock();

		try {
			activator.executeTest();
			fail("should have thrown exception");
		}
		catch (RuntimeException ex) {
			// expected
		}

		setActivatorField("service", runner);
		runner.runTest(null);
		servCtrl.setMatcher(MockControl.ALWAYS_MATCHER);
		servCtrl.replay();

		setActivatorField("context", ctx);
		OsgiTestInfoHolder.INSTANCE.setTestClassName(TestExample.class.getName());

		activator.executeTest();
		assertSame(ctx, TestExample.context);
		servCtrl.verify();
	}

	private void setActivatorField(String fieldName, Object value) throws Exception {
		Field field = JUnitTestActivator.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(activator, value);

	}
}
