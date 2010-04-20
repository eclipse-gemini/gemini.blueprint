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


/**
 * Interface to be implemented by objects that can manage a number of
 * {@link OsgiBundleApplicationContextListener}s, and publish events to them.
 * 
 * <p/> The contract of this interface is very similar to that of
 * {@link org.springframework.context.event.ApplicationEventMulticaster} except
 * the type of listeners this multicaster can handle. Different from the
 * aforementioned class, this interface is used for broadcasting life cycle
 * events of application contexts started inside an OSGi environment, to outside
 * entities. This normally implies that the entities as well as the multicaster
 * are not managed by the application context triggering the event (so that a
 * destruction event can be properly propagated).
 * 
 * @see org.springframework.context.event.ApplicationEventMulticaster
 * 
 * @author Costin Leau
 */
public interface OsgiBundleApplicationContextEventMulticaster {

	/**
	 * Add an OSGi listener to be notified of all events.
	 * 
	 * @param osgiListener the listener to add
	 */
	void addApplicationListener(OsgiBundleApplicationContextListener osgiListener);

	/**
	 * Remove an OSGi listener from the notification list.
	 * 
	 * @param osgiListener the listener to remove
	 */
	void removeApplicationListener(OsgiBundleApplicationContextListener osgiListener);

	/**
	 * Remove all listeners registered with this multicaster. It will perform no
	 * action on event notification until more listeners are registered.
	 */
	void removeAllListeners();

	/**
	 * Multicast the given application event to appropriate listeners.
	 * 
	 * @param osgiListener the event to multicast
	 */
	void multicastEvent(OsgiBundleApplicationContextEvent osgiListener);
}
