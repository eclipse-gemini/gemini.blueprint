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
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.eclipse.gemini.blueprint.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.extender.OsgiApplicationContextCreator;
import org.eclipse.gemini.blueprint.extender.support.ApplicationContextConfiguration;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Blueprint specific context creator. Picks up the Blueprint locations instead of Spring DM's.
 * 
 * @author Costin Leau
 * 
 */
public class BlueprintContainerCreator implements OsgiApplicationContextCreator {

	/** logger */
	private static final Log log = LogFactory.getLog(BlueprintContainerCreator.class);

	public DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext)
			throws Exception {
		Bundle bundle = bundleContext.getBundle();
		ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
		String bundleName = OsgiStringUtils.nullSafeNameAndSymName(bundle);
		if (log.isTraceEnabled())
			log.trace("Created configuration " + config + " for bundle " + bundleName);

		// it's not a spring bundle, ignore it
		if (!config.isSpringPoweredBundle()) {
			if (log.isDebugEnabled())
				log.debug("No blueprint configuration found in bundle " + bundleName + "; ignoring it...");
			return null;
		}

		// If Aries Blueprint is present, delegates to him the blueprint instantiation
		List<Bundle> allBundles = Arrays.asList(bundleContext.getBundles());
		boolean hasAries = allBundles.stream()
						.anyMatch(b -> b.getSymbolicName().equals("org.apache.aries.blueprint.core"));
		if (hasAries) {
			log.info("[Gemini Extender] Aries Blueprint is enabled at the running container, skipping blueprint " +
							"instantiation for bundle " + bundleName + "...");
			return null;
		}

		log.info("Discovered configurations " + ObjectUtils.nullSafeToString(config.getConfigurationLocations())
				+ " in bundle [" + bundleName + "]");

		DelegatedExecutionOsgiBundleApplicationContext sdoac =
				new OsgiBundleXmlApplicationContext(config.getConfigurationLocations());
		sdoac.setBundleContext(bundleContext);
		sdoac.setPublishContextAsService(config.isPublishContextAsService());

		return sdoac;
	}
}
