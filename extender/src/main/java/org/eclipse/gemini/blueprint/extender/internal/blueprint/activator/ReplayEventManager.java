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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.blueprint.container.BlueprintListener;

/**
 * Class managing blueprint replay events.
 * 
 * @author Costin Leau
 */
class ReplayEventManager {

	/** logger */
	private static final Log log = LogFactory.getLog(ReplayEventManager.class);

	private final Map<Bundle, BlueprintEvent> events =
			Collections.synchronizedMap(new LinkedHashMap<Bundle, BlueprintEvent>());

	private final BundleContext bundleContext;
	private final BundleListener listener = new BundleListener() {

		public void bundleChanged(BundleEvent event) {
			if (BundleEvent.STOPPED == event.getType() || BundleEvent.UNINSTALLED == event.getType()
					|| BundleEvent.UNRESOLVED == event.getType()) {
				BlueprintEvent removed = events.remove(event.getBundle());
				if (log.isTraceEnabled())
					log.trace("Removed  bundle " + event.getBundle() + " for sending replayes events; last one was "
							+ removed);
			}
		}
	};

	ReplayEventManager(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		bundleContext.addBundleListener(listener);
	}

	void addEvent(BlueprintEvent event) {
		// copy event
		BlueprintEvent replay = new BlueprintEvent(event, true);
		Bundle bnd = replay.getBundle();
		if (bnd.getState() == Bundle.ACTIVE || bnd.getState() == Bundle.STARTING || bnd.getState() == Bundle.STOPPING) {
			events.put(bnd, replay);
			if (log.isTraceEnabled())
				log.trace("Adding replay event  " + replay.getType() + " for bundle " + replay.getBundle());
		} else {
			if (log.isTraceEnabled()) {
				log.trace("Replay event " + replay.getType() + " ignored; " + "owning bundle has been uninstalled "
						+ bnd);
				events.remove(bnd);
			}
		}
	}

	void destroy() {
		events.clear();
		try {
			bundleContext.removeBundleListener(listener);
		} catch (Exception ex) {
			// discard
		}
	}

	void dispatchReplayEvents(BlueprintListener listener) {
		synchronized (events) {
			for (BlueprintEvent event : events.values()) {
				listener.blueprintEvent(event);
			}
		}
	}
}