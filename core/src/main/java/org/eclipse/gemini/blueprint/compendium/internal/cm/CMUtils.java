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
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.eclipse.gemini.blueprint.util.internal.MapBasedDictionary;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.cm.ManagedService;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.util.StringUtils;

/**
 * Utility class for the Configuration Admin package.
 * 
 * @author Costin Leau
 */
public abstract class CMUtils {

	/**
	 * Injects the properties from the given Map to the given object. Additionally, a bean factory can be passed in for
	 * copying property editors inside the injector.
	 * 
	 * @param instance bean instance to configure
	 * @param properties
	 * @param beanFactory
	 */
	public static void applyMapOntoInstance(Object instance, Map<String, ?> properties, AbstractBeanFactory beanFactory) {
		if (properties != null && !properties.isEmpty()) {
			BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(instance);
			beanWrapper.setAutoGrowNestedPaths(true);

			// configure bean wrapper (using method from Spring 2.5.6)
			if (beanFactory != null) {
				beanFactory.copyRegisteredEditorsTo(beanWrapper);
			}
			for (Iterator<?> iterator = properties.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, ?> entry = (Map.Entry<String, ?>) iterator.next();
				String propertyName = entry.getKey();
				if (beanWrapper.isWritableProperty(propertyName)) {
					beanWrapper.setPropertyValue(propertyName, entry.getValue());
				}
			}
		}
	}

	public static void bulkUpdate(UpdateCallback callback, Collection<?> instances, Map<?, ?> properties) {
		for (Iterator<?> iterator = instances.iterator(); iterator.hasNext();) {
			Object instance = iterator.next();
			callback.update(instance, properties);
		}
	}

	public static UpdateCallback createCallback(boolean autowireOnUpdate, String methodName, BeanFactory beanFactory) {
		UpdateCallback beanManaged = null, containerManaged = null;
		if (autowireOnUpdate) {
			containerManaged = new ContainerManagedUpdate(beanFactory);
		}
		if (StringUtils.hasText(methodName)) {
			beanManaged = new BeanManagedUpdate(methodName);
		}

		// if both strategies are present, return a chain
		if (containerManaged != null && beanManaged != null)
			return new ChainedManagedUpdate(new UpdateCallback[] { containerManaged, beanManaged });

		// otherwise return the non-null one
		return (containerManaged != null ? containerManaged : beanManaged);
	}

	/**
	 * Returns a map containing the Configuration Admin entry with given pid. Waits until a non-null (initialized)
	 * object is returned if initTimeout is bigger then 0.
	 * 
	 * @param bundleContext
	 * @param pid
	 * @param initTimeout
	 * @return
	 * @throws IOException
	 */
	public static Map getConfiguration(BundleContext bundleContext, final String pid, long initTimeout)
			throws IOException {
		ServiceReference ref = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
		if (ref != null) {
			ConfigurationAdmin cm = (ConfigurationAdmin) bundleContext.getService(ref);
			if (cm != null) {
				Dictionary dict = cm.getConfiguration(pid).getProperties();
				// if there are properties or no timeout, return as is
				if (dict != null || initTimeout == 0) {
					return new MapBasedDictionary(dict);
				}
				// no valid props, register a listener and start waiting
				final Object monitor = new Object();
				Dictionary<String, Object> props = new Hashtable<String, Object>();
				props.put(Constants.SERVICE_PID, pid);
				
				ServiceRegistration reg =
						bundleContext.registerService(ConfigurationListener.class.getName(),
								new ConfigurationListener() {
									public void configurationEvent(ConfigurationEvent event) {
										if (ConfigurationEvent.CM_UPDATED == event.getType()
												&& pid.equals(event.getPid())) {
											synchronized (monitor) {
												monitor.notify();
											}
										}
									}
								}, props);

				try {
					// try to get the configuration one more time (in case the update was fired before the service was
					// registered)
					dict = cm.getConfiguration(pid).getProperties();
					if (dict != null) {
						return new MapBasedDictionary(dict);
					}

					// start waiting
					synchronized (monitor) {
						try {
							monitor.wait(initTimeout);
						} catch (InterruptedException ie) {
							// consider the timeout has passed
						}
					}

					// return whatever is available (either we timed out or an update occured)
					return new MapBasedDictionary(cm.getConfiguration(pid).getProperties());

				} finally {
					OsgiServiceUtils.unregisterService(reg);
				}
			}
		}
		return Collections.EMPTY_MAP;
	}

	public static ServiceRegistration registerManagedService(BundleContext bundleContext, ManagedService listener,
			String pid) {

        Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put(Constants.SERVICE_PID, pid);
		Bundle bundle = bundleContext.getBundle();
		props.put(Constants.BUNDLE_SYMBOLICNAME, OsgiStringUtils.nullSafeSymbolicName(bundle));
		props.put(Constants.BUNDLE_VERSION, OsgiBundleUtils.getBundleVersion(bundle));

		return bundleContext.registerService(ManagedService.class.getName(), listener, props);
	}
}