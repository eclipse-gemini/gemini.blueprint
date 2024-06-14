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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;
import java.util.Hashtable;

import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.eclipse.gemini.blueprint.test.internal.OsgiJUnitTest;
import org.eclipse.gemini.blueprint.test.internal.holder.OsgiTestInfoHolder;
import org.eclipse.gemini.blueprint.test.internal.support.OsgiJUnitService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class JUnitTestActivatorTest {

	private JUnitTestActivator activator;

	public static class TestExample implements OsgiJUnitTest {

		private static BundleContext context;

		public void osgiSetUp() throws Exception {
		}

		public void osgiTearDown() throws Exception {
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
	}

	@Before
	public void setUp() throws Exception {
		activator = new JUnitTestActivator();
	}

	@Test
	public void testStart() throws Exception {
		BundleContext ctx = createMock(BundleContext.class);

		OsgiJUnitService runner = createMock(OsgiJUnitService.class);

		ServiceReference ref = new MockServiceReference();

		expect(ctx.getServiceReference(Runner.class)).andReturn(ref);
		expect(ctx.getService(ref)).andReturn(runner);
		runner.setBundleContext(ctx);
		expectLastCall();
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

	private void setActivatorField(String fieldName, Object value) throws Exception {
		Field field = JUnitTestActivator.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(activator, value);
	}
}
