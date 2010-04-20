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

import java.util.Dictionary;
import java.util.Map;

/**
 * Custom class used for testing.
 * 
 * @author Costin Leau
 * 
 */
public class CustomRegistrationListener {

	public static int REG_CALLS = 0;

	public static int UNREG_CALLS = 0;

	public static Object SERVICE_UNREG;

	public static Object SERVICE_REG;

	public void onRegistration(Object service, Map properties) {
		REG_CALLS++;
		SERVICE_REG = service;
	}

	public void onUnregistration(Object service, Dictionary props) {
		UNREG_CALLS++;
		SERVICE_UNREG = service;
	}

}
