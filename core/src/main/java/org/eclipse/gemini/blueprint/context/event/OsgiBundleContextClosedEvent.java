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

package org.eclipse.gemini.blueprint.context.event;

import org.osgi.framework.Bundle;
import org.springframework.context.ApplicationContext;

/**
 * Event raised when an <tt>ApplicationContext#close()</tt> method executes
 * inside an OSGi bundle.
 *
 * @author Andy Piper
 */
public class OsgiBundleContextClosedEvent extends OsgiBundleApplicationContextEvent {

	private Throwable cause;

	/**
	 * Constructs a new <code>OsgiBundleContextClosedEvent</code> instance.
	 *
	 * @param source the <code>ApplicationContext</code> that has been closed (must
	 * not be <code>null</code>)
	 * @param bundle the OSGi bundle associated with the source application
	 * context
	 * @param cause optional <code>Throwable</code> indicating the cause of
	 * the failure
	 */
	public OsgiBundleContextClosedEvent(ApplicationContext source, Bundle bundle, Throwable cause) {
		super(source, bundle);
		this.cause = cause;
	}

    /**
     * Constructs a new <code>OsgiBundleContextClosedEvent</code> instance.
     *
     * @param source event source
     * @param bundle associated OSGi bundle
     */
    public OsgiBundleContextClosedEvent(ApplicationContext source, Bundle bundle) {
        super(source, bundle);
    }

	/**
	 * Returns the <code>Throwable</code> that caused the application context
	 * closure to fail.
	 *
	 * @return the cause of the failure.
	 */
	public final Throwable getFailureCause() {
		return cause;
	}	
}
