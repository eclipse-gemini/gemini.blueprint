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

package org.eclipse.gemini.blueprint.extender.internal.activator;

import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.GenericTypeResolver;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Listener interface that delegates to a list of listener. This is useful in OSGi environments when dealing with
 * dynamic collections which can be updated during iteration.
 * 
 * @author Costin Leau
 * 
 */
@SuppressWarnings("unchecked")
class ListListenerAdapter implements OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent>,
		InitializingBean, DisposableBean {

	private final ServiceTracker tracker;
	private final Map<Class<? extends OsgiBundleApplicationContextListener>, Class<? extends OsgiBundleApplicationContextEvent>> eventCache =
			new WeakHashMap<Class<? extends OsgiBundleApplicationContextListener>, Class<? extends OsgiBundleApplicationContextEvent>>();

	/**
	 * Constructs a new <code>ListListenerAdapter</code> instance.
	 */
	public ListListenerAdapter(BundleContext bundleContext) {
		this.tracker = new ServiceTracker(bundleContext, OsgiBundleApplicationContextListener.class.getName(), null);
	}

	public void afterPropertiesSet() {
		this.tracker.open();
	}

	public void destroy() {
		this.tracker.close();
		eventCache.clear();
	}

	public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
		Object[] listeners = tracker.getServices();

		if (listeners != null) {
			synchronized (eventCache) {
				for (Object listnr : listeners) {
					OsgiBundleApplicationContextListener listener = (OsgiBundleApplicationContextListener) listnr;
					Class<? extends OsgiBundleApplicationContextListener> listenerClass = listener.getClass();
					Class<? extends OsgiBundleApplicationContextEvent> eventType = eventCache.get(listenerClass);
					if (eventType == null) {
						Class<?> evtType =
								GenericTypeResolver.resolveTypeArgument(listenerClass,
										OsgiBundleApplicationContextListener.class);
						if (evtType == null) {
							evtType = OsgiBundleApplicationContextEvent.class;
						}
						if (evtType != null && evtType.isAssignableFrom(OsgiBundleApplicationContextEvent.class)) {
							eventType = (Class<? extends OsgiBundleApplicationContextEvent>) evtType;
						} else {
							eventType = OsgiBundleApplicationContextEvent.class;
						}
						eventCache.put(listenerClass, eventType);
					}
					if (eventType.isInstance(event)) {
						listener.onOsgiApplicationEvent(event);
					}
				}
			}
		}
	}
}