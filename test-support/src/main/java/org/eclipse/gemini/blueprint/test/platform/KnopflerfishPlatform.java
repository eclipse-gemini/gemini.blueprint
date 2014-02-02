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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.eclipse.gemini.blueprint.test.internal.util.IOUtils;
import org.knopflerfish.framework.FrameworkFactoryImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Knopflerfish 2.0.4+/3.x Platform. Automatically detects the available version on the class path and uses the
 * appropriate means to configure and instantiate it.
 * 
 * @author Costin Leau
 */
public class KnopflerfishPlatform extends AbstractOsgiPlatform {

	private static class KF2Platform implements Platform {
		private static final Class<?> BOOT_CLASS;
		private static final Constructor<?> CONSTRUCTOR;
		private static final Method LAUNCH;
		private static final Method GET_BUNDLE_CONTEXT;
		private static final Method SHUTDOWN;

		static {
			BOOT_CLASS = ClassUtils.resolveClassName(KF_2X_BOOT_CLASS, KF2Platform.class.getClassLoader());

			try {
				CONSTRUCTOR = BOOT_CLASS.getDeclaredConstructor(Object.class);
			} catch (NoSuchMethodException nsme) {
				throw new IllegalArgumentException("Invalid framework class", nsme);
			}

			LAUNCH = BeanUtils.findDeclaredMethod(BOOT_CLASS, "launch", new Class[] { long.class });
			GET_BUNDLE_CONTEXT =
					org.springframework.util.ReflectionUtils.findMethod(BOOT_CLASS, "getSystemBundleContext");
			SHUTDOWN = org.springframework.util.ReflectionUtils.findMethod(BOOT_CLASS, "shutdown");
		}

		private final Object monitor;
		private Object framework;

		KF2Platform(Object monitor) {
			this.monitor = monitor;

		}

		public BundleContext start() {
			framework = BeanUtils.instantiateClass(CONSTRUCTOR, monitor);
			ReflectionUtils.invokeMethod(LAUNCH, framework, 0);
			return (BundleContext) ReflectionUtils.invokeMethod(GET_BUNDLE_CONTEXT, framework);
		}

		public void stop() {
			if (framework != null) {
				ReflectionUtils.invokeMethod(SHUTDOWN, framework);
				framework = null;
			}
		}
	}

	private static class KF3Platform implements Platform {
		private Bundle framework;
		private final Map<String, String> properties;
		private final Log log;
		private FrameworkTemplate fwkTemplate;

		KF3Platform(Map<String, String> properties, Log log) {
			this.properties = properties;
			this.log = log;
		}

		public BundleContext start() {
			framework = new FrameworkFactoryImpl().newFramework(properties);
			fwkTemplate = new DefaultFrameworkTemplate(framework, log);
			fwkTemplate.init();
			fwkTemplate.start();

			return framework.getBundleContext();
		}

		public void stop() {
			if (fwkTemplate != null) {
				fwkTemplate.stopAndWait(1000);
				fwkTemplate = null;
			}
		}
	}

	private static final String KF_2X_BOOT_CLASS = "org.knopflerfish.framework.Framework";
	private static final boolean KF_2X =
			ClassUtils.isPresent(KF_2X_BOOT_CLASS, KnopflerfishPlatform.class.getClassLoader());

	private BundleContext context;
	private Platform framework;
	private File kfStorageDir;

	public KnopflerfishPlatform() {
		toString = "Knopflerfish OSGi Platform";
	}

	Properties getPlatformProperties() {
		if (kfStorageDir == null) {
			kfStorageDir = createTempDir("kf");
			kfStorageDir.deleteOnExit();
			if (log.isDebugEnabled())
				log.debug("KF temporary storage dir is " + kfStorageDir.getAbsolutePath());

		}

		// default properties
		Properties props = new Properties();
		props.setProperty("org.osgi.framework.dir", kfStorageDir.getAbsolutePath());
		props.setProperty("org.knopflerfish.framework.bundlestorage", "file");
		props.setProperty("org.knopflerfish.framework.bundlestorage.file.reference", "true");
		props.setProperty("org.knopflerfish.framework.bundlestorage.file.unpack", "true");
		props.setProperty("org.knopflerfish.startlevel.use", "true");
		props.setProperty("org.knopflerfish.osgi.setcontextclassloader", "true");
		// embedded mode
		props.setProperty("org.knopflerfish.framework.exitonshutdown", "false");
		// disable patch CL
		props.setProperty("org.knopflerfish.framework.patch", "false");
		// new in KF 2.0.4 - automatically exports system packages based on the JRE version
		props.setProperty("org.knopflerfish.framework.system.export.all", "true");
		// props.setProperty("org.knopflerfish.framework.system.export.all_15", "true");
		// add strict bootpath delegation (introduced in KF 2.3.0)
		// since otherwise classes will be loaded from the booth classpath
		// when generating JDK proxies instead of the OSGi space
		// since KF thinks that a non-OSGi class is making the call.
		props.setProperty("org.knopflerfish.framework.strictbootclassloading", "true");

		return props;
	}

	public BundleContext getBundleContext() {
		return context;
	}

	public void start() throws Exception {
		if (framework == null) {
			// copy configuration properties to sys properties
			System.getProperties().putAll(getConfigurationProperties());
            Map<String, String> props = new HashMap<String, String>();
            CollectionUtils.mergePropertiesIntoMap(getPlatformProperties(), props);
			framework = (KF_2X ? new KF2Platform(this) : new KF3Platform(props, log));
			context = framework.start();
		}
	}

	public void stop() throws Exception {
		if (framework != null) {
			context = null;
			try {
				framework.stop();
			} finally {
				framework = null;
				IOUtils.delete(kfStorageDir);
			}
		}
	}
}