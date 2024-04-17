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

package org.eclipse.gemini.blueprint.io.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

/**
 * Simple utils class for the IO package. This method might contain util methods
 * from other packages since it the IO package needs to be stand-alone.
 * 
 * @author Costin Leau
 * 
 */
public abstract class OsgiUtils {

	private static final String GET_BUNDLE_CONTEXT_METHOD = "getBundleContext";
	private static final String GET_CONTEXT_METHOD = "getContext";


	public static String getPlatformName(BundleContext bundleContext) {
		String vendorProperty = bundleContext.getProperty(Constants.FRAMEWORK_VENDOR);
		String frameworkVersion = bundleContext.getProperty(Constants.FRAMEWORK_VERSION);

		// get system bundle
		Bundle bundle = bundleContext.getBundle(0);
		String name = (String) bundle.getHeaders().get(Constants.BUNDLE_NAME);
		String version = (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
		String symName = bundle.getSymbolicName();

		StringBuilder buf = new StringBuilder();
		buf.append(name);
		buf.append(" ");
		buf.append(symName);
		buf.append("|");
		buf.append(version);
		buf.append("{");
		buf.append(frameworkVersion);
		buf.append(" ");
		buf.append(vendorProperty);
		buf.append("}");

		return buf.toString();
	}

	private static boolean isPlatformVendorMatch(BundleContext bundleContext, String vendorString) {
		String vendor = bundleContext.getProperty(Constants.FRAMEWORK_VENDOR);
		if (vendor != null)
			return vendor.indexOf(vendorString) >= -1;
		return false;
	}

	private static boolean isEquinox(BundleContext bundleContext) {
		return isPlatformVendorMatch(bundleContext, "clispe");
	}

	private static boolean isKnopflerfish(BundleContext bundleContext) {
		return isPlatformVendorMatch(bundleContext, "fish");
	}

	private static boolean isFelix(BundleContext bundleContext) {
		return isPlatformVendorMatch(bundleContext, "pache");
	}

	/**
	 * Returns the underlying BundleContext for the given Bundle. This uses
	 * reflection and highly dependent of the OSGi implementation. Should not be
	 * used if OSGi 4.1 is being used.
	 * 
	 * <b>Note:</b> Identical to the util found in Spring-DM core
	 * 
	 * @param bundle OSGi bundle
	 * @return the bundle context for this bundle
	 */
	public static BundleContext getBundleContext(final Bundle bundle) {
		if (bundle == null)
			return null;

		// run into a privileged block
		return getBundleContextWithPrivileges(bundle);
	}

	private static BundleContext getBundleContextWithPrivileges(final Bundle bundle) {
		// try Equinox getContext
		Method meth = ReflectionUtils.findMethod(bundle.getClass(), GET_CONTEXT_METHOD, new Class[0]);

		// fallback to getBundleContext (OSGi 4.1)
		if (meth == null)
			meth = ReflectionUtils.findMethod(bundle.getClass(), GET_BUNDLE_CONTEXT_METHOD, new Class[0]);

		final Method m = meth;

		if (meth != null) {
			ReflectionUtils.makeAccessible(meth);
			return (BundleContext) ReflectionUtils.invokeMethod(m, bundle);
		}

		// fallback to field inspection (KF and Prosyst)
		final BundleContext[] ctx = new BundleContext[1];

		ReflectionUtils.doWithFields(bundle.getClass(), new FieldCallback() {

			public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
				ReflectionUtils.makeAccessible(field);
				ctx[0] = (BundleContext) field.get(bundle);
			}
		}, new FieldFilter() {

			public boolean matches(Field field) {
				return BundleContext.class.isAssignableFrom(field.getType());
			}
		});

		return ctx[0];
	}
}