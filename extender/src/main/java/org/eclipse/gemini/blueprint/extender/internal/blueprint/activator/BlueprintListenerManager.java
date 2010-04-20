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

package org.eclipse.gemini.blueprint.extender.internal.blueprint.activator;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.blueprint.container.BlueprintListener;
import org.springframework.beans.factory.DisposableBean;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.eclipse.gemini.blueprint.service.importer.support.CollectionType;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;

/**
 * Management class sending notifications to ModuleContextListener services. The class deals with the management of the
 * listener services.
 * 
 * @author Costin Leau
 * 
 */
class BlueprintListenerManager implements BlueprintListener, DisposableBean {

	/** logger */
	private static final Log log = LogFactory.getLog(BlueprintListenerManager.class);

	private volatile DisposableBean cleanupHook;
	private volatile List<BlueprintListener> listeners;
	private volatile ReplayEventManager replayManager;

	private class RegistrationReplayDelivery implements OsgiServiceLifecycleListener {

		public void bind(Object service, Map properties) throws Exception {
			BlueprintListener listener = (BlueprintListener) service;
			replayManager.dispatchReplayEvents(listener);
		}

		public void unbind(Object service, Map properties) throws Exception {
		}
	}

	public BlueprintListenerManager(BundleContext context) {
		this.replayManager = new ReplayEventManager(context);

		OsgiServiceCollectionProxyFactoryBean fb = new OsgiServiceCollectionProxyFactoryBean();
		fb.setBundleContext(context);
		fb.setAvailability(Availability.OPTIONAL);
		fb.setCollectionType(CollectionType.LIST);
		fb.setInterfaces(new Class[] { BlueprintListener.class });
		fb.setBeanClassLoader(BundleDelegatingClassLoader.createBundleClassLoaderFor(context.getBundle()));
		fb.setListeners(new OsgiServiceLifecycleListener[] { new RegistrationReplayDelivery() });
		fb.afterPropertiesSet();

		cleanupHook = fb;
		listeners = (List) fb.getObject();
	}

	public void destroy() {
		replayManager.destroy();

		if (cleanupHook != null) {
			try {
				cleanupHook.destroy();
			} catch (Exception ex) {
				// just log
				log.warn("Cannot destroy listeners collection", ex);
			}
			cleanupHook = null;
		}
	}

	public void blueprintEvent(BlueprintEvent event) {
		replayManager.addEvent(event);

		for (BlueprintListener listener : listeners) {
			try {
				listener.blueprintEvent(event);
			} catch (Exception ex) {
				log.warn("exception encountered when calling listener " + System.identityHashCode(listener), ex);
			}
		}
	}
}