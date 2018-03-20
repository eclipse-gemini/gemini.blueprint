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

package org.eclipse.gemini.blueprint.extender.internal.activator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;

/**
 * @author Costin Leau
 */
public class VersionMatcher {

	/** logger */
	private static final Log log = LogFactory.getLog(LifecycleManager.class);

	private final String versionHeader;
	private final Version expectedVersion;


	public VersionMatcher(String versionHeader, Version expectedVersion) {
		this.versionHeader = versionHeader;
		this.expectedVersion = expectedVersion;
	}

	public boolean matchVersion(Bundle bundle) {

		if (!ConfigUtils.matchExtenderVersionRange(bundle, versionHeader, expectedVersion)) {
			if (log.isDebugEnabled())
				log.debug("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle)
						+ "] expects an extender w/ version[" + bundle.getHeaders().get(versionHeader)
						+ "] which does not match current extender w/ version[" + expectedVersion
						+ "]; skipping bundle analysis...");
			return false;
		}

		return true;
	}
}
