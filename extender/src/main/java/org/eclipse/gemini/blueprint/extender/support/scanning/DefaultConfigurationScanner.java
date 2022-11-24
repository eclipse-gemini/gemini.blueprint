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

package org.eclipse.gemini.blueprint.extender.support.scanning;

import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.io.OsgiBundleResource;
import org.osgi.framework.Bundle;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Default implementation of {@link ConfigurationScanner} interface.
 * 
 * <p/>Supports <tt>Spring-Context</tt> manifest header and
 * <tt>META-INF/spring/*.xml</tt>.
 * 
 * @author Costin Leau
 *
 */
public class DefaultConfigurationScanner implements ConfigurationScanner {

	private static final String CONTEXT_DIR = "/META-INF/spring/";

	private static final String CONTEXT_FILES = "*.xml";

	/** Default configuration location */
	public static final String DEFAULT_CONFIG = OsgiBundleResource.BUNDLE_URL_PREFIX + CONTEXT_DIR + CONTEXT_FILES;


	public String[] getConfigurations(Bundle bundle) {
		String[] locations = ConfigUtils.getHeaderLocations(bundle.getHeaders());

        // if no location is specified in the header, try the defaults
        if (ObjectUtils.isEmpty(locations)) {
            // check the default locations if the manifest doesn't provide any info
            Enumeration defaultConfig = bundle.findEntries(CONTEXT_DIR, CONTEXT_FILES, false);
            if (defaultConfig != null && defaultConfig.hasMoreElements()) {
                return new String[]{DEFAULT_CONFIG};
            } else {
                return new String[0];
            }
        } else {
            return parseLocations(locations);
        }
    }

    private String[] parseLocations(String... locations) {
        List<String> parsedLocations = new ArrayList<>();
        for (String location : locations) {
            String parsed = replaceSysEnv(location);
            parsedLocations.add(parsed);
        }
        return parsedLocations.toArray(new String[0]);
    }

    private String replaceSysEnv(String location) {
        if (location != null && !location.isEmpty()) {
            int envBegin = location.indexOf("${");
            int envEnd = location.indexOf("}");
            if (envBegin >= 0 && envEnd > envBegin) {
                String envName = location.substring(envBegin+2, envEnd);
                String envValue = System.getenv(envName);
                if (envValue != null) {
                    return "file:" + location.replace("${" + envName + "}", envValue);
                }
            }
        }
        return location;
    }
}
