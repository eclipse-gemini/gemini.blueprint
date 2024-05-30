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

package org.eclipse.gemini.blueprint.extender.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.gemini.blueprint.extender.internal.activator.ContextLoaderListener;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.eclipse.gemini.blueprint.extender.internal.support.TestTaskExecutor;
import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.mock.EntryLookupControllingMockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceRegistration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Adrian Colyer
 * 
 */
public abstract class ContextLoaderListenerTest {
	private ContextLoaderListener listener;
    // TODO: mock & train once there are any applications of this base class.
    private ExtenderConfiguration configuration;

    @Before
    public void setup() throws Exception {
		this.listener = new ContextLoaderListener(this.configuration);
	}

    @Test
	public void testStart() throws Exception {
		BundleContext context = createMock(BundleContext.class);
		// platform determination

		// extracting bundle id from bundle
		expect(context.getBundle()).andReturn(new MockBundle());

		// look for existing resolved bundles
		expect(context.getBundles()).andReturn(new Bundle[0]).times(2);

		// register context service
		expect(context.registerService((String[]) null, null, null)).andReturn(null).atLeastOnce();

		// create task executor
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(null);
		aBundle.setEntryReturnOnNextCallToGetEntry(null);
		expect(context.getBundle()).andReturn(aBundle).atLeastOnce();

		// listen for bundle events
		context.addBundleListener(null);
        expectLastCall().times(2);

		expect(context.registerService(new String[0], null, new Hashtable<String, Object>())).andReturn(new MockServiceRegistration()).atLeastOnce();
		replay(context);

		this.listener.start(context);
		verify(context);
	}

    @Test
    @Ignore
	public void testTaskExecutor() throws Exception {
		Dictionary headers = new Hashtable();
		headers.put(Constants.BUNDLE_NAME, "Extender mock bundle");
		final EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setEntryReturnOnNextCallToGetEntry(new ClassPathResource("META-INF/spring/moved-extender.xml").getURL());

		MockBundleContext ctx = new MockBundleContext() {

			public Bundle getBundle() {
				return aBundle;
			}
		};

		this.listener.start(ctx);

		Dictionary hdrs = new Hashtable();
		hdrs.put(ConfigUtils.SPRING_CONTEXT_HEADER, "bla bla");
		MockBundle anotherBundle = new MockBundle(hdrs);
		anotherBundle.setBundleId(1);

		BundleEvent event = new BundleEvent(BundleEvent.STARTED, anotherBundle);

		BundleListener listener = (BundleListener) ctx.getBundleListeners().iterator().next();

		TestTaskExecutor.called = false;

		listener.bundleChanged(event);
		assertTrue("task executor should have been called if configured properly", TestTaskExecutor.called);
	}

}
