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

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.GenericTypeResolver;

/**
 * Listener dispatching OSGi events to interested listeners. This class acts mainly as an adapter bridging the
 * {@link ApplicationListener} interface with {@link OsgiBundleApplicationContextListener}.
 * 
 * @author Costin Leau
 * 
 */
class ApplicationListenerAdapter<E extends OsgiBundleApplicationContextEvent> implements SmartApplicationListener {

	private final OsgiBundleApplicationContextListener<E> osgiListener;
	private final Class<?> eventType;
	private final String toString;

	static <E extends OsgiBundleApplicationContextEvent> ApplicationListenerAdapter<E> createAdapter(
			OsgiBundleApplicationContextListener<E> listener) {
		return new ApplicationListenerAdapter<E>(listener);
	}

	private ApplicationListenerAdapter(OsgiBundleApplicationContextListener<E> listener) {
		this.osgiListener = listener;
		Class<?> evtType =
				GenericTypeResolver
						.resolveTypeArgument(listener.getClass(), OsgiBundleApplicationContextListener.class);
		this.eventType = (evtType == null ? OsgiBundleApplicationContextEvent.class : evtType);

		toString = "ApplicationListenerAdapter for listener " + osgiListener;
	}

	@SuppressWarnings("unchecked")
	public void onApplicationEvent(ApplicationEvent event) {
		if (eventType.isInstance(event)) {
			osgiListener.onOsgiApplicationEvent((E) event);
		}
	}

	public boolean equals(Object obj) {
		return osgiListener.equals(obj);
	}

	public int hashCode() {
		return osgiListener.hashCode();
	}

	public String toString() {
		return toString;
	}

	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return (eventType != null && eventType.isAssignableFrom(eventType));
	}

	public boolean supportsSourceType(Class<?> sourceType) {
		return true;
	}

	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}
}