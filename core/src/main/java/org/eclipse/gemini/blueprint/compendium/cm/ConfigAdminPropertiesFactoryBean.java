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

package org.eclipse.gemini.blueprint.compendium.cm;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.compendium.internal.cm.CMUtils;
import org.eclipse.gemini.blueprint.compendium.internal.cm.util.ChangeableProperties;
import org.eclipse.gemini.blueprint.compendium.internal.cm.util.PropertiesUtil;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.service.exporter.support.ServicePropertiesChangeListener;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * FactoryBean returning the properties stored under a given persistent id in the ConfigurationAdmin service. Once
 * retrieved, the properties will remain the same, even when the configuration object that it maps, changes.
 * 
 * <b>Note:</b> This implementation performs a lazy initialization of the properties to receive the most up to date
 * configuration.
 * 
 * @author Costin Leau
 * @see Configuration
 * @see ConfigurationAdmin
 * @see org.springframework.core.io.support.PropertiesFactoryBean
 */
public class ConfigAdminPropertiesFactoryBean implements BundleContextAware, InitializingBean, DisposableBean,
		FactoryBean<Properties> {

	@SuppressWarnings("unchecked")
	private class ConfigurationWatcher implements ManagedService {

		public void updated(Dictionary props) throws ConfigurationException {
			if (log.isTraceEnabled())
				log.trace("Configuration [" + persistentId + "] has been updated with properties " + props);

			// update properties
			PropertiesUtil.initProperties(localProperties, localOverride, props, properties);
			// inform listeners (the dynamic check is redundant but nevertheless safe)
			if (dynamic) {
				((ChangeableProperties) properties).notifyListeners();
			}
		}
	}

	/** logger */
	private static final Log log = LogFactory.getLog(ConfigAdminPropertiesFactoryBean.class);

	private volatile String persistentId;
	private volatile Properties properties;
	private BundleContext bundleContext;
	private boolean localOverride = false;
	private Properties localProperties;
	private volatile boolean dynamic = false;
	private volatile ServiceRegistration registration;
	private boolean initLazy = true;
	private long initTimeout = 0;
	private final Object monitor = new Object();

	public void afterPropertiesSet() throws Exception {
		Assert.hasText(persistentId, "persistentId property is required");
		Assert.notNull(bundleContext, "bundleContext property is required");
		Assert.isTrue(initTimeout >= 0, "a positive initTimeout is required");

		if (!initLazy) {
			createProperties();
		}
	}

	public void destroy() throws Exception {
		OsgiServiceUtils.unregisterService(registration);
		registration = null;
	}

	private void createProperties() {
		if (properties == null) {
			properties = (dynamic ? new ChangeableProperties() : new Properties());
			// init properties by copying config admin properties
			try {
				PropertiesUtil.initProperties(localProperties, localOverride, CMUtils.getConfiguration(bundleContext,
						persistentId, initTimeout), properties);
			} catch (IOException ioe) {
				throw new BeanInitializationException("Cannot retrieve configuration for pid=" + persistentId, ioe);
			}

			if (dynamic) {
				// perform eager registration
				registration = CMUtils.registerManagedService(bundleContext, new ConfigurationWatcher(), persistentId);
			}
		}
	}

	public Properties getObject() throws Exception {
		// perform lazy initialization (if needed)
		if (properties == null) {
			synchronized (monitor) {
				if (properties == null) {
					createProperties();
				}
			}
		}

		return properties;
	}

	public Class<? extends Properties> getObjectType() {
		return (dynamic ? ChangeableProperties.class : Properties.class);
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Returns the persistentId.
	 * 
	 * @return Returns the persistentId
	 */
	public String getPersistentId() {
		return persistentId;
	}

	/**
	 * Sets the ConfigurationAdmin persistent Id that the bean should read.
	 * 
	 * @param persistentId The persistentId to set.
	 */
	public void setPersistentId(String persistentId) {
		this.persistentId = persistentId;
	}

	/**
	 * Sets the local properties, e.g. via the nested tag in XML bean definitions. These can be considered defaults, to
	 * be overridden by properties loaded from the Configuration Admin.
	 */
	public void setProperties(Properties properties) {
		this.localProperties = properties;
	}

	/**
	 * Sets whether local properties override properties from files. <p> Default is "false": Properties from the
	 * Configuration Admin override local defaults. Can be switched to "true" to let local properties override the
	 * Configuration Admin properties.
	 */
	public void setLocalOverride(boolean localOverride) {
		this.localOverride = localOverride;
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	/**
	 * Indicates whether the returned properties object is dynamic or not.
	 * 
	 * @return boolean indicating if the configuration object is dynamic
	 */
	public boolean isDynamic() {
		return dynamic;
	}

	/**
	 * Indicates if the returned configuration is dynamic or static. A static configuration (default) ignores any
	 * updates made to the configuration admin entry that it maps. A dynamic configuration on the other hand will
	 * reflect the changes in its content. Third parties can be notified through the
	 * {@link ServicePropertiesChangeListener} contract.
	 * 
	 * @param dynamic whether the returned object reflects the changes in the configuration admin or not.
	 */
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	/**
	 * Specifies whether the properties reflecting the Configuration Admin service entry will be initialized lazy or
	 * not. Default is "true": meaning the properties will be initialized just before being requested (from the factory)
	 * for the first time. This is the common case as it allows the most recent entry to be used. If set to "false", the
	 * properties object will be initialized at startup, along with the bean factory.
	 * 
	 * @param initLazy whether or not the bean is lazily initialized
	 */
	public void setInitLazy(boolean initLazy) {
		this.initLazy = initLazy;
	}

	/**
	 * Specifies the amount of time (in milliseconds) the bean factory will wait for the Configuration Admin entry to be
	 * initialized (return a non-null value). If the entry is not null at startup, no waiting will be performed. Similar
	 * to the other timeout options, a value of '0' means no waiting. By default, no waiting (0) is performed.
	 * 
	 * @param initTimeout the amount of time to wait for the entry to be initialized.
	 */
	public void setInitTimeout(long initTimeout) {
		this.initTimeout = initTimeout;
	}
}