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

package org.eclipse.gemini.blueprint.extender.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.eclipse.gemini.blueprint.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.extender.OsgiApplicationContextCreator;
import org.eclipse.gemini.blueprint.extender.support.scanning.ConfigurationScanner;
import org.eclipse.gemini.blueprint.extender.support.scanning.DefaultConfigurationScanner;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Default {@link OsgiApplicationContextCreator} implementation.
 * 
 * <p/> Creates an {@link OsgiBundleXmlApplicationContext} instance using the
 * default locations (<tt>Spring-Context</tt> manifest header or
 * <tt>META-INF/spring/*.xml</tt>) if available, null otherwise.
 * 
 * <p/> Additionally, this implementation allows custom locations to be
 * specified through {@link ConfigurationScanner} interface.
 * 
 * @see ConfigurationScanner
 * @author Costin Leau
 * 
 */
public class DefaultOsgiApplicationContextCreator implements OsgiApplicationContextCreator {

	/** logger */
	private static final Log log = LogFactory.getLog(DefaultOsgiApplicationContextCreator.class);

	private ConfigurationScanner configurationScanner = new DefaultConfigurationScanner();


	public DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext)
			throws Exception {
		Bundle bundle = bundleContext.getBundle();
		ApplicationContextConfiguration config = new ApplicationContextConfiguration(bundle, configurationScanner);
		if (log.isTraceEnabled())
			log.trace("Created configuration " + config + " for bundle "
					+ OsgiStringUtils.nullSafeNameAndSymName(bundle));

		// it's not a spring bundle, ignore it
		if (!config.isSpringPoweredBundle()) {
			return null;
		}

		log.info("Discovered configurations " + ObjectUtils.nullSafeToString(config.getConfigurationLocations())
				+ " in bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "]");

		DelegatedExecutionOsgiBundleApplicationContext sdoac = new OsgiBundleXmlApplicationContext(
			config.getConfigurationLocations());
		sdoac.setBundleContext(bundleContext);
		sdoac.setPublishContextAsService(config.isPublishContextAsService());

		return sdoac;
	}

	/**
	 * Sets the configurationScanner used by this creator.
	 * 
	 * @param configurationScanner The configurationScanner to set.
	 */
	public void setConfigurationScanner(ConfigurationScanner configurationScanner) {
		Assert.notNull(configurationScanner);
		this.configurationScanner = configurationScanner;
	}
}