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

package org.eclipse.gemini.blueprint;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

/**
 * Util classes for test cases.
 * 
 * @author Costin Leau
 * 
 */
public abstract class TestUtils {

	public static Object getFieldValue(final Object object, final String fieldName) {
		final Object[] fld = new Object[1];
		ReflectionUtils.doWithFields(object.getClass(), new FieldCallback() {

			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				field.setAccessible(true);
				fld[0] = field.get(object);
			}

		}, new FieldFilter() {

			public boolean matches(Field field) {
				return fld[0] == null && fieldName.equals(field.getName());
			}

		});

		return fld[0];
	}

	private static Object invokeMethod(final Object target, final Class<?> targetClass, String methodName, Object[] args) {
		Class<?>[] types = null;
		if (ObjectUtils.isEmpty(args)) {
			types = new Class[0];
		}
		else {
			types = new Class[args.length];
			for (int objectIndex = 0; objectIndex < args.length; objectIndex++) {
				types[objectIndex] = args[objectIndex].getClass();
			}
		}

		Method method = ReflectionUtils.findMethod(targetClass, methodName, types);
		ReflectionUtils.makeAccessible(method);
		return ReflectionUtils.invokeMethod(method, target, args);
	}

	public static Object invokeMethod(final Object target, String methodName, Object[] args) {
		return invokeMethod(target, target.getClass(), methodName, args);
	}

	public static Object invokeStaticMethod(final Class<?> target, String methodName, Object[] args) {
		return invokeMethod(null, target, methodName, args);
	}
}
