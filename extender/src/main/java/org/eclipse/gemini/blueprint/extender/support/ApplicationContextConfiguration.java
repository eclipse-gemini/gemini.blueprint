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

import java.util.Dictionary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.extender.support.scanning.ConfigurationScanner;
import org.eclipse.gemini.blueprint.extender.support.scanning.DefaultConfigurationScanner;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Configuration class for Spring-DM application contexts.
 * 
 * Determines the configuration information available in a bundle for constructing an application context. Reads all the
 * Spring-DM options present in the bundle header.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
public class ApplicationContextConfiguration {

	/** logger */
	private static final Log log = LogFactory.getLog(ApplicationContextConfiguration.class);

	private final Bundle bundle;
	private final ConfigurationScanner configurationScanner;
	private final boolean asyncCreation;
	private final String[] configurationLocations;
	private final boolean isSpringPoweredBundle;
	private final boolean publishContextAsService;
	private final boolean waitForDeps;
	private final String toString;
	private final long timeout;
	private final boolean hasTimeout;

	/**
	 * Constructs a new <code>ApplicationContextConfiguration</code> instance from the given bundle. Uses the
	 * {@link DefaultConfigurationScanner} internally for discovering Spring-powered bundles.
	 * 
	 * @param bundle bundle for which the application context configuration is created
	 */
	public ApplicationContextConfiguration(Bundle bundle) {
		this(bundle, new DefaultConfigurationScanner());
	}

	public ApplicationContextConfiguration(Bundle bundle, ConfigurationScanner configurationScanner) {
		Assert.notNull(bundle, "bundle is required");
		Assert.notNull(configurationScanner, "configurationScanner is required");
		this.bundle = bundle;
		this.configurationScanner = configurationScanner;

		Dictionary headers = this.bundle.getHeaders();

		String[] configs = this.configurationScanner.getConfigurations(bundle);

		this.isSpringPoweredBundle = !ObjectUtils.isEmpty(configs);
		this.configurationLocations = configs;

		hasTimeout = ConfigUtils.isDirectiveDefined(headers, ConfigUtils.DIRECTIVE_TIMEOUT);

		long option = ConfigUtils.getTimeOut(headers);
		// translate into ms
		this.timeout = (option >= 0 ? option * 1000 : option);
		this.publishContextAsService = ConfigUtils.getPublishContext(headers);
		this.asyncCreation = ConfigUtils.getCreateAsync(headers);
		this.waitForDeps = ConfigUtils.getWaitForDependencies(headers);

		// create toString
		StringBuilder buf = new StringBuilder();
		buf.append("AppCtxCfg [Bundle=");
		buf.append(OsgiStringUtils.nullSafeSymbolicName(bundle));
		buf.append("]isSpringBundle=");
		buf.append(isSpringPoweredBundle());
		buf.append("|async=");
		buf.append(isCreateAsynchronously());
		buf.append("|wait-for-deps=");
		buf.append(isWaitForDependencies());
		buf.append("|publishCtx=");
		buf.append(isPublishContextAsService());
		buf.append("|timeout=");
		buf.append(getTimeout() / 1000);
		buf.append("s");
		toString = buf.toString();
		if (log.isTraceEnabled()) {
			log.trace("Configuration: " + toString);
		}
	}

	/**
	 * Indicates if the given bundle is "Spring-Powered" or not.
	 * 
	 * True if this bundle has at least one defined application context configuration file.
	 * 
	 * <p/> A bundle is "Spring-Powered" if it has at least one configuration resource.
	 */
	public boolean isSpringPoweredBundle() {
		return this.isSpringPoweredBundle;
	}

	public boolean isTimeoutDeclared() {
		return hasTimeout;
	}

	/**
	 * Returns the timeout (in milliseconds) an application context needs to wait for mandatory dependent services.
	 */
	public long getTimeout() {
		return this.timeout;
	}

	/**
	 * Indicates if an application context needs to be created asynchronously or not.
	 * 
	 * Should the application context wait for all non-optional service references to be satisfied before starting?
	 */
	public boolean isCreateAsynchronously() {
		return this.asyncCreation;
	}

	/**
	 * Indicates if the application context needs to be published as a service or not.
	 * 
	 * @return Returns the publishContextAsService.
	 */
	public boolean isPublishContextAsService() {
		return publishContextAsService;
	}

	/**
	 * Indicates if the configuration must wait for dependencies.
	 * 
	 * @return true if the configuration indicates that dependencies should be waited for.
	 */
	public boolean isWaitForDependencies() {
		return waitForDeps;
	}

	/**
	 * Returns the locations of the configuration resources used to build the application context (as Spring resource
	 * paths).
	 * 
	 * @return configuration paths
	 */
	public String[] getConfigurationLocations() {
		return this.configurationLocations;
	}

	public String toString() {
		return toString;
	}
}