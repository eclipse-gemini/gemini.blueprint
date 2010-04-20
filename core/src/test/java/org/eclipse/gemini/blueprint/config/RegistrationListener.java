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

package org.eclipse.gemini.blueprint.config;

import java.util.Map;

import org.eclipse.gemini.blueprint.service.exporter.OsgiServiceRegistrationListener;

/**
 * Registration listener.
 * 
 * @author Costin Leau
 * 
 */
public class RegistrationListener implements OsgiServiceRegistrationListener {

	static int BIND_CALLS = 0;

	static int UNBIND_CALLS = 0;

	static Object SERVICE_UNREG;

	static Object SERVICE_REG;

	public void registered(Object service, Map serviceProperties) {
		BIND_CALLS++;
		SERVICE_REG = service;
	}

	public void unregistered(Object service, Map serviceProperties) {
		UNBIND_CALLS++;
		SERVICE_UNREG = service;
	}

}
