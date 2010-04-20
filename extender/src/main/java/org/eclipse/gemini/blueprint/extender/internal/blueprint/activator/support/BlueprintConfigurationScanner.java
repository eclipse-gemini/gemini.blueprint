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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.extender.support.scanning.ConfigurationScanner;
import org.eclipse.gemini.blueprint.io.OsgiBundleResource;
import org.eclipse.gemini.blueprint.io.OsgiBundleResourcePatternResolver;
import org.osgi.framework.Bundle;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.springframework.util.ObjectUtils;

/**
 * Dedication {@link ConfigurationScanner scanner} implementation suitable for Blueprint bundles.
 * 
 * @author Costin Leau
 */
public class BlueprintConfigurationScanner implements ConfigurationScanner {

	/** logger */
	private static final Log log = LogFactory.getLog(BlueprintConfigurationScanner.class);

	private static final String CONTEXT_DIR = "OSGI-INF/blueprint/";

	private static final String CONTEXT_FILES = "*.xml";

	/** Default configuration location */
	public static final String DEFAULT_CONFIG = OsgiBundleResource.BUNDLE_URL_PREFIX + CONTEXT_DIR + CONTEXT_FILES;

	public String[] getConfigurations(Bundle bundle) {
		String bundleName = OsgiStringUtils.nullSafeName(bundle);

		boolean trace = log.isTraceEnabled();
		boolean debug = log.isDebugEnabled();

		if (debug)
			log.debug("Scanning bundle " + bundleName + " for blueprint configurations...");

		String[] locations = BlueprintConfigUtils.getBlueprintHeaderLocations(bundle.getHeaders());

		// if no location is specified in the header, try the defaults
		if (locations == null) {
			if (trace) {
				log.trace("Bundle " + bundleName + " has no declared locations; trying default " + DEFAULT_CONFIG);
			}
			locations = new String[] { DEFAULT_CONFIG };
		} else {
			// check whether the header is empty
			if (ObjectUtils.isEmpty(locations)) {
				log.info("Bundle " + bundleName + " has an empty blueprint header - ignoring bundle...");
				return new String[0];
			}
		}

		String[] configs = findValidBlueprintConfigs(bundle, locations);
		if (debug)
			log.debug("Discovered in bundle" + bundleName + " blueprint configurations=" + Arrays.toString(configs));
		return configs;
	}

	/**
	 * Checks if the given bundle contains existing configurations. The absolute paths are returned without performing
	 * any checks.
	 * 
	 * @return
	 */
	private String[] findValidBlueprintConfigs(Bundle bundle, String[] locations) {
		List<String> configs = new ArrayList<String>(locations.length);
		ResourcePatternResolver loader = new OsgiBundleResourcePatternResolver(bundle);

		boolean debug = log.isDebugEnabled();
		for (String location : locations) {
			if (isAbsolute(location)) {
				configs.add(location);
			}
			// resolve the location to check if it's present
			else {
				try {
					String loc = location;
					if (loc.endsWith("/")) {
						loc = loc + "*.xml";
					}
					Resource[] resources = loader.getResources(loc);
					if (!ObjectUtils.isEmpty(resources)) {
						for (Resource resource : resources) {
							if (resource.exists()) {
								String value = resource.getURL().toString();
								if (debug)
									log.debug("Found location " + value);
								configs.add(value);
							}
						}
					}
				} catch (IOException ex) {
					if (debug)
						log.debug("Cannot resolve location " + location, ex);
				}
			}
		}
		return (String[]) configs.toArray(new String[configs.size()]);
	}

	private boolean isAbsolute(String location) {
		return !(location.endsWith("/") || location.contains("*"));
	}
}