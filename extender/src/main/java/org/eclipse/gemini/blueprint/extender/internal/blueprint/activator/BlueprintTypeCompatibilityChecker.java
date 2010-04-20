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

package org.eclipse.gemini.blueprint.extender.internal.blueprint.activator;

import org.eclipse.gemini.blueprint.extender.internal.activator.TypeCompatibilityChecker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Basic type compatibility checker
 * @author Costin Leau
 */
class BlueprintTypeCompatibilityChecker implements TypeCompatibilityChecker {

	// container package check
	private static final String CONTAINER_PKG_CLASS = "org.osgi.service.blueprint.container.BlueprintContainer";
	// reflect package check
	private static final String REFLECT_PKG_CLASS = "org.osgi.service.blueprint.reflect.ComponentMetadata";

	private final Class<?> containerPkgClass;
	private final Class<?> reflectPkgClass;

	public BlueprintTypeCompatibilityChecker(Bundle extenderBundle) {
		try {
			containerPkgClass = extenderBundle.loadClass(CONTAINER_PKG_CLASS);
			reflectPkgClass = extenderBundle.loadClass(REFLECT_PKG_CLASS);
		} catch (ClassNotFoundException cnf) {
			throw new IllegalStateException("Cannot load blueprint classes " + cnf);
		}
	}

	public boolean isTypeCompatible(BundleContext targetContext) {
		Bundle bnd = targetContext.getBundle();
		return (checkCompatibility(CONTAINER_PKG_CLASS, bnd, containerPkgClass) && checkCompatibility(
				REFLECT_PKG_CLASS, bnd, reflectPkgClass));
	}

	private boolean checkCompatibility(String of, Bundle in, Class<?> against) {
		try {
			Class<?> found = in.loadClass(of);
			return against.equals(found);
		} catch (ClassNotFoundException cnf) {
			// no class means compatible
			return true;
		}
	}
}
