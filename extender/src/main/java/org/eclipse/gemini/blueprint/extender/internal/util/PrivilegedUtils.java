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

package org.eclipse.gemini.blueprint.extender.internal.util;

/**
 * Utility class for commons actions used within PrivilegedBlocks.
 * 
 * @author Costin Leau
 * 
 */
public abstract class PrivilegedUtils {

	public interface UnprivilegedThrowableExecution<T> {

		public T run() throws Throwable;
	}

	public interface UnprivilegedExecution<T> {

		public T run();
	}

	public static ClassLoader getTCCL() {
	    return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * Temporarily changes the TCCL to the given one for the duration of the
	 * given execution. All actions except the execution are executed with
	 * privileged access.
	 * 
	 * Consider checking if there is a security manager in place before calling
	 * this method.
	 * 
	 * @param customClassLoader
	 * @param execution
	 * @return
	 */
	public static <T> T executeWithCustomTCCL(final ClassLoader customClassLoader,
			final UnprivilegedExecution<T> execution) {
		final Thread currentThread = Thread.currentThread();
		final ClassLoader oldTCCL = getTCCL();

		try {
		    currentThread.setContextClassLoader(customClassLoader);
			return execution.run();
		}
		finally {
		    currentThread.setContextClassLoader(oldTCCL);
		}
	}

	/**
	 * Temporarily changes the TCCL to the given one for the duration of the
	 * given execution. All actions except the execution are executed with
	 * privileged access.
	 * 
	 * Consider checking if there is a security manager in place before calling
	 * this method.
	 * 
	 * @param customClassLoader
	 * @param execution
	 * @return
	 * @throws Throwable
	 */
	public static <T> T executeWithCustomTCCL(final ClassLoader customClassLoader,
			final UnprivilegedThrowableExecution<T> execution) throws Throwable {
		final Thread currentThread = Thread.currentThread();
		final ClassLoader oldTCCL = getTCCL();

		try {
		    currentThread.setContextClassLoader(customClassLoader);
			return execution.run();
		}
		catch (Exception e) {
			throw e.getCause();
		}
		finally {
		    currentThread.setContextClassLoader(oldTCCL);
		}
	}
}