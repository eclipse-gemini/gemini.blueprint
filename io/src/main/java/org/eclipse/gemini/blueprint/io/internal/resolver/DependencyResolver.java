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

package org.eclipse.gemini.blueprint.io.internal.resolver;

import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Simple interface offering utility methods for OSGi dependencies, mainly
 * bundles. This class suplements the {@link PackageAdmin} service by offering
 * information on importing, not just exporting.
 * 
 * @author Costin Leau
 *
 * TODO: Rework to remove reference to PackageAdmin
 */
public interface DependencyResolver {

	/**
	 * Returns the bundles imported by the given bundle. It's up to the
	 * implementation to consider required bundles, bundle class-path and
	 * dynamic imports.
	 * 
	 * <p/> The returned array should not contain duplicates (each imported
	 * bundle should be present exactly once).
	 * 
	 * <p/> In general it is not expected to have knowledge about runtime
	 * loading (such as dynamic imports).
	 * 
	 * @param bundle OSGi bundle for which imported bundles will be determined
	 * @return a not-null array of importing bundles
	 */
	ImportedBundle[] getImportedBundles(Bundle bundle);
}
