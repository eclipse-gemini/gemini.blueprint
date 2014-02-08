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

package org.eclipse.gemini.blueprint.test.platform;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for OsgiPlatform classes. Provides common functionality such as creation a temporary folder on startup and
 * deletion on shutdown. Uses system properties to allow easy configuration from the command line.
 * 
 * @author Costin Leau
 */
abstract class AbstractOsgiPlatform implements OsgiPlatform {

	private static final String TMP_DIR_FALLBACK = "./tmp-test";

	private static final String DEFAULT_SUFFIX = "osgi";

	private static final String TMP_PREFIX = "org.sfw.osgi";

	final Log log = LogFactory.getLog(getClass());

	/**
	 * Subclasses should override this field.
	 */
	String toString = getClass().getName();

	private Properties configurationProperties = null;

	/**
	 * {@inheritDoc}
	 * 
	 * This implementation considers existing system properties as well as platform specific ones, defined in this
	 * class. The system properties are convenient for changing the configuration directly from the command line (useful
	 * for CI builds) leaving the programmer to ultimately decide the actual configuration used.
	 */
	public Properties getConfigurationProperties() {
		// check if defaults should apply
		if (configurationProperties == null) {
			configurationProperties = new Properties();
			// system properties
			configurationProperties.putAll(System.getProperties());
			// local properties
			configurationProperties.putAll(getPlatformProperties());
			return configurationProperties;
		}
		return configurationProperties;
	}

	/**
	 * Subclasses can override this to provide special platform properties.
	 * 
	 * @return platform implementation specific properties.
	 */
	abstract Properties getPlatformProperties();

	/**
	 * Returns the underlying OSGi platform name.
	 * 
	 * @return the platform name
	 */
	public String toString() {
		return toString;
	}

	File createTempDir(String suffix) {
		if (suffix == null) {
			suffix = DEFAULT_SUFFIX;
        }

		File tempFileName;
		try {
			tempFileName = File.createTempFile(TMP_PREFIX, suffix);
		} catch (IOException ex) {
			if (log.isWarnEnabled()) {
				log.warn("Could not create temporary directory, returning a temp folder inside the current folder", ex);
			}
			return new File(TMP_DIR_FALLBACK);
		}

		tempFileName.delete(); // we want it to be a directory...
		File tempFolder = new File(tempFileName.getAbsolutePath());
		tempFolder.mkdir();
		return tempFolder;
	}
}