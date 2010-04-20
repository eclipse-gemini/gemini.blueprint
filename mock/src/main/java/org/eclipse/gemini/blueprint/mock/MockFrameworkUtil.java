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

package org.eclipse.gemini.blueprint.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.osgi.framework.Filter;

/**
 * FrameworkUtil-like class that tries to create a somewhat valid filter.
 * 
 * Filters objects can be created without an actual OSGi platform running
 * however, the default OSGi implementation delegates the creation to the
 * package indicated by "org.osgi.vendor.framework" property.
 * 
 * In its current implementation, this class requires one of Equinox,
 * Knoplerfish or Felix on its classpath to create the filter object.
 * 
 * 
 * @author Costin Leau
 */
public class MockFrameworkUtil {

	private static final String EQUINOX_CLS = "org.eclipse.osgi.framework.internal.core.FilterImpl";

	private static final String KF_CLS = "org.knopflerfish.framework.FilterImpl";

	private static final String FELIX_CLS = "org.apache.felix.framework.FilterImpl";

	private final Constructor filterConstructor;


	/**
	 * Constructs a new <code>MockFrameworkUtil</code> instance.
	 * 
	 * As opposed to the OSGi approach this class doesn't use statics since it
	 * makes configuration and initialization a lot harder without any
	 * particular benefit.
	 * 
	 */
	MockFrameworkUtil() {
		// detect filter implementation
		ClassLoader cl = getClass().getClassLoader();
		Class<?> filterClz = null;
		// try Equinox
		filterClz = loadClass(cl, EQUINOX_CLS);
		// try KF
		if (filterClz == null)
			filterClz = loadClass(cl, KF_CLS);
		// try Felix
		if (filterClz == null)
			filterClz = loadClass(cl, FELIX_CLS);

		if (filterClz == null)
			// nothing is found, bail out
			throw new IllegalStateException("cannot find Equinox, Knopflerfish or Felix on the classpath");

		try {
			filterConstructor = filterClz.getConstructor(new Class<?>[] { String.class });
		}
		catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("found invalid filter class " + filterClz);
		}
	}

	private Class<?> loadClass(ClassLoader loader, String className) {
		try {
			return loader.loadClass(className);
		}
		catch (ClassNotFoundException e) {
			// swallow exception
		}

		return null;
	}

	/**
	 * Create a mock filter that is _might_ be valid. This method does not throw
	 * an checked exception and will always return a filter implementation.
	 * 
	 * @param filter OSGi filter given as a String.
	 * @return actual OSGi filter using the underlying OSGi platform
	 */
	public Filter createFilter(String filter) {
		try {
			return (Filter) filterConstructor.newInstance(new Object[] { filter });
		}
		catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}