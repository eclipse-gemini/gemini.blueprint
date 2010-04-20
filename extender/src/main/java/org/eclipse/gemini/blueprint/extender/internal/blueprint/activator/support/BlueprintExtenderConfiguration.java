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

package org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.support;

import org.apache.commons.logging.Log;
import org.eclipse.gemini.blueprint.extender.OsgiApplicationContextCreator;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.osgi.framework.BundleContext;

/**
 * Extension of the default extender configuration for handling RFC 124 extender semantics.
 * 
 * @author Costin Leau
 * 
 */
public class BlueprintExtenderConfiguration extends ExtenderConfiguration {

	private final OsgiApplicationContextCreator contextCreator = new BlueprintContainerCreator();

	/**
	 * Constructs a new <code>BlueprintExtenderConfiguration</code> instance.
	 * 
	 * @param bundleContext
	 */
	public BlueprintExtenderConfiguration(BundleContext bundleContext, Log log) {
		super(bundleContext, log);
	}

	public OsgiApplicationContextCreator getContextCreator() {
		return contextCreator;
	}
}
