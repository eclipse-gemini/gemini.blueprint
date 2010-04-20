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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.StringMap;
import org.apache.felix.main.AutoProcessor;
import org.apache.felix.main.Main;
import org.eclipse.gemini.blueprint.test.internal.util.IOUtils;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;

/**
 * Apache Felix (1.0.3+/1.4.x+/2.0.x) OSGi platform. Automatically detects the available version on the classpath and
 * uses the appropriate means to configure and instantiate it.
 * 
 * @author Costin Leau
 */
public class FelixPlatform extends AbstractOsgiPlatform {

	private static abstract class Felix1XPlatform implements Platform {
		private static final Constructor<?> CTOR;

		static {
			Class<?> autoActivator =
					ClassUtils.resolveClassName("org.apache.felix.main.AutoActivator", Felix1XPlatform.class
							.getClassLoader());
			try {
				CTOR = autoActivator.getConstructor(Map.class);
			} catch (Exception ex) {
				throw new IllegalStateException("Cannot instantiate class " + autoActivator, ex);
			}
		}

		private Felix felix;

		public final BundleContext start() throws Exception {
			// load properties
			Map<Object, Object> configMap = getConfiguration();

			// pass the auto activator as a list
			List<Object> list = new ArrayList<Object>(1);
			list.add(BeanUtils.instantiateClass(CTOR, configMap));

			felix = createFelix(configMap, list);
			felix.start();
			return felix.getBundleContext();
		}

		public final void stop() throws Exception {
			felix.stop();
		}

		abstract Felix createFelix(Map<Object, Object> configMap, List<?> activators) throws Exception;
	}

	private static class Felix10XPlatform extends Felix1XPlatform {

		private static final Constructor<Felix> CTOR;
		static {
			try {
				CTOR = Felix.class.getConstructor(new Class<?>[] { Map.class, List.class });
			} catch (NoSuchMethodException ex) {
				throw new IllegalStateException("Cannot find Felix constructor", ex);
			}
		}

		@Override
		Felix createFelix(Map<Object, Object> configMap, List<?> activators) throws Exception {

			return CTOR.newInstance(new Object[] { configMap, activators });
		}
	}

	private static class Felix14XPlatform extends Felix1XPlatform {

		@Override
		Felix createFelix(Map<Object, Object> configMap, List<?> activators) throws Exception {
			configMap.put("felix.systembundle.activators", activators);
			return new Felix(configMap);
		}
	}

	private static class Felix20XPlatform implements Platform {
		private FrameworkTemplate fwkTemplate;
		private final Log log;

		Felix20XPlatform(Log log) {
			this.log = log;
		}

		public BundleContext start() throws Exception {
			Map<Object, Object> configMap = getConfiguration();
			Felix fx = new Felix(configMap);
			fwkTemplate = new DefaultFrameworkTemplate(fx, log);

			fwkTemplate.init();
			BundleContext context = fx.getBundleContext();
			AutoProcessor.process(configMap, context);
			fwkTemplate.start();
			return context;
		}

		public void stop() throws Exception {
			fwkTemplate.stopAndWait(1000);
		}
	}

	private static enum FelixVersion {
		V_10X, V_14X, V_20X
	}

	private static FelixVersion FELIX_VERSION;

	private static final Log log = LogFactory.getLog(FelixPlatform.class);

	private static final String FELIX_PROFILE_DIR_PROPERTY = "felix.cache.profiledir";
	/** new property in 1.4.0 replacing cache.profiledir */
	private static final String OSGI_STORAGE_PROPERTY = "org.osgi.framework.storage";

	static {
		ClassLoader loader = Felix.class.getClassLoader();
		// detect available Felix version
		if (ClassUtils.isPresent("org.apache.felix.main.AutoProcessor", loader)) {
			FELIX_VERSION = FelixVersion.V_20X;
		} else {
			if (ClassUtils.isPresent("org.apache.felix.main.RegularBundleInfo", loader)) {
				FELIX_VERSION = FelixVersion.V_14X;
			}
			FELIX_VERSION = FelixVersion.V_10X;
		}
	}

	private BundleContext context;
	private File felixStorageDir;
	private Platform platform;

	public FelixPlatform() {
		toString = "Felix OSGi Platform";
	}

	Properties getPlatformProperties() {
		// load Felix configuration
		Properties props = new Properties();
		createStorageDir(props);
		// disable logging
		props.put("felix.log.level", "0");

		// use embedded mode
		props.put("felix.embedded.execution", "true");
		return props;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	/**
	 * Configuration settings for the OSGi test run.
	 * 
	 * @return
	 */
	private void createStorageDir(Properties configProperties) {
		// create a temporary file if none is set
		if (felixStorageDir == null) {
			felixStorageDir = createTempDir("felix");
			felixStorageDir.deleteOnExit();

			if (log.isTraceEnabled())
				log.trace("Felix storage dir is " + felixStorageDir.getAbsolutePath());
		}

		configProperties.setProperty(FELIX_PROFILE_DIR_PROPERTY, this.felixStorageDir.getAbsolutePath());
		configProperties.setProperty(OSGI_STORAGE_PROPERTY, this.felixStorageDir.getAbsolutePath());
		configProperties.setProperty("org.osgi.framework.storage.clean", "onFirstInit");
	}

	public void start() throws Exception {
		if (platform == null) {
			// initialize properties and set them as system wide so Felix can pick them up
			Map<Object, Object> configProperties = getConfigurationProperties();
			System.getProperties().putAll(configProperties);

			switch (FELIX_VERSION) {
			case V_20X:
				platform = new Felix20XPlatform(null);
				break;
			case V_14X:
				platform = new Felix14XPlatform();
				break;
			// fallback to 10-12 version
			default:
				platform = new Felix10XPlatform();
				break;
			}

			context = platform.start();
		}
	}

	/**
	 * Returns the platform configuration for creating a Felix instance. Uses Felix classes to load and process the
	 * system properties.
	 * 
	 * @return Felix configuration
	 */
	private static Map<Object, Object> getConfiguration() {
		// Load system properties.
		Main.loadSystemProperties();

		// Read configuration properties.
		Properties configProps = Main.loadConfigProperties();

		if (configProps == null) {
			configProps = new Properties();
		}

		// Copy framework properties from the system properties.
		Main.copySystemProperties(configProps);

		// Create a (Felix specific) case-insensitive property map
		return new StringMap(configProps, false);
	}

	public void stop() throws Exception {
		if (platform != null) {
			try {
				platform.stop();
			} finally {
				context = null;
				platform = null;
				// remove cache folder
				IOUtils.delete(felixStorageDir);
			}
		}
	}
}