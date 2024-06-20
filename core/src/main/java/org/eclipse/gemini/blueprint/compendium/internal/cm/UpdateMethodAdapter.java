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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.util.internal.ReflectionUtils;
import org.springframework.util.Assert;

/**
 * Adapter class that takes care of detecting and invoking a custom method for managed-service beans.
 * 
 * The supported method formats are:
 * 
 * <pre> public void anyMethodName(Map properties) public void anyMethodName(Map&lt;String,?&gt; properties); // for
 * Java 5 public void anyMethodName(Dictionary properties); </pre>
 * 
 * @author Costin Leau
 * 
 */
class UpdateMethodAdapter {

	private static final Log log = LogFactory.getLog(UpdateMethodAdapter.class);

	/**
	 * Determines the update method.
	 * 
	 * @param target
	 * @param methodName
	 * @return
	 */
	static Map determineUpdateMethod(final Class<?> target, final String methodName) {
		Assert.notNull(target, "target is required");
		Assert.notNull(methodName, "methodName is required");

		final Map methods = new LinkedHashMap(2);
		final boolean trace = log.isTraceEnabled();

		org.springframework.util.ReflectionUtils.doWithMethods(target,
				new org.springframework.util.ReflectionUtils.MethodCallback() {

					public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
						// consider filtering bridge non-void and non-public methods as well
						if (!method.isBridge() && Modifier.isPublic(method.getModifiers())
								&& (void.class.equals(method.getReturnType())) && methodName.equals(method.getName())) {
							// check the argument types
							Class<?>[] args = method.getParameterTypes();

							// Properties can be passed as Map or Dictionary
							if (args != null && args.length == 1) {
								Class<?> propertiesType = args[0];
								if (propertiesType.isAssignableFrom(Map.class)
										|| propertiesType.isAssignableFrom(Dictionary.class)) {

									if (trace)
										log.trace("Discovered custom method [" + method.toString() + "] on " + target);
									// see if there was a method already found
									Method m = (Method) methods.get(propertiesType);

									if (m != null) {
										if (trace)
											log.trace("Type " + propertiesType + " already has an associated method ["
													+ m.toString() + "];ignoring " + method);
									} else
										methods.put(propertiesType, method);
								}
							}
						}
					}
				});

		return methods;
	}

	static void invokeCustomMethods(Object target, Map methods, Map properties) {
		if (methods != null && !methods.isEmpty()) {
			boolean trace = log.isTraceEnabled();

			Object[] args = new Object[] { properties };
			for (Iterator iterator = methods.values().iterator(); iterator.hasNext();) {
				Method method = (Method) iterator.next();
				if (trace)
					log.trace("Invoking listener custom method " + method);

				try {
					ReflectionUtils.invokeMethod(method, target, args);
				}
				// make sure to log exceptions and continue with the
				// rest of the methods
				catch (Exception ex) {
					Exception cause = ReflectionUtils.getInvocationException(ex);
					log.warn("Custom method [" + method + "] threw exception when passing properties [" + properties
							+ "]", cause);
				}
			}
		}
	}

	private final Map methods;

	/**
	 * Constructs a new <code>UpdateMethodAdapter</code> instance.
	 * 
	 * @param methodName
	 * @param type
	 */
	UpdateMethodAdapter(String methodName, Class<?> type) {
		this.methods = determineUpdateMethod(type, methodName);
	}

	void invoke(Object instance, Map properties) {
		invokeCustomMethods(instance, methods, properties);
	}
}