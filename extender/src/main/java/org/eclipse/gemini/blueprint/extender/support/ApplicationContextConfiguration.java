/*
 Copyright (c) 2006, 2010 VMware Inc.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 and Apache License v2.0 which accompanies this distribution.
 The Eclipse Public License is available at
 http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 is available at http://www.opensource.org/licenses/apache2.0.php.
 You may elect to redistribute this code under either of these licenses.

 Contributors:
 VMware Inc.
 */

package org.eclipse.gemini.blueprint.extender.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.extender.support.scanning.ConfigurationScanner;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.osgi.framework.Bundle;
import org.springframework.util.ObjectUtils;

import java.util.Dictionary;

import static org.springframework.util.Assert.notNull;

/**
 * Configuration class for Blueprint application contexts.
 * <p>
 * Determines the configuration information available in a bundle for constructing an application context. Reads all the
 * Blueprint options present in the bundle header.
 *
 * @author Adrian Colyer
 * @author Costin Leau
 */
public abstract class ApplicationContextConfiguration {

    /**
     * logger
     */
    private static final Log LOG = LogFactory.getLog(ApplicationContextConfiguration.class);

    private final boolean asyncCreation;
    private final String[] configurationLocations;
    private final boolean isBlueprintConfigurationPresent;
    private final boolean publishContextAsService;
    private final boolean waitForDeps;
    private final String toString;
    private final long timeout;
    private final boolean hasTimeout;

    public ApplicationContextConfiguration(Bundle bundle, ConfigurationScanner configurationScanner) {
        notNull(bundle, "Constructor parameter bundle must not be null.");
        notNull(configurationScanner, "Constructor parameter configurationScanner must not be null.");

        Dictionary headers = bundle.getHeaders();

        String[] configs = configurationScanner.getConfigurations(bundle);

        this.isBlueprintConfigurationPresent = !ObjectUtils.isEmpty(configs);
        this.configurationLocations = configs;

        this.hasTimeout = ConfigUtils.isDirectiveDefined(headers, ConfigUtils.DIRECTIVE_TIMEOUT);

        long option = ConfigUtils.getTimeOut(headers);
        // translate into ms
        this.timeout = (option >= 0 ? option * 1000 : option);
        this.publishContextAsService = ConfigUtils.getPublishContext(headers);
        this.asyncCreation = ConfigUtils.getCreateAsync(headers);
        this.waitForDeps = ConfigUtils.getWaitForDependencies(headers);

        // create toString
        this.toString = "AppCtxCfg [Bundle=" +
                OsgiStringUtils.nullSafeSymbolicName(bundle) +
                "]isBlueprintConfigurationPresent=" +
                isBlueprintConfigurationPresent() +
                "|async=" +
                isCreateAsynchronously() +
                "|wait-for-deps=" +
                isWaitForDependencies() +
                "|publishCtx=" +
                isPublishContextAsService() +
                "|timeout=" +
                getTimeout() / 1000 +
                "s";
        if (LOG.isTraceEnabled()) {
            LOG.trace("Configuration: " + this.toString);
        }
    }

    /**
     * Indicates if the given bundle is "Spring-Powered" or not.
     * <p>
     * True if this bundle has at least one defined application context configuration file.
     * <p>
     * <p/> A bundle is "Spring-Powered" if it has at least one configuration resource.
     */
    public boolean isBlueprintConfigurationPresent() {
        return this.isBlueprintConfigurationPresent;
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
     * <p>
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