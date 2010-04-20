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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;

/**
 * 
 * @author Costin Leau
 * 
 */
public class SimpleLoggerTest extends TestCase {

	class AssertivePrintStream extends PrintStream {

		public AssertivePrintStream(OutputStream out, boolean autoFlush, String encoding)
				throws UnsupportedEncodingException {
			super(out, autoFlush, encoding);
		}

		public AssertivePrintStream(OutputStream out, boolean autoFlush) {
			super(out, autoFlush);
		}

		public AssertivePrintStream(OutputStream out) {
			super(out);
		}

		public void println(Object x) {
			loggingCalled(this, x);
		}
	}

	class NullOutputStream extends OutputStream {

		public void write(int b) throws IOException {
			// do nothing
		}
	}

	class MyThrowable extends Exception {

		public void printStackTrace(PrintStream s) {
			assertSame("the right stream [" + shouldBeCalled + "] is not called", shouldBeCalled, s);
			super.printStackTrace(s);
		}
	}


	private PrintStream outStream, errStream;
	private PrintStream shouldBeCalled, shouldNotBeCalled;
	private Log simpleLogger;
	private Object object;
	private Throwable throwable;


	protected void setUp() throws Exception {
		outStream = new AssertivePrintStream(new NullOutputStream());
		errStream = new AssertivePrintStream(new NullOutputStream());
		System.setErr(errStream);
		System.setOut(outStream);

		simpleLogger = new SimpleLogger();
		object = new Object();
		throwable = new MyThrowable();
	}

	protected void tearDown() throws Exception {
		System.setErr(null);
		System.setOut(null);
		simpleLogger = null;
		object = null;
		throwable = null;
	}

	private void loggingCalled(AssertivePrintStream assertivePrintStream, Object x) {
		assertSame("the right stream [" + shouldBeCalled + "] is not called", shouldBeCalled, assertivePrintStream);
		assertNotSame("the wrong stream [" + shouldBeCalled + "] is called", shouldNotBeCalled, assertivePrintStream);
	}

	public void testDebugObject() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.debug(object);
	}

	public void testDebugObjectThrowable() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.debug(object, throwable);
	}

	public void testErrorObject() {
		shouldBeCalled = errStream;
		shouldNotBeCalled = outStream;
		simpleLogger.error(object);
	}

	public void testErrorObjectThrowable() {
		shouldBeCalled = errStream;
		shouldNotBeCalled = outStream;
		simpleLogger.error(object, throwable);
	}

	public void testFatalObject() {
		shouldBeCalled = errStream;
		shouldNotBeCalled = outStream;
		simpleLogger.fatal(object);
	}

	public void testFatalObjectThrowable() {
		shouldBeCalled = errStream;
		shouldNotBeCalled = outStream;
		simpleLogger.fatal(object, throwable);
	}

	public void testInfoObject() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.info(object);
	}

	public void testInfoObjectThrowable() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.info(object, throwable);
	}

	public void testIsDebugEnabled() {
		assertTrue(simpleLogger.isDebugEnabled());
	}

	public void testIsErrorEnabled() {
		assertTrue(simpleLogger.isErrorEnabled());
	}

	public void testIsFatalEnabled() {
		assertTrue(simpleLogger.isFatalEnabled());
	}

	public void testIsInfoEnabled() {
		assertTrue(simpleLogger.isInfoEnabled());
	}

	public void testIsTraceEnabled() {
		assertTrue(simpleLogger.isTraceEnabled());
	}

	public void testIsWarnEnabled() {
		assertTrue(simpleLogger.isWarnEnabled());
	}

	public void testTraceObject() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.trace(object);
	}

	public void testTraceObjectThrowable() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.info(object, throwable);
	}

	public void testWarnObject() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.warn(object);
	}

	public void testWarnObjectThrowable() {
		shouldBeCalled = outStream;
		shouldNotBeCalled = errStream;
		simpleLogger.warn(object, throwable);
	}
}
