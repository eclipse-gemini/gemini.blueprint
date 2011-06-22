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

package org.eclipse.gemini.blueprint.extender.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.eclipse.gemini.blueprint.service.exporter.OsgiServicePropertiesResolver;

/**
 * Mock bundle useful for testing service dependencies.
 * 
 * @author Costin Leau
 * 
 */
public class DependencyMockBundle extends MockBundle {

	// bundles which depend on the current one
	protected List<Bundle> dependentOn = new ArrayList<Bundle>();

	// bundles on which the current bundle depends on
	protected List<Bundle> dependsOn = new ArrayList<Bundle>();

	private Map<Bundle, ServiceReference> inUseServices = new LinkedHashMap<Bundle, ServiceReference>();

	private Map<Bundle, ServiceReference> registeredServices = new LinkedHashMap<Bundle, ServiceReference>();

	public DependencyMockBundle() {
		super();
	}

	public DependencyMockBundle(BundleContext context) {
		super(context);
	}

	public DependencyMockBundle(Dictionary headers) {
		super(headers);
	}

	public DependencyMockBundle(String location, Dictionary headers, BundleContext context) {
		super(location, headers, context);
	}

	public DependencyMockBundle(String location) {
		super(location);
	}

	private Dictionary createProps(int index, int[] serviceRanking, long[] serviceId) {
		// set Properties
		Dictionary props = new Properties();

		props.put(Constants.SERVICE_RANKING, new Integer((index < serviceRanking.length ? serviceRanking[index]
				: serviceRanking[0])));
		long id = (index < serviceId.length ? serviceId[index] : serviceId[0]);
		if (id >= 0)
			props.put(Constants.SERVICE_ID, new Long(id));

		props.put(OsgiServicePropertiesResolver.SPRING_DM_BEAN_NAME_PROPERTY_KEY, new Long(id));
		props.put(OsgiServicePropertiesResolver.BEAN_NAME_PROPERTY_KEY, new Long(id));

		return props;
	}

	/**
	 * Create one service reference returning the using bundle.
	 * 
	 * @param dependent
	 */
	public void setDependentOn(final Bundle[] dependents, int[] serviceRanking, long[] serviceId) {
		this.dependentOn.addAll(Arrays.asList(dependents));

		for (Bundle dependent : dependents) {
			if (dependent instanceof DependencyMockBundle) {
				((DependencyMockBundle) dependent).dependsOn.add(this);
			}
		}

		// initialise registered services
		registeredServices.clear();

		for (int i = 0; i < dependents.length; i++) {
			registeredServices.put(dependents[i], new MockServiceReference(DependencyMockBundle.this, createProps(i,
					serviceRanking, serviceId), null) {

				public Bundle[] getUsingBundles() {
					return DependencyMockBundle.this.dependentOn.toArray(new Bundle[dependentOn.size()]);
				}
			});
		}
	}

	public void setDependentOn(final Bundle[] dependent, int serviceRanking, long serviceId) {
		setDependentOn(dependent, new int[] { serviceRanking }, new long[] { serviceId });
	}

	public void setDependentOn(final Bundle[] dependent) {
		setDependentOn(dependent, 0, -1);
	}

	public void setDependentOn(Bundle dependent) {
		setDependentOn(new Bundle[] { dependent }, 0, -1);
	}

	public void setDependentOn(Bundle dependent, int serviceRanking, long serviceId) {
		setDependentOn(new Bundle[] { dependent }, serviceRanking, serviceId);
	}

	protected void setDependsOn(Bundle[] depends) {
		this.dependsOn.addAll(Arrays.asList(depends));

		// initialize InUseServices
		inUseServices.clear();

		final Bundle[] usingBundles = new Bundle[] { this };

		for (final Bundle dependencyBundle : dependsOn) {
			// make connection from the opposite side also
			if (dependencyBundle instanceof DependencyMockBundle) {
				((DependencyMockBundle) dependencyBundle).setDependentOn(this);
			}

			Properties props = new Properties();

			props.put(OsgiServicePropertiesResolver.SPRING_DM_BEAN_NAME_PROPERTY_KEY, new Long(System
					.identityHashCode(dependencyBundle)));

			props.put(OsgiServicePropertiesResolver.BEAN_NAME_PROPERTY_KEY, new Long(System
					.identityHashCode(dependencyBundle)));

			inUseServices.put(dependencyBundle, new MockServiceReference() {
				public Bundle getBundle() {
					return dependencyBundle;
				}

				public Bundle[] getUsingBundles() {
					return usingBundles;
				}
			});
		}
	}

	protected void setDependsOn(Bundle depends) {
		setDependsOn(new Bundle[] { depends });
	}

	public ServiceReference[] getRegisteredServices() {
		return registeredServices.values().toArray(new ServiceReference[registeredServices.size()]);
	}

	public ServiceReference[] getServicesInUse() {
		return inUseServices.values().toArray(new ServiceReference[registeredServices.size()]);
	}

	@Override
	public void stop(int options) throws BundleException {
		if (dependentOn != null)
			for (Bundle dependent : dependentOn) {
				if (dependent instanceof DependencyMockBundle) {
					DependencyMockBundle dep = ((DependencyMockBundle) dependent);
					List<Bundle> list = dep.dependsOn;
					if (list != null)
						list.remove(this);
					dep.inUseServices.remove(dependent);
				}
			}
		dependentOn = null;

		if (dependsOn != null)
			for (Bundle dependent : dependsOn) {
				if (dependent instanceof DependencyMockBundle) {
					DependencyMockBundle dep = ((DependencyMockBundle) dependent);
					List<Bundle> list = dep.dependentOn;
					if (list != null)
						list.remove(this);
				}
			}
		dependsOn = null;
		inUseServices.clear();
		registeredServices.clear();
	}
}