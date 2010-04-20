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

import org.apache.commons.logging.Log;

/**
 * Simple Logger implementation used as fall back to degrade gracefully in case
 * the LogFactory implementation is not configured properly.
 * 
 * @author Costin Leau
 * 
 */
class SimpleLogger implements Log {

	public void debug(Object message) {
		System.out.println(message);
	}

	public void debug(Object message, Throwable th) {
		System.out.println(message);
		th.printStackTrace(System.out);
	}

	public void error(Object message) {
		System.err.println(message);
	}

	public void error(Object message, Throwable th) {
		System.err.println(message);
		th.printStackTrace(System.err);
	}

	public void fatal(Object message) {
		System.err.println(message);
	}

	public void fatal(Object message, Throwable th) {
		System.err.println(message);
		th.printStackTrace(System.err);
	}

	public void info(Object message) {
		System.out.println(message);
	}

	public void info(Object message, Throwable th) {
		System.out.println(message);
		th.printStackTrace(System.out);
	}

	public boolean isDebugEnabled() {
		return true;
	}

	public boolean isErrorEnabled() {
		return true;
	}

	public boolean isFatalEnabled() {
		return true;
	}

	public boolean isInfoEnabled() {
		return true;
	}

	public boolean isTraceEnabled() {
		return true;
	}

	public boolean isWarnEnabled() {
		return true;
	}

	public void trace(Object message) {
		System.out.println(message);
	}

	public void trace(Object message, Throwable th) {
		System.out.println(message);
		th.printStackTrace(System.out);
	}

	public void warn(Object message) {
		System.out.println(message);
	}

	public void warn(Object message, Throwable th) {
		System.out.println(message);
		th.printStackTrace(System.out);
	}
}
