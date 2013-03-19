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

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;

import static java.lang.Thread.yield;

/**
 * @author Olaf Otto
 */
public class LifecycleManagerTest extends TestCase {
    private MockControl contextControl = MockControl.createControl(ConfigurableOsgiBundleApplicationContext.class);
    private ConfigurableOsgiBundleApplicationContext context = (ConfigurableOsgiBundleApplicationContext) contextControl.getMock();

    private MockControl osgiContextProcessorControl = MockControl.createControl(OsgiContextProcessor.class);
    private OsgiContextProcessor osgiContextProcessor = (OsgiContextProcessor) osgiContextProcessorControl.getMock();

    private boolean shouldShutdownAsynchronously;

    // Cannot mock a class with easymock 1.2
    ExtenderConfiguration configuration = new ExtenderConfiguration() {
        @Override
        public boolean shouldShutdownAsynchronously() {
            return shouldShutdownAsynchronously;
        }

        @Override
        public long getShutdownWaitTime() {
            return 0L;
        }
    };

    private LifecycleManager testee = new LifecycleManager(this.configuration, null, null, null, this.osgiContextProcessor, null, null);

    public void testSuccessfulAsynchronousShutdown() throws Exception {
        withAsynchronousShutdownDisabled();
        withSuccessfulContextClose();
        withPreAndPostProcessing();

        shutdownContext();

        verifyContextIsClosed();
        verifyOsgiContextProcessorInteractions();
    }

    public void testSuccessfulSynchronousShutdown() throws Exception {
        withAsynchronousShutdownEnabled();
        withSuccessfulContextClose();
        withPreAndPostProcessing();

        shutdownContext();

        verifyContextIsClosed();
        verifyOsgiContextProcessorInteractions();
    }

    public void testFailingAsynchronousShutdown() throws Exception {
        withAsynchronousShutdownEnabled();
        withFailingApplicationContextClose();
        withPreAndPostProcessing();

        shutdownContext();
        yield();

        verifyContextIsClosed();
        verifyOsgiContextProcessorInteractions();
    }

    public void testFailingSynchronousShutdown() {
        withAsynchronousShutdownDisabled();
        withFailingApplicationContextClose();
        withPreAndPostProcessing();

        shutdownContext();

        verifyContextIsClosed();
        verifyOsgiContextProcessorInteractions();
    }

    private void withFailingApplicationContextClose() {
        this.context.getDisplayName();
        this.contextControl.setReturnValue("Display name");
        this.context.close();
        this.contextControl.setThrowable(new RuntimeException("THIS IS AN EXPECTED TEST EXCEPTION"));
    }

    private void verifyContextIsClosed() {
        this.contextControl.verify();
    }

    private void verifyOsgiContextProcessorInteractions() {
        this.osgiContextProcessorControl.verify();
    }

    private void withPreAndPostProcessing() {
        this.osgiContextProcessor.preProcessClose(this.context);
        this.osgiContextProcessor.postProcessClose(this.context);
    }

    private void withSuccessfulContextClose() {
        this.context.close();
        this.context.getDisplayName();
        this.contextControl.setDefaultReturnValue("Nothing");
    }

    private void shutdownContext() {
        this.contextControl.replay();
        this.osgiContextProcessorControl.replay();

        this.testee.maybeClose(this.context);
    }

    private void withAsynchronousShutdownDisabled() {
        this.shouldShutdownAsynchronously = false;
    }

    private void withAsynchronousShutdownEnabled() {
        this.shouldShutdownAsynchronously = true;
    }
}
