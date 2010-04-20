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

package org.eclipse.gemini.blueprint.blueprint.container.support;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.context.support.internal.security.SecurityUtils;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ObjectUtils;

/**
 * Infrastructure bean that automatically publishes the given ModuleContext as an OSGi service. The bean listens for the
 * start/stop events inside an {@link ApplicationContext} to register/unregister the equivalent service.
 * 
 * <b>Note:</b> This component is stateful and should not be shared by multiple threads.
 * 
 * @author Costin Leau
 * 
 */
public class BlueprintContainerServicePublisher implements ApplicationListener<ApplicationContextEvent> {

	/** logger */
	private static final Log log = LogFactory.getLog(BlueprintContainerServicePublisher.class);

	private static final String BLUEPRINT_SYMNAME = "osgi.blueprint.container.symbolicname";
	private static final String BLUEPRINT_VERSION = "osgi.blueprint.container.version";

	private final BlueprintContainer blueprintContainer;
	private final BundleContext bundleContext;
	/** registration */
	private volatile ServiceRegistration registration;

	/**
	 * Constructs a new <code>ModuleContextServicePublisher</code> instance.
	 * 
	 * @param blueprintContainer
	 * @param bundleContext
	 */
	public BlueprintContainerServicePublisher(BlueprintContainer blueprintContainer, BundleContext bundleContext) {
		this.blueprintContainer = blueprintContainer;
		this.bundleContext = bundleContext;
	}

	public void onApplicationEvent(ApplicationContextEvent event) {
		// publish
		if (event instanceof ContextRefreshedEvent) {
			registerService(event.getApplicationContext());
		} else if (event instanceof ContextClosedEvent) {
			unregisterService();
		}
	}

	private void registerService(ApplicationContext applicationContext) {
		final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>();

		Bundle bundle = bundleContext.getBundle();
		String symName = bundle.getSymbolicName();
		serviceProperties.put(Constants.BUNDLE_SYMBOLICNAME, symName);
		serviceProperties.put(BLUEPRINT_SYMNAME, symName);

		Version version = OsgiBundleUtils.getBundleVersion(bundle);
		serviceProperties.put(Constants.BUNDLE_VERSION, version);
		serviceProperties.put(BLUEPRINT_VERSION, version);

		log.info("Publishing BlueprintContainer as OSGi service with properties " + serviceProperties);

		// export just the interface
		final String[] serviceNames = new String[] { BlueprintContainer.class.getName() };

		if (log.isDebugEnabled())
			log.debug("Publishing service under classes " + ObjectUtils.nullSafeToString(serviceNames));

		AccessControlContext acc = SecurityUtils.getAccFrom(applicationContext);

		// publish service
		if (System.getSecurityManager() != null) {
			registration = AccessController.doPrivileged(new PrivilegedAction<ServiceRegistration>() {
				public ServiceRegistration run() {
					return bundleContext.registerService(serviceNames, blueprintContainer, serviceProperties);
				}
			}, acc);
		} else {
			registration = bundleContext.registerService(serviceNames, blueprintContainer, serviceProperties);
		}
	}

	private void unregisterService() {
		OsgiServiceUtils.unregisterService(registration);
		registration = null;
	}
}