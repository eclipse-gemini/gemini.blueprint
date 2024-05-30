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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Hashtable;

import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.eclipse.gemini.blueprint.test.internal.OsgiJUnitTest;
import org.eclipse.gemini.blueprint.test.internal.TestRunnerService;
import org.eclipse.gemini.blueprint.test.internal.holder.OsgiTestInfoHolder;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import junit.framework.TestCase;

public class JUnitTestActivatorTest {

	private JUnitTestActivator activator;

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

	@Before
	public void setUp() throws Exception {
		activator = new JUnitTestActivator();
	}

	@Test
	public void testStart() throws Exception {
		BundleContext ctx = createMock(BundleContext.class);

		TestRunnerService runner = createMock(TestRunnerService.class);

		ServiceReference ref = new MockServiceReference();

		expect(ctx.getServiceReference(TestRunnerService.class)).andReturn(ref);
		expect(ctx.getService(ref)).andReturn(runner);
		expect(ctx.registerService(eq(JUnitTestActivator.class), eq(activator), eq(new Hashtable<String, Object>()))).andReturn(null);

		replay(ctx, runner);
		activator.start(ctx);
		verify(ctx, runner);
	}

	@Test
	public void testStop() throws Exception {
		ServiceReference ref = new MockServiceReference();
		ServiceRegistration reg = createMock(ServiceRegistration.class);

		BundleContext ctx = createMock(BundleContext.class);

		reg.unregister();
        expectLastCall();

		replay(ctx, reg);

		setActivatorField("reference", ref);
		setActivatorField("registration", reg);

		activator.stop(ctx);

		verify(ctx, reg);
	}

	@Test
	public void testLoadTest() throws Exception {
		BundleContext ctx = new MockBundleContext();
		TestRunnerService runner = createMock(TestRunnerService.class);

		try {
			activator.executeTest();
			fail("should have thrown exception");
		} catch (RuntimeException ex) {
			// expected
		}

		setActivatorField("service", runner);
		runner.runTest(anyObject(OsgiJUnitTest.class));
        expectLastCall();
		replay(runner);

		setActivatorField("context", ctx);
		OsgiTestInfoHolder.INSTANCE.setTestClassName(TestExample.class.getName());

		activator.executeTest();
		assertSame(ctx, TestExample.context);
		verify(runner);
	}

	private void setActivatorField(String fieldName, Object value) throws Exception {
		Field field = JUnitTestActivator.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(activator, value);
	}
}
