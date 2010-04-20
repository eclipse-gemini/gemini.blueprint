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

package org.eclipse.gemini.blueprint.compendium.internal.cm;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;
import org.eclipse.gemini.blueprint.util.internal.MapBasedDictionary;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;

/**
 * Class responsible for interacting with the Configuration Admin service. It handles the retrieval and updates for a
 * given persistent id.
 * 
 * @author Costin Leau
 * @see org.osgi.service.cm.ConfigurationAdmin
 * @see ManagedService
 */
class ConfigurationAdminManager implements DisposableBean {

	/**
	 * Configuration Admin whiteboard 'listener'.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private class ConfigurationWatcher implements ManagedService {

		public void updated(Dictionary props) throws ConfigurationException {
			if (log.isTraceEnabled())
				log.trace("Configuration [" + pid + "] has been updated with properties " + props);

			synchronized (monitor) {
				// update properties
				properties = new MapBasedDictionary(props);
				// invoke callback
				if (beanManager != null)
					beanManager.updated(properties);
			}
		}
	}

	/** logger */
	private static final Log log = LogFactory.getLog(ConfigurationAdminManager.class);

	private final BundleContext bundleContext;
	private final String pid;
	// up to date configuration
	private Map properties = null;
	private boolean initialized = false;
	private ManagedServiceBeanManager beanManager;
	private final Object monitor = new Object();

	private ServiceRegistration registration;

	/**
	 * Constructs a new <code>ConfigurationAdminManager</code> instance.
	 * 
	 */
	public ConfigurationAdminManager(String pid, BundleContext bundleContext) {
		this.pid = pid;
		this.bundleContext = bundleContext;
	}

	public void setBeanManager(ManagedServiceBeanManager beanManager) {
		synchronized (monitor) {
			this.beanManager = beanManager;
		}
	}

	/**
	 * Returns the configuration 'monitored' by this managed.
	 * 
	 * @return monitored configuration
	 */
	public Map getConfiguration() {
		initialize();
		synchronized (monitor) {
			return properties;
		}
	}

	/**
	 * Initializes the conversation with the configuration admin. This method allows for lazy service registration to
	 * avoid notification being sent w/o any beans requesting it.
	 */
	private void initialize() {
		synchronized (monitor) {
			if (initialized)
				return;
			initialized = true;

			// initialize the properties
			initProperties();
		}

		if (log.isTraceEnabled())
			log.trace("Initial properties for pid [" + pid + "] are " + properties);

		ServiceRegistration reg = CMUtils.registerManagedService(bundleContext, new ConfigurationWatcher(), pid);

		synchronized (monitor) {
			this.registration = reg;
		}
	}

	private void initProperties() {
		try {
			//TODO: allow timeout for managed-properties as well
			properties = CMUtils.getConfiguration(bundleContext, pid, 0);
		} catch (IOException ioe) {
			// FIXME: consider adding a custom/different exception
			throw new BeanInitializationException("Cannot retrieve configuration for pid=" + pid, ioe);
		}
	}

	public void destroy() {
		ServiceRegistration reg = null;
		synchronized (monitor) {
			reg = this.registration;
			this.registration = null;
		}

		if (OsgiServiceUtils.unregisterService(reg)) {
			log.trace("Shutting down CM tracker for pid [" + pid + "]");
		}
	}
}