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

package org.eclipse.gemini.blueprint.extender.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.Filter;
import org.springframework.context.ApplicationContext;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyEvent;
import org.springframework.util.Assert;

/**
 * Spring-DM Extender bootstrapping event. Used during the application context discovery phase, before an application
 * context is fully initialized. Similar to {@link BootstrappingDependencyEvent}, this event contains the information
 * regarding all unsatisfied dependencies.
 * 
 * Consider using this event {@link BootstrappingDependencyEvent} for getting a global overview of the waiting
 * application and {@link BootstrappingDependencyEvent} for finding out specific information.
 * 
 * <p/> It can be used to receive status updates for contexts started by the extender.
 * 
 * @author Costin Leau
 */
public class BootstrappingDependenciesEvent extends OsgiBundleApplicationContextEvent {

	private final Collection<OsgiServiceDependencyEvent> dependencyEvents;
	private final Collection<String> dependencyFilters;
	private final Filter dependenciesFilter;
	private final long timeLeft;

	/**
	 * Constructs a new <code>BootstrappingDependencyEvent</code> instance.
	 * 
	 * @param source
	 */
	public BootstrappingDependenciesEvent(ApplicationContext source, Bundle bundle,
			Collection<OsgiServiceDependencyEvent> nestedEvents, Filter filter, long timeLeft) {
		super(source, bundle);
		Assert.notNull(nestedEvents);
		this.dependencyEvents = nestedEvents;
		this.dependenciesFilter = filter;
		this.timeLeft = timeLeft;

		List<String> depFilters = new ArrayList<String>(dependencyEvents.size());

		for (OsgiServiceDependencyEvent dependency : nestedEvents) {
			depFilters.add(dependency.getServiceDependency().getServiceFilter().toString());
		}

		dependencyFilters = Collections.unmodifiableCollection(depFilters);
	}

	/**
	 * Returns the nested, dependency event that caused the bootstrapping event to be raised.
	 * 
	 * @return associated dependency event
	 */
	public Collection<OsgiServiceDependencyEvent> getDependencyEvents() {
		return dependencyEvents;
	}

	public Filter getDependenciesAsFilter() {
		return dependenciesFilter;
	}

	public Collection<String> getDependencyFilters() {
		return dependencyFilters;
	}

	public long getTimeToWait() {
		return timeLeft;
	}
}