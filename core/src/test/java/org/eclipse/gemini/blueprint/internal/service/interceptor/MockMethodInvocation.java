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

package org.eclipse.gemini.blueprint.internal.service.interceptor;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Dummy MethodInvocation.
 * 
 * @author Costin Leau
 * 
 */
public class MockMethodInvocation implements MethodInvocation {

	private final Method m;

	private final Object[] args;

	public MockMethodInvocation(Method m) {
		this(m, null);
	}

	public MockMethodInvocation(Method m, Object[] args) {
		this.m = m;
		this.args = (args == null ? new Object[0] : args);
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInvocation#getMethod()
	 */
	public Method getMethod() {
		return m;
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.Invocation#getArguments()
	 */
	public Object[] getArguments() {
		return args;
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.Joinpoint#getStaticPart()
	 */
	public AccessibleObject getStaticPart() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.Joinpoint#getThis()
	 */
	public Object getThis() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.Joinpoint#proceed()
	 */
	public Object proceed() throws Throwable {
		return null;
	}

}
