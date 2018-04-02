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
import org.eclipse.gemini.blueprint.blueprint.container.SpringBlueprintContainer;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEventMulticaster;
import org.eclipse.gemini.blueprint.extender.internal.activator.ListenerServiceActivator;
import org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.BlueprintLoaderListener;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.eclipse.gemini.blueprint.extender.internal.support.TestTaskExecutor;
import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.mock.ArrayEnumerator;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Adrian Colyer
 */
public class BlueprintLoaderListenerTest extends TestCase {
    private ExtenderConfiguration extenderConfiguration = mock(ExtenderConfiguration.class);
    private OsgiBundleApplicationContextEventMulticaster multicaster = mock(OsgiBundleApplicationContextEventMulticaster.class);
    private BundleContext context = mock(BundleContext.class);

    private BlueprintLoaderListener testee;

    protected void setUp() throws Exception {
        super.setUp();
        doReturn(multicaster).when(this.extenderConfiguration).getEventMulticaster();
        doReturn((TaskExecutor) Runnable::run).when(this.extenderConfiguration).getTaskExecutor();

        ListenerServiceActivator listenerServiceActivator = new ListenerServiceActivator(this.extenderConfiguration);
        listenerServiceActivator.start(this.context);

        this.testee = new BlueprintLoaderListener(this.extenderConfiguration, listenerServiceActivator);
    }

    public void testStart() throws Exception {
        when(context.registerService((String[]) null, null, null)).thenReturn(null);

        Bundle bundle = new MockBundle() {
            @Override
            public Enumeration findEntries(String path, String filePattern, boolean recurse) {
                if (path.endsWith("/blueprint/extender/internal/")) {
                    // Retrieval of all XML files in a discovered directory: return test XML context
                    return new ArrayEnumerator<>(getClass().getResource("/org/eclipse/gemini/blueprint/extender/internal/BlueprintLoaderListenerTest.xml"));
                } else {
                    // Directory listing: return directory containing XML file
                    return new ArrayEnumerator<>(getClass().getResource("/org/eclipse/gemini/blueprint/extender/internal/"));
                }
            }

            @Override
            public BundleContext getBundleContext() {
                return context;
            }
        };

        when(context.getBundle()).thenReturn(bundle);
        when(context.getBundles()).thenReturn(new Bundle[]{bundle});
        this.testee.start(context);

        // Verify a blueprint container is published for the resulting application context.
        verify(context).registerService(isA(String[].class), isA(SpringBlueprintContainer.class), isA(Dictionary.class));
    }

    public void ignoredTestTaskExecutor() throws Exception {
        Dictionary<String, String> headers = new Hashtable<>();
        headers.put(Constants.BUNDLE_NAME, "Extender mock bundle");
        final Bundle aBundle = mock(Bundle.class);
        doReturn(new ClassPathResource("META-INF/spring/moved-extender.xml").getURL()).when(aBundle).getEntry(anyString());

        MockBundleContext ctx = new MockBundleContext() {

            public Bundle getBundle() {
                return aBundle;
            }
        };

        this.testee.start(ctx);

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
