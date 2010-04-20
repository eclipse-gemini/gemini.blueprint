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

import java.util.Properties;

import org.osgi.framework.Constants;

/**
 * Default properties used by the BundleContext.
 * 
 * @author Costin Leau
 * 
 */
class DefaultBundleContextProperties extends Properties {

	private static final long serialVersionUID = 7814061041669242672L;


	public DefaultBundleContextProperties() {
		this(null);
	}

	public DefaultBundleContextProperties(Properties defaults) {
		super(defaults);
		initProperties();
	}

	private static String getVersion() {
		Package pkg = MockBundleContext.class.getPackage();
		if (pkg != null) {
			String version = pkg.getImplementationVersion();
			if (version != null)
				return version;
		}

		return "unknown";
	}

	protected void initProperties() {
		put(Constants.FRAMEWORK_VERSION, getVersion());
		put(Constants.FRAMEWORK_VENDOR, "SpringSource");
		put(Constants.FRAMEWORK_LANGUAGE, System.getProperty("user.language"));
		put(Constants.FRAMEWORK_OS_NAME, System.getProperty("os.name"));
		put(Constants.FRAMEWORK_OS_VERSION, System.getProperty("os.version"));
		put(Constants.FRAMEWORK_PROCESSOR, System.getProperty("os.arch"));
	}
}
