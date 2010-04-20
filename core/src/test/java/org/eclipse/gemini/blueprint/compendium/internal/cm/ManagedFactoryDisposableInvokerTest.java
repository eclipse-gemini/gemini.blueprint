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

package org.eclipse.gemini.blueprint.compendium.internal.cm;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.compendium.internal.cm.ManagedFactoryDisposableInvoker.DestructionCodes;
import org.springframework.beans.factory.DisposableBean;

/**
 * @author Costin Leau
 */
public class ManagedFactoryDisposableInvokerTest extends TestCase {

	enum Action {
		INTERFACE, SPRING_CUSTOM_METHOD, OSGI_CUSTOM_METHOD;
	}


	private List<Action> actions;


	class A implements DisposableBean {

		public void destroy() throws Exception {
			actions.add(Action.INTERFACE);
		}

	}

	class B extends A {

		public void stop() {
			actions.add(Action.SPRING_CUSTOM_METHOD);
		}
	}

	class C extends B {

		public void stop(int code) {
			actions.add(Action.OSGI_CUSTOM_METHOD);
		}
	}

	class D extends A {

		public void stop(int code) {
			actions.add(Action.OSGI_CUSTOM_METHOD);
		}
	}

	class E {

		public void stop() {
			actions.add(Action.SPRING_CUSTOM_METHOD);
		}

		public void stop(int code) {
			actions.add(Action.OSGI_CUSTOM_METHOD);
		}
	}


	private ManagedFactoryDisposableInvoker invoker;


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		actions = new ArrayList<Action>();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		invoker = null;
		actions = null;
	}

	public void testDefinitionWithCustomMethods() throws Exception {
		C c = new C();
		invoker = new ManagedFactoryDisposableInvoker(C.class, "stop");
		assertTrue(actions.isEmpty());
		doInvoke(c);
		assertEquals(3, actions.size());
		assertSame(Action.INTERFACE, actions.get(0));
		assertSame(Action.SPRING_CUSTOM_METHOD, actions.get(1));
		assertSame(Action.OSGI_CUSTOM_METHOD, actions.get(2));
	}

	public void testInterfaceAndSpringMethod() throws Exception {
		B b = new B();
		invoker = new ManagedFactoryDisposableInvoker(B.class, "stop");
		assertTrue(actions.isEmpty());
		doInvoke(b);
		assertEquals(2, actions.size());
		assertSame(Action.INTERFACE, actions.get(0));
		assertSame(Action.SPRING_CUSTOM_METHOD, actions.get(1));
	}

	public void testInterfaceAndOsgiMethod() throws Exception {
		D d = new D();
		invoker = new ManagedFactoryDisposableInvoker(D.class, "stop");
		assertTrue(actions.isEmpty());
		doInvoke(d);
		assertEquals(2, actions.size());
		assertSame(Action.INTERFACE, actions.get(0));
		assertSame(Action.OSGI_CUSTOM_METHOD, actions.get(1));
	}

	public void testSpringAndOsgiMethod() throws Exception {
		E e = new E();
		invoker = new ManagedFactoryDisposableInvoker(E.class, "stop");
		assertTrue(actions.isEmpty());
		doInvoke(e);
		assertEquals(2, actions.size());
		assertSame(Action.SPRING_CUSTOM_METHOD, actions.get(0));
		assertSame(Action.OSGI_CUSTOM_METHOD, actions.get(1));
	}

	public void testNoMethod() throws Exception {
		invoker = new ManagedFactoryDisposableInvoker(Object.class, "stop");
		doInvoke(new Object());
		assertEquals(0, actions.size());
	}

	private void doInvoke(Object target) {
		invoker.destroy("test", target, DestructionCodes.BUNDLE_STOPPING);
	}
}