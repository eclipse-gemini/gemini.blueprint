/******************************************************************************
 * Copyright (c) 2013 Olaf Otto
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *   Olaf Otto
 *****************************************************************************/
package org.eclipse.gemini.blueprint.extender.internal.activator;

import org.eclipse.gemini.blueprint.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.extender.OsgiApplicationContextCreator;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.eclipse.gemini.blueprint.extender.support.ApplicationContextConfiguration;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import static java.lang.Thread.yield;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.*;

/**
 * @author Olaf Otto
 */
@RunWith(MockitoJUnitRunner.class)
public class LifecycleManagerTest {
    @Mock
    private DelegatedExecutionOsgiBundleApplicationContext context;
    @Mock
    private OsgiApplicationContextCreator contextCreator;
    @Mock
    private OsgiContextProcessor osgiContextProcessor;
    @Mock
    private VersionMatcher versionMatcher;
    @Mock
    private ApplicationContextConfigurationFactory factory;
    @Mock
    private ApplicationContextConfiguration contextConfiguration;
    @Mock
    private ExtenderConfiguration configuration;

    private BundleContext bundleContext = new MockBundleContext();

    private LifecycleManager testee;

    @Before
    public void setUp() throws Exception {
        this.testee = new LifecycleManager(this.configuration, this.versionMatcher, this.factory, this.contextCreator, this.osgiContextProcessor, null, this.bundleContext);

        doReturn("JUnit test context").when(this.context).getDisplayName();
        doReturn(this.context).when(this.contextCreator).createApplicationContext(eq(this.bundleContext));
        doReturn(true).when(this.versionMatcher).matchVersion(isA(Bundle.class));
        doReturn(this.contextConfiguration).when(this.factory).createConfiguration(isA(Bundle.class));
        doReturn(this.bundleContext.getBundle()).when(this.context).getBundle();
        doReturn(SECONDS.toMillis(2)).when(this.configuration).getShutdownWaitTime();
    }

    @Test
    public void testSuccessfulAsynchronousShutdown() throws Exception {
        withAsynchronousShutdownDisabled();

        shutdownContext();

        verifyContextIsClosed();
        verifyOsgiContextProcessorInteractions();
    }

    @Test
    public void testSuccessfulSynchronousShutdown() throws Exception {
        withAsynchronousShutdownEnabled();

        shutdownContext();

        verifyContextIsClosed();
        verifyOsgiContextProcessorInteractions();
    }

    @Test
    public void testFailingAsynchronousShutdown() throws Exception {
        withAsynchronousShutdownEnabled();
        withFailingApplicationContextClose();

        shutdownContext();
        Thread.yield();

        verifyContextIsClosed();
        verifyOsgiContextProcessorInteractions();
    }

    @Test
    public void testFailingSynchronousShutdown() {
        withAsynchronousShutdownDisabled();
        withFailingApplicationContextClose();

        shutdownContext();

        verifyContextIsClosed();
        verifyOsgiContextProcessorInteractions();
    }

    @Test
    public void testSuccessfulSynchronousDestruction() throws Exception {
        withAsynchronousShutdownDisabled();
        addContextToLifecycleManager();

        destroy();

        verifyContextIsClosed();
        verifyOsgiContextProcessorInteractions();
    }

    @Test
    public void testSuccessfulAsynchronousDestruction() throws Exception {
        withAsynchronousShutdownEnabled();
        addContextToLifecycleManager();

        destroy();
        Thread.yield();

        verifyContextIsClosed();
        verifyOsgiContextProcessorInteractions();
    }

    @Test
    public void testLifecycleManagerIgnoresBundlesWithoutContexttForContextCreation() throws Exception {
        this.testee.maybeCreateApplicationContextFor(createBundleWithoutBundleContext());
        verifyContextCreationIsNotAttempted();
    }

    private void verifyContextCreationIsNotAttempted() throws Exception {
        verify(this.contextCreator, never()).createApplicationContext(Mockito.<BundleContext>any());
    }

    private Bundle createBundleWithoutBundleContext() {
        Bundle bundle = mock(Bundle.class);
        Version version = new Version(1, 0, 0);
        doReturn(version).when(bundle).getVersion();
        return bundle;
    }


    private void addContextToLifecycleManager() throws Exception {
        this.testee.maybeCreateApplicationContextFor(this.bundleContext.getBundle());
    }

    private void destroy() {
        this.testee.destroy();
    }

    private void withFailingApplicationContextClose() {
        doThrow(new RuntimeException("THIS IS AN EXPECTED TEST EXCEPTION")).when(this.context).close();
    }

    private void verifyContextIsClosed() {
        verify(this.context).close();
    }

    private void verifyOsgiContextProcessorInteractions() {
        verify(this.osgiContextProcessor).preProcessClose(eq(this.context));
        verify(this.osgiContextProcessor).postProcessClose(eq(this.context));
    }

    private void shutdownContext() {
        this.testee.maybeClose(this.context);
    }

    private void withAsynchronousShutdownDisabled() {
        doReturn(false).when(this.configuration).shouldShutdownAsynchronously();
    }

    private void withAsynchronousShutdownEnabled() {
        doReturn(true).when(this.configuration).shouldShutdownAsynchronously();
    }
}
