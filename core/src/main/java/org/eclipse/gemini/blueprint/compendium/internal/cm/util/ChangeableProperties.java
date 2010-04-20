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

package org.eclipse.gemini.blueprint.compendium.internal.cm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.gemini.blueprint.service.exporter.support.ServicePropertiesChangeEvent;
import org.eclipse.gemini.blueprint.service.exporter.support.ServicePropertiesChangeListener;
import org.eclipse.gemini.blueprint.service.exporter.support.ServicePropertiesListenerManager;

/**
 * Basic implementation of {@link ServicePropertiesChangeListener}.
 * 
 * @author Costin Leau
 */
public class ChangeableProperties extends Properties implements ServicePropertiesListenerManager {

	private List<ServicePropertiesChangeListener> listeners =
			Collections.synchronizedList(new ArrayList<ServicePropertiesChangeListener>(4));

	public void addListener(ServicePropertiesChangeListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void removeListener(ServicePropertiesChangeListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	public void notifyListeners() {
		ServicePropertiesChangeEvent event = new ServicePropertiesChangeEvent(this);
		synchronized (listeners) {
			for (ServicePropertiesChangeListener listener : listeners) {
				listener.propertiesChange(event);
			}
		}
	}
}
