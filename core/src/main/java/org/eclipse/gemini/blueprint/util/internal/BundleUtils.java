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

package org.eclipse.gemini.blueprint.util.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Internal utility used for internal purposes.
 * 
 * @author Costin Leau
 */
public abstract class BundleUtils {
	public static final String DM_CORE_ID = "spring.osgi.core.bundle.id";
	public static final String DM_CORE_TS = "spring.osgi.core.bundle.timestamp";

    public static Bundle getDMCoreBundle(BundleContext ctx) {
        return FrameworkUtil.getBundle(BundleUtils.class);
    }

	public static String createNamespaceFilter(BundleContext ctx) {
		Bundle bnd = getDMCoreBundle(ctx);
		if (bnd != null) {
			return "(|(" + DM_CORE_ID + "=" + bnd.getBundleId() + ")(" + DM_CORE_TS + "=" + bnd.getLastModified()
					+ "))";
		}
		return "";
	}
}
