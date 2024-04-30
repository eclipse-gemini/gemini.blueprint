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

import org.osgi.framework.Bundle;
import org.springframework.context.ApplicationContext;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyEvent;
import org.springframework.util.Assert;

/**
 * Spring-DM Extender bootstrapping event. This event is used during the
 * application context discovery phase, before an application context is fully
 * initialized.
 * 
 * <p/> It can be used to receive status updates for contexts started by the
 * extender.
 * 
 * @author Costin Leau
 * 
 */
public class BootstrappingDependencyEvent extends OsgiBundleApplicationContextEvent {

	private final OsgiServiceDependencyEvent dependencyEvent;


	/**
	 * Constructs a new <code>BootstrappingDependencyEvent</code> instance.
	 * 
	 * @param source
	 */
	public BootstrappingDependencyEvent(ApplicationContext source, Bundle bundle, OsgiServiceDependencyEvent nestedEvent) {
		super(source, bundle);
		Assert.notNull(nestedEvent, "nestedEvent is required");
		this.dependencyEvent = nestedEvent;
	}

	/**
	 * Returns the nested, dependency event that caused the bootstrapping event
	 * to be raised.
	 * 
	 * @return associated dependency event
	 */
	public OsgiServiceDependencyEvent getDependencyEvent() {
		return dependencyEvent;
	}
}
