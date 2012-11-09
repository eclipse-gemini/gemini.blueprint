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

package org.eclipse.gemini.blueprint.extender.internal.boot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.extender.internal.activator.*;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.BlueprintLoaderListener;
import org.eclipse.gemini.blueprint.util.OsgiPlatformDetector;
import org.springframework.util.ClassUtils;

/**
 * Bundle activator that simply the lifecycle callbacks to other activators.
 *
 * @author Costin Leau
 *
 */
public class ChainActivator implements BundleActivator {

	protected final Log log = LogFactory.getLog(getClass());

	private static final boolean BLUEPRINT_AVAILABLE =
			ClassUtils.isPresent("org.osgi.service.blueprint.container.BlueprintContainer", ChainActivator.class
					.getClassLoader());

	private final BundleActivator[] CHAIN;

	public ChainActivator() {
        final LoggingActivator logStatus = new LoggingActivator();
        final JavaBeansCacheActivator activateJavaBeansCache = new JavaBeansCacheActivator();
        final NamespaceHandlerActivator activateCustomNamespaceHandling = new NamespaceHandlerActivator();
        final NamespaceHandlerActivator activateBlueprintspecificNamespaceHandling = new BlueprintNamespaceHandlerActivator();
        final ExtenderConfiguration initializeExtenderConfiguration = new ExtenderConfiguration();
        final ListenerServiceActivator activateListeners = new ListenerServiceActivator(initializeExtenderConfiguration);
        final ContextLoaderListener listenForSpringDmBundles = new ContextLoaderListener(initializeExtenderConfiguration);
        final BlueprintLoaderListener listenForBlueprintBundles = new BlueprintLoaderListener(initializeExtenderConfiguration, activateListeners);

        if (OsgiPlatformDetector.isR42()) {
			if (BLUEPRINT_AVAILABLE) {
				log.info("Blueprint API detected; enabling Blueprint Container functionality");
				CHAIN = new BundleActivator[] {
                        logStatus,
                        activateJavaBeansCache,
                        activateCustomNamespaceHandling,
                        activateBlueprintspecificNamespaceHandling,
                        initializeExtenderConfiguration,
                        activateListeners,
                        listenForSpringDmBundles,
                        listenForBlueprintBundles
                };
			}
			else {
				log.warn("Blueprint API not found; disabling Blueprint Container functionality");
				CHAIN = new BundleActivator[] {
                        logStatus,
                        activateJavaBeansCache,
                        activateCustomNamespaceHandling,
                        initializeExtenderConfiguration,
                        activateListeners,
                        listenForSpringDmBundles
                };
			}
		} else {
			log.warn("Pre-4.2 OSGi platform detected; disabling Blueprint Container functionality");
            CHAIN = new BundleActivator[] {
                    logStatus,
                    activateJavaBeansCache,
                    activateCustomNamespaceHandling,
                    initializeExtenderConfiguration,
                    activateListeners,
                    listenForSpringDmBundles
            };
        }
	}

	public void start(BundleContext context) throws Exception {
		for (int i = 0; i < CHAIN.length; i++) {
			CHAIN[i].start(context);
		}
	}

	public void stop(BundleContext context) throws Exception {
		for (int i = CHAIN.length - 1; i >= 0; i--) {
			CHAIN[i].stop(context);
		}
	}
}
