/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.service.util.internal.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.ObjectUtils;

/**
 * Simple interceptor for dealing with ThreadContextClassLoader(TCCL)
 * management.
 * 
 * @author Hal Hildebrand
 * @author Costin Leau
 */
public class ServiceTCCLInterceptor implements MethodInterceptor {

	private static final int hashCode = ServiceTCCLInterceptor.class.hashCode() * 13;

	/** classloader to set the TCCL during invocation */
	private final ClassLoader loader;


	/**
	 * Constructs a new <code>OsgiServiceTCCLInterceptor</code> instance.
	 * 
	 * @param loader classloader to use for TCCL during invocation. Can be null.
	 */
	public ServiceTCCLInterceptor(ClassLoader loader) {
		this.loader = loader;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(loader);
			return invocation.proceed();
		}
		finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof ServiceTCCLInterceptor) {
			ServiceTCCLInterceptor oth = (ServiceTCCLInterceptor) other;
			return (ObjectUtils.nullSafeEquals(loader, oth.loader));
		}
		return false;
	}

	public int hashCode() {
		return hashCode;
	}
}