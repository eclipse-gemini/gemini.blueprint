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

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceInvoker;

/**
 * @author Costin Leau
 * 
 */
public class OsgiServiceInvokerTest extends TestCase {

	private ServiceInvoker invoker;

	private Object target;


	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		target = new Object();
		invoker = new ServiceInvoker() {

			protected Object getTarget() {
				return target;
			}

			public void destroy() {
			}
		};
	}

	protected void tearDown() throws Exception {
		target = null;
		invoker = null;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.service.interceptor.ServiceInvoker#invoke(org.aopalliance.intercept.MethodInvocation)}.
	 */
	public void testInvoke() throws Throwable {
		MethodInvocation invocation = new MockMethodInvocation(Object.class.getMethod("hashCode", null));
		Object result = invoker.invoke(invocation);
		assertEquals("different target invoked", new Integer(target.hashCode()), result);
	}

	public void testExceptionUnwrapping() throws Throwable {
		MethodInvocation invocation = new MockMethodInvocation(Integer.class.getMethod("parseInt",
			new Class<?>[] { String.class }), new Object[] { "invalid number" });

		try {
			invoker.invoke(invocation);
			fail("should have thrown exception" + NumberFormatException.class);
		}
		catch (NumberFormatException nfe) {
			// expected
		}
	}
}
