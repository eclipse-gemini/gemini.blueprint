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

package org.eclipse.gemini.blueprint.service.exporter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.gemini.blueprint.service.exporter.OsgiServiceRegistrationListener;

/**
 * @author Costin Leau
 */
public class TestRegistrationListener implements OsgiServiceRegistrationListener {

	public Map<Object, Map> registered = new LinkedHashMap<Object, Map>();
	public Map<Object, Map> unregistered = new LinkedHashMap<Object, Map>();

	public void registered(Object service, Map serviceProperties) throws Exception {
		registered.put(service, serviceProperties);
	}

	public void unregistered(Object service, Map serviceProperties) throws Exception {
		unregistered.put(service, serviceProperties);
	}
}
