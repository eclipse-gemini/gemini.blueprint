/*
 Copyright (c) 2006, 2010 VMware Inc.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 and Apache License v2.0 which accompanies this distribution.
 The Eclipse Public License is available at
 http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 is available at http://www.opensource.org/licenses/apache2.0.php.
 You may elect to redistribute this code under either of these licenses.

 Contributors:
 VMware Inc.
 */

package org.eclipse.gemini.blueprint.extender.internal;

import junit.framework.TestCase;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEventMulticaster;
import org.eclipse.gemini.blueprint.extender.internal.activator.ListenerServiceActivator;
import org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.BlueprintLoaderListener;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.eclipse.gemini.blueprint.extender.internal.support.TestTaskExecutor;
import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.mock.EntryLookupControllingMockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceRegistration;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.springframework.core.io.ClassPathResource;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Adrian Colyer
 * 
 */
public class BlueprintLoaderListenerTest extends TestCase {
	private BlueprintLoaderListener listener;
    private ExtenderConfiguration configuration = mock(ExtenderConfiguration.class);
    private OsgiBundleApplicationContextEventMulticaster multicaster = mock(OsgiBundleApplicationContextEventMulticaster.class);
	private ListenerServiceActivator listenerServiceActivator;
	private BundleContext context;

	protected void setUp() throws Exception {
		super.setUp();
		doReturn(multicaster).when(this.configuration).getEventMulticaster();
		this.context = mock(BundleContext.class);
		this.listenerServiceActivator = new ListenerServiceActivator(this.configuration);
		this.listener = new BlueprintLoaderListener(this.configuration, listenerServiceActivator);
		this.listenerServiceActivator.start(this.context);
	}

	public void testStart() throws Exception {
		BundleContext context = Mockito.mock(BundleContext.class);
		// look for existing resolved bundles
		when(context.getBundles()).thenReturn(new Bundle[0]);

		// register context service
		when(context.registerService((String[]) null, null, null)).thenReturn(null);

		// create task executor
		EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(null);
		aBundle.setEntryReturnOnNextCallToGetEntry(null);
		when(context.getBundle()).thenReturn(aBundle);

		// listen for bundle events
		context.addBundleListener(null);

		when(context.registerService(isA(String[].class), any(), isA(Dictionary.class))).thenReturn(new MockServiceRegistration<>());

		this.listener.start(context);

		verify(context).registerService(isA(String[].class), any(), isA(Dictionary.class));
	}

	public void ignoredTestTaskExecutor() throws Exception {
		Dictionary<String, String> headers = new Hashtable<>();
		headers.put(Constants.BUNDLE_NAME, "Extender mock bundle");
		final EntryLookupControllingMockBundle aBundle = new EntryLookupControllingMockBundle(headers);
		aBundle.setEntryReturnOnNextCallToGetEntry(new ClassPathResource("META-INF/spring/moved-extender.xml").getURL());

		MockBundleContext ctx = new MockBundleContext() {

			public Bundle getBundle() {
				return aBundle;
			}
		};

		this.listener.start(ctx);

		Dictionary<String, String> hdrs = new Hashtable<>();
		hdrs.put(ConfigUtils.SPRING_CONTEXT_HEADER, "bla bla");
		MockBundle anotherBundle = new MockBundle(hdrs);
		anotherBundle.setBundleId(1);

		BundleEvent event = new BundleEvent(BundleEvent.STARTED, anotherBundle);

		BundleListener listener = ctx.getBundleListeners().iterator().next();

		TestTaskExecutor.called = false;

		listener.bundleChanged(event);
		assertTrue("task executor should have been called if configured properly", TestTaskExecutor.called);
	}

}
