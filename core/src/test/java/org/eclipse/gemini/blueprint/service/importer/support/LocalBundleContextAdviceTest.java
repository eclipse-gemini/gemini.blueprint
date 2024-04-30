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

package org.eclipse.gemini.blueprint.service.importer.support;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Method;

import org.eclipse.gemini.blueprint.internal.service.interceptor.MockMethodInvocation;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.springframework.util.ReflectionUtils;

public class LocalBundleContextAdviceTest {

	private MockMethodInvocation invocation;

	private LocalBundleContextAdvice interceptor;

	private BundleContext context;

	@Before
	public void setup() throws Exception {
		Method m = ReflectionUtils.findMethod(Object.class, "hashCode");
		context = new MockBundleContext();
		interceptor = new LocalBundleContextAdvice(context);
		
		invocation = new MockMethodInvocation(m) {
			public Object proceed() throws Throwable {
				assertSame("bundle context not set", context, LocalBundleContext.getInvokerBundleContext());
				return null;
			}
		};

	}

	@After
	public void tearDown() throws Exception {
		invocation = null;
		interceptor = null;
		context = null;
	}

	@Test
	public void testInvoke() throws Throwable {
		assertNull(LocalBundleContext.getInvokerBundleContext());
		interceptor.invoke(invocation);
	}

}
