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

package org.eclipse.gemini.blueprint.context.event;

import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.osgi.framework.Bundle;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.util.Assert;

/**
 * Base class for events raised for an <code>ApplicationContext</code> created
 * inside an OSGi environment. Normally, events of this type are raised by the
 * OSGi extender to notify 3rd parties, external to the context, about changes
 * in the life cycle of the application context.
 * 
 * <p/><b>Note:</b>While the context source is likely to be an implementation
 * of {@link ConfigurableOsgiBundleApplicationContext}, this is not mandatory
 * (it's entirely possible to have a non-OSGi aware {@link ApplicationContext}
 * implementation).
 * 
 * @author Costin Leau
 */
public abstract class OsgiBundleApplicationContextEvent extends ApplicationContextEvent {

	private final Bundle bundle;


	/**
	 * Constructs a new <code>OsgiApplicationContextEvent</code> instance.
	 * 
	 * @param source the <code>ConfigurableOsgiBundleApplicationContext</code>
	 * that the event is raised for (must not be <code>null</code>)
	 */
	public OsgiBundleApplicationContextEvent(ApplicationContext source, Bundle bundle) {
		super(source);
		Assert.notNull(bundle, "bundle is required");
		this.bundle = bundle;
	}

	/**
	 * Returns the OSGi {@link Bundle} associated with the application context
	 * that triggers the event.
	 * 
	 * @return associated OSGi bundle
	 */
	public Bundle getBundle() {
		return bundle;
	}
}
