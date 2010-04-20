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

package org.eclipse.gemini.blueprint.extender.internal.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;

/**
 * Internal utility used for internal purposes.
 * 
 * @author Costin Leau
 */
public abstract class BundleUtils {
	public static final String DM_CORE_ID = "spring.osgi.core.bundle.id";
	public static final String DM_CORE_TS = "spring.osgi.core.bundle.timestamp";

	public static Bundle getDMCoreBundle(BundleContext ctx) {
		ServiceReference ref = ctx.getServiceReference(PackageAdmin.class.getName());
		if (ref != null) {
			Object service = ctx.getService(ref);
			if (service instanceof PackageAdmin) {
				PackageAdmin pa = (PackageAdmin) service;
				if (pa != null) {
					return pa.getBundle(OsgiBundleXmlApplicationContext.class);
				}
			}
		}
		return null;
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
