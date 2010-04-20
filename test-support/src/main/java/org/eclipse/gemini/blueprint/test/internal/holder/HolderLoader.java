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

package org.eclipse.gemini.blueprint.test.internal.holder;

import java.lang.reflect.Field;

import org.osgi.framework.Bundle;
import org.springframework.util.ReflectionUtils;

/**
 * Specific OSGi loader for OsgiTestHolder. It's main usage is to load the
 * holder using a class-loader outside the OSGi world and to store results
 * there.
 * 
 * <p/> Boot delegation should work here but each platform has its own approach.
 * Notably, Equinox uses by default the boot (not the app) classloader which
 * means the classpath used for starting OSGi is not seen. To not interfere with
 * the default configuration which might change in the future (it has been
 * changed between 3.2 and 3.3) and to not impose restrictions on the test
 * usage, the loader manually discovers the proper classloader and uses it to
 * load the holder class. Inside OSGi, special care must be taken to make sure
 * no CCE are generated.
 * 
 * @author Costin Leau
 * 
 */
public class HolderLoader {

	private static final String INSTANCE_FIELD = "INSTANCE";

	private static final String HOLDER_CLASS_NAME = "org.eclipse.gemini.blueprint.test.internal.holder.OsgiTestInfoHolder";

	public static final HolderLoader INSTANCE = new HolderLoader();

	private final OsgiTestInfoHolder holder;


	public HolderLoader() {
		// try to load the holder using the app ClassLoader
		ClassLoader appCL = Bundle.class.getClassLoader();
		Class<?> clazz;
		try {
			clazz = appCL.loadClass(HOLDER_CLASS_NAME);
		}
		catch (Exception ex) {
			// if it's not found, then the class path is incorrectly constructed
			throw (RuntimeException) new IllegalStateException(
				"spring-osgi-test.jar is not available on the boot class path; are you deploying the test framework"
						+ "as a bundle by any chance? ").initCause(ex);
		}
		// get the static instance
		Field field = ReflectionUtils.findField(clazz, INSTANCE_FIELD, clazz);
		Object instance;
		try {
			instance = field.get(null);
		}
		catch (Exception ex) {
			throw (RuntimeException) new IllegalStateException("Cannot read property " + INSTANCE_FIELD).initCause(ex);
		}
		// once the class is loaded return it wrapped through it's OSGi instance
		holder = new ReflectionOsgiHolder(instance);
	}

	public OsgiTestInfoHolder getHolder() {
		return holder;
	}

}
