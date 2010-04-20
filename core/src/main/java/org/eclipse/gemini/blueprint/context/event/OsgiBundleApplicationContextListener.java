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

import java.util.EventListener;

/**
 * OSGi specific listener interested in notifications regarding the life cycle of OSGi application contexts.
 * 
 * <p/> Note that this listener is normally used for distributing events to entities outside the source application
 * context. For events sent inside the application context, consider using an
 * {@link org.springframework.context.ApplicationListener}.
 * 
 * @see OsgiBundleContextRefreshedEvent
 * @see OsgiBundleContextFailedEvent
 * @see OsgiBundleContextClosedEvent
 * 
 * @author Costin Leau
 */
public interface OsgiBundleApplicationContextListener<E extends OsgiBundleApplicationContextEvent> extends
		EventListener {

	/**
	 * Handles an OSGi application event.
	 * 
	 * @param event OSGi application event
	 */
	void onOsgiApplicationEvent(E event);
}
