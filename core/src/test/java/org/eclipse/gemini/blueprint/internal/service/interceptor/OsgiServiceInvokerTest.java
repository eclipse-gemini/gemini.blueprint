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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceInvoker;

/**
 * @author Costin Leau
 * 
 */
public class OsgiServiceInvokerTest {

	private ServiceInvoker invoker;

	private Object target;

	@Before
	public void setup() throws Exception {
		target = new Object();
		invoker = new ServiceInvoker() {

			protected Object getTarget() {
				return target;
			}

			public void destroy() {
			}
		};
	}

	@After
	public void tearDown() throws Exception {
		target = null;
		invoker = null;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gemini.blueprint.service.interceptor.ServiceInvoker#invoke(org.aopalliance.intercept.MethodInvocation)}.
	 */
	@Test
	public void testInvoke() throws Throwable {
		MethodInvocation invocation = new MockMethodInvocation(Object.class.getMethod("hashCode", null));
		Object result = invoker.invoke(invocation);
		assertEquals("different target invoked", Integer.valueOf(target.hashCode()), result);
	}

	@Test
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
