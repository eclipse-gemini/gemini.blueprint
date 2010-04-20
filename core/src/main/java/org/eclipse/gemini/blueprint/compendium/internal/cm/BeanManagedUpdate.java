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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * BeanManaged update class. Performs the update call using a custom method on the given object.
 * 
 * @author Costin Leau
 */
class BeanManagedUpdate implements UpdateCallback {

	private final String methodName;
	// class cache = keeps track of method adapters for each given class
	// the cache becomes useful when dealing with FactoryBean which can returns
	// different class types on each invocation
	private final Map<Class<?>, WeakReference<UpdateMethodAdapter>> classCache =
			new WeakHashMap<Class<?>, WeakReference<UpdateMethodAdapter>>(2);

	public BeanManagedUpdate(String methodName) {
		this.methodName = methodName;
	}

	public void update(Object instance, Map properties) {
		getUpdateMethod(instance).invoke(instance, properties);
	}

	/**
	 * Returns a (lazily created) method adapter that invokes a predefined method on the given instance.
	 * 
	 * @param instance object instance
	 * @return method update method adapter
	 */
	private UpdateMethodAdapter getUpdateMethod(Object instance) {
		UpdateMethodAdapter adapter;
		Class<?> type = instance.getClass();

		WeakReference<UpdateMethodAdapter> adapterReference = classCache.get(type);
		if (adapterReference != null) {
			adapter = adapterReference.get();
			if (adapter != null)
				return adapter;
		}
		adapter = new UpdateMethodAdapter(methodName, type);
		classCache.put(type, new WeakReference<UpdateMethodAdapter>(adapter));
		return adapter;
	}
}