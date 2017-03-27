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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Costin Leau
 * @author Olaf Otto
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleLoggerTest {
    @Mock
    private PrintStream out;
    @Mock
    private PrintStream err;
    @Mock
    private Throwable exception;
    @Mock
    private Throwable throwable;

    private Object object = new Object();

    @InjectMocks
    private SimpleLogger testee;

    @Test
    public void testDebugObject() {
        testee.debug(object);
        assertMessageSendViaSystemOut();
        assertMessageNotSendViaSystemErr();
    }

    @Test
    public void testDebugObjectThrowable() {
        testee.debug(object, throwable);
        assertMessageSendViaSystemOut();
        assertMessageNotSendViaSystemErr();
    }

    @Test
    public void testErrorObject() {
        testee.error(object);
        assertMessageNotSendViaSystemOut();
        assertMessageSendViaSystemErr();
    }

    @Test
    public void testErrorObjectThrowable() {
        testee.error(object, throwable);
        assertMessageSendViaSystemErr();
        assertThrowablePrintedToSystemErr();
        assertMessageNotSendViaSystemOut();
    }

    @Test
    public void testFatalObject() {
        testee.fatal(object);
        assertMessageSendViaSystemErr();
        assertMessageNotSendViaSystemOut();
    }

    @Test
    public void testFatalObjectThrowable() {
        testee.fatal(object, throwable);
        assertMessageSendViaSystemErr();
        assertThrowablePrintedToSystemErr();
        assertMessageNotSendViaSystemOut();
    }

    @Test
    public void testInfoObject() {
        testee.info(object);
        assertMessageSendViaSystemOut();
        assertMessageNotSendViaSystemErr();
    }

    @Test
    public void testInfoObjectThrowable() {
        testee.info(object, throwable);
        assertMessageSendViaSystemOut();
        assertMessageNotSendViaSystemErr();
        assertThrowablePrintedToSystemOut();
    }

    @Test
    public void testIsDebugEnabled() {
        assertThat(testee.isDebugEnabled()).isTrue();
    }

    @Test
    public void testIsErrorEnabled() {
        assertThat(testee.isErrorEnabled()).isTrue();
    }

    @Test
    public void testIsFatalEnabled() {
        assertThat(testee.isFatalEnabled()).isTrue();
    }

    @Test
    public void testIsInfoEnabled() {
        assertThat(testee.isInfoEnabled()).isTrue();
    }

    @Test
    public void testIsTraceEnabled() {
        assertThat(testee.isTraceEnabled()).isTrue();
    }

    @Test
    public void testIsWarnEnabled() {
        assertThat(testee.isWarnEnabled()).isTrue();
    }

    @Test
    public void testTraceObject() {
        testee.trace(object);
        assertMessageSendViaSystemOut();
        assertMessageNotSendViaSystemErr();
    }

    @Test
    public void testTraceObjectThrowable() {
        testee.info(object, throwable);
        assertMessageSendViaSystemOut();
        assertThrowablePrintedToSystemOut();
        assertMessageNotSendViaSystemErr();
    }

    @Test
    public void testWarnObject() {
        testee.warn(object);
        assertMessageSendViaSystemOut();
        assertMessageNotSendViaSystemErr();
    }

    @Test
    public void testWarnObjectThrowable() {
        testee.warn(object, throwable);

        assertMessageSendViaSystemOut();
        assertThrowablePrintedToSystemOut();
        assertMessageNotSendViaSystemErr();
    }

    private void assertMessageNotSendViaSystemErr() {
        verify(this.err, never()).println(eq(this.object));
    }

    private void assertMessageSendViaSystemOut() {
        verify(this.out).println(eq(this.object));
    }

    private void assertMessageSendViaSystemErr() {
        verify(this.out, never()).println(eq(this.object));
    }

    private void assertMessageNotSendViaSystemOut() {
        verify(this.err).println(eq(this.object));
    }

    private void assertThrowablePrintedToSystemErr() {
        verify(this.throwable).printStackTrace(eq(this.err));
    }

    private void assertThrowablePrintedToSystemOut() {
        verify(this.throwable).printStackTrace(eq(this.out));
    }
}
