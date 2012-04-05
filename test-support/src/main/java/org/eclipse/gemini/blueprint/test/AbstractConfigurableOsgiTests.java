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

package org.eclipse.gemini.blueprint.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.gemini.blueprint.test.platform.EquinoxPlatform;
import org.eclipse.gemini.blueprint.test.platform.OsgiPlatform;
import org.eclipse.gemini.blueprint.test.platform.Platforms;
import org.osgi.framework.Constants;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Abstract JUnit super class which configures an {@link OsgiPlatform}. <p/>
 * This class offers more hooks for programmatic and declarative configuration
 * of the underlying OSGi platform used when running the test suite.
 * 
 * @author Costin Leau
 * 
 */
public abstract class AbstractConfigurableOsgiTests extends AbstractOsgiTests {

	/**
	 * 
	 * Default constructor. Constructs a new
	 * <code>AbstractConfigurableOsgiTests</code> instance.
	 */
	public AbstractConfigurableOsgiTests() {
		super();
	}

	/**
	 * Constructs a new <code>AbstractConfigurableOsgiTests</code> instance.
	 * 
	 * @param name test name
	 */
	public AbstractConfigurableOsgiTests(String name) {
		super(name);
	}


	/**
	 * System property for selecting the appropriate OSGi implementation.
	 */
	public static final String OSGI_FRAMEWORK_SELECTOR = "org.eclipse.gemini.blueprint.test.framework";

	/**
	 * {@inheritDoc}
	 * 
	 * <p/>This implementation determines and creates the OSGi platform used by
	 * the test suite (Equinox by default). It will try to create a Platform
	 * instance based on the <code>getPlatformName</code>, falling back to
	 * Equinox in case of a failure.
	 * 
	 * @see #getPlatformName() for an easier alternative.
	 */
	protected OsgiPlatform createPlatform() {
		boolean trace = logger.isTraceEnabled();
		String platformClassName = getPlatformName();

		OsgiPlatform platform = null;
		ClassLoader currentCL = getClass().getClassLoader();

		if (StringUtils.hasText(platformClassName)) {
			if (ClassUtils.isPresent(platformClassName, currentCL)) {
				Class<?> platformClass = ClassUtils.resolveClassName(platformClassName, currentCL);
				if (OsgiPlatform.class.isAssignableFrom(platformClass)) {
					if (trace)
						logger.trace("Instantiating platform wrapper...");
					try {
						platform = (OsgiPlatform) platformClass.newInstance();
					}
					catch (Exception ex) {
						logger.warn("cannot instantiate class [" + platformClass + "]; using default");
					}
				}
				else
					logger.warn("Class [" + platformClass + "] does not implement " + OsgiPlatform.class.getName()
							+ " interface; falling back to defaults");
			}
			else {
				logger.warn("OSGi platform starter [" + platformClassName + "] not found; using default");
			}

		}
		else
			logger.trace("No platform specified; using default");

		// fall back
		if (platform == null)
			platform = new EquinoxPlatform();

		Properties config = platform.getConfigurationProperties();
		// add boot delegation
		config.setProperty(Constants.FRAMEWORK_BOOTDELEGATION,
			getBootDelegationPackageString());

		return platform;
	}

	/**
	 * Indicates what OSGi platform should be used by the test suite. By
	 * default, {@link #OSGI_FRAMEWORK_SELECTOR} system property is used.
	 * Subclasses can override this and provide directly the OSGi platform name.
	 * By default, the platform name holds the fully qualified name of the OSGi
	 * platform class.
	 * 
	 * @return platform platform name
	 * @see Platforms
	 */
	protected String getPlatformName() {
		String systemProperty = System.getProperty(OSGI_FRAMEWORK_SELECTOR);
		if (logger.isTraceEnabled())
			logger.trace("System property [" + OSGI_FRAMEWORK_SELECTOR + "] has value=" + systemProperty);

		return (!StringUtils.hasText(systemProperty) ? Platforms.EQUINOX : systemProperty);
	}

	/**
	 * Returns a String representation of the boot delegation packages list.
	 * 
	 * @return boot delegation path
	 */
	private String getBootDelegationPackageString() {
		StringBuilder buf = new StringBuilder();

		for (Iterator iter = getBootDelegationPackages().iterator(); iter.hasNext();) {
			buf.append(((String) iter.next()).trim());
			if (iter.hasNext()) {
				buf.append(",");
			}
		}

		return buf.toString();
	}

	/**
	 * Returns the list of OSGi packages that are delegated to the boot
	 * classpath. See the OSGi specification regarding the format of the package
	 * string representation.
	 * 
	 * @return the list of strings representing the packages that the OSGi
	 * platform will delegate to the boot class path.
	 */
	protected List getBootDelegationPackages() {
		List defaults = new ArrayList();
		// javax packages
		defaults.add("javax.*");
		// XML API available in JDK 1.4
		defaults.add("org.w3c.*");
		defaults.add("org.xml.*");

		// sun packages
		defaults.add("sun.*");
		defaults.add("com.sun.*");

		// FIXME: the JAXP package (for 1.4 VMs) should be discovered in an OSGi
		// manner
		defaults.add("org.apache.xerces.jaxp.*");
		return defaults;
	}
}