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

package org.eclipse.gemini.blueprint.blueprint;

import java.lang.reflect.Constructor;

import org.junit.Test;

public class ReflectionTest {

	static class Foo {
		public Foo(boolean bool) {
			System.out.println("boolean " + bool);
		}

		public Foo(Boolean bool) {
			System.out.println("Boolean " + bool);
		}
	};

	@Test
	public void testPrimitive() throws Exception {
		Constructor[] constructors = Foo.class.getDeclaredConstructors();
		for (Constructor constructor : constructors) {
			Class[] parameterTypes = constructor.getParameterTypes();
			for (Class class1 : parameterTypes) {
				System.out.println(class1.getName());
			}
		}

		boolean obj = true;
		
		Foo foo = new Foo(Boolean.TRUE);
		foo = new Foo(obj);
		foo = new Foo((Boolean) true);
	}
}
