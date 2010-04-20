/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.extender.internal.dependencies.startup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.eclipse.gemini.blueprint.service.importer.DefaultOsgiServiceDependency;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceDependency;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.springframework.util.ObjectUtils;

/**
 * Holder/helper class representing an OSGi service dependency
 * 
 * @author Costin Leau
 * @author Hal Hildebrand
 * @author Andy Piper
 */
class MandatoryServiceDependency implements OsgiServiceDependency {
	// match the class inside object class (and use a non backing reference group)
	private static final Pattern PATTERN = Pattern.compile("objectClass=(?:[^\\)]+)");

	protected final BundleContext bundleContext;

	private OsgiServiceDependency serviceDependency;
	private final AtomicInteger matchingServices = new AtomicInteger(0);
	protected final String filterAsString;
	private final String[] classes;

	MandatoryServiceDependency(BundleContext bc, Filter serviceFilter, boolean isMandatory, String beanName) {
		this(bc, new DefaultOsgiServiceDependency(beanName, serviceFilter, isMandatory));
	}

	MandatoryServiceDependency(BundleContext bc, OsgiServiceDependency dependency) {
		bundleContext = bc;
		serviceDependency = dependency;
		this.filterAsString = dependency.getServiceFilter().toString();
		this.classes = extractObjectClassFromFilter(filterAsString);
	}

	boolean matches(ServiceEvent event) {
		return serviceDependency.getServiceFilter().match(event.getServiceReference());
	}

	boolean isServicePresent() {
		return (!serviceDependency.isMandatory() || OsgiServiceReferenceUtils.isServicePresent(bundleContext,
				filterAsString));
	}

	public String toString() {
		return "Dependency on [" + filterAsString + "] (from bean [" + serviceDependency.getBeanName() + "])";
	}

	public Filter getServiceFilter() {
		return serviceDependency.getServiceFilter();
	}

	public String getBeanName() {
		return serviceDependency.getBeanName();
	}

	public boolean isMandatory() {
		return serviceDependency.isMandatory();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final MandatoryServiceDependency that = (MandatoryServiceDependency) o;

		return (serviceDependency.equals(that.serviceDependency));
	}

	public int hashCode() {
		int result = MandatoryServiceDependency.class.hashCode();
		result = 29 * result + serviceDependency.hashCode();
		return result;
	}

	public OsgiServiceDependency getServiceDependency() {
		return serviceDependency;
	}

	/**
	 * Adds another matching service.
	 * 
	 * @return the counter after adding the service.
	 */
	int increment() {
		return matchingServices.incrementAndGet();
	}

	/**
	 * Removes a matching service.
	 * 
	 * @return the counter after substracting the service.
	 */
	int decrement() {
		return matchingServices.decrementAndGet();
	}

	private static String[] extractObjectClassFromFilter(String filterString) {
		List<String> matches = null;
		Matcher matcher = PATTERN.matcher(filterString);
		while (matcher.find()) {
			if (matches == null) {
				matches = new ArrayList<String>(4);
			}

			matches.add(matcher.group());
		}

		return (matches == null ? new String[0] : matches.toArray(new String[matches.size()]));
	}
}