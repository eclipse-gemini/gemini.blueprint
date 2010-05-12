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

package org.eclipse.gemini.blueprint.iandt.serviceProxyFactoryBean;

import java.util.Dictionary;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Costin Leau
 * 
 */
public abstract class ServiceBaseTest extends BaseIntegrationTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "net.sourceforge.cglib, com.springsource.net.sf.cglib, 2.1.3" };
	}

	protected ServiceRegistration publishService(Object obj, String name) throws Exception {
		return bundleContext.registerService(name, obj, null);
	}

	protected ServiceRegistration publishService(Object obj, String names[]) throws Exception {
		return bundleContext.registerService(names, obj, null);
	}

	protected ServiceRegistration publishService(Object obj, String names[], Dictionary dict) throws Exception {
		return bundleContext.registerService(names, obj, null);
	}

	protected ServiceRegistration publishService(Object obj) throws Exception {
		return publishService(obj, obj.getClass().getName());
	}

	protected ServiceRegistration publishService(Object obj, Dictionary dict) throws Exception {
		return bundleContext.registerService(obj.getClass().getName(), obj, dict);
	}

}
