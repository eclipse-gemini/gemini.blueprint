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

package org.eclipse.gemini.blueprint.context;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.context.support.internal.scope.OsgiBundleScope;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;

/**
 * Tests for OsgiBundleScope.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiBundleScopeTest extends TestCase {

	ObjectFactory objFactory;

	OsgiBundleScope scope;


	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		scope = new OsgiBundleScope();
		OsgiBundleScope.EXTERNAL_BUNDLE.set(null);
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		objFactory = null;
		scope.destroy();
		scope = null;
	}

	public void testLocalBeans() {
		ObjectFactory factory = new ObjectFactory() {

			public Object getObject() throws BeansException {
				return new Object();
			}

		};
		Object foo = scope.get("foo", factory);
		Object foo2 = scope.get("foo", factory);
		assertNotNull(foo);
		assertSame("instance not cached", foo, foo2);

		Object bar = scope.get("bar", factory);
		Object bar2 = scope.get("bar", factory);
		assertNotNull(bar);
		assertSame("instance not cached", bar, bar2);
	}

	public void testIsExternalBundleCalling() {
		assertFalse(OsgiBundleScope.EXTERNAL_BUNDLE.get() != null);
		OsgiBundleScope.EXTERNAL_BUNDLE.set(new Object());
		assertTrue(OsgiBundleScope.EXTERNAL_BUNDLE.get() != null);
	}

	public void testLocalDestructionCallback() {

		final Object[] callbackCalls = new Object[1];

		scope.registerDestructionCallback("foo", new Runnable() {

			public void run() {
				callbackCalls[0] = Boolean.TRUE;
			}
		});

		scope.destroy();
		assertSame(Boolean.TRUE, callbackCalls[0]);
	}

	public void testDestructionCallbackPassedAround() {
		OsgiBundleScope.EXTERNAL_BUNDLE.set(new Object());

		Runnable callback = new Runnable() {

			public void run() {
			}
		};

		scope.registerDestructionCallback("foo", callback);
		assertSame(callback, OsgiBundleScope.EXTERNAL_BUNDLE.get());
	}
}
