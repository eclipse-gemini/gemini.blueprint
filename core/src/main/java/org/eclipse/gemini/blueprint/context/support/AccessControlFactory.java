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

package org.eclipse.gemini.blueprint.context.support;

import java.security.AccessControlContext;
import java.security.Permission;
import java.security.ProtectionDomain;

import org.osgi.framework.Bundle;

/**
 * Security utility for wrapping an AccessControlContext around a Bundle.
 * 
 * @author Costin Leau
 */
abstract class AccessControlFactory {

	private static class BundleProtectionDomain extends ProtectionDomain {

		private final Bundle bundle;

		BundleProtectionDomain(Bundle bundle) {
			// cannot determine CodeSource or PermissionCollection from a bundle
			super(null, null);
			this.bundle = bundle;
		}

		@Override
		public boolean implies(Permission permission) {
			return bundle.hasPermission(permission);
		}
	}

	/**
	 * Creates an AccessControlContext based on the current security context and the given bundle.
	 * 
	 * @param bundle
	 * @return
	 */
	static AccessControlContext createContext(Bundle bundle) {
		return new AccessControlContext(new ProtectionDomain[] { new BundleProtectionDomain(bundle) });
	}
}
