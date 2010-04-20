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

package org.eclipse.gemini.blueprint.compendium.internal.cm;

import java.util.Map;

/**
 * Manager dealing with injection and updates of Spring beans configured through
 * the Configuration Admin.
 * 
 * <p/> Implementations are responsible for interacting with the Configuration
 * Admin service, for injection/reinjection of properties into the managed
 * beans.
 * 
 * @author Costin Leau
 * 
 */
public interface ManagedServiceBeanManager {

	/**
	 * Registers the given Spring-managed bean instance with the manager. The
	 * manager will apply any existing configuration to the given bean and
	 * return the newly configured instance back.
	 * 
	 * @param bean Spring-managed bean instance
	 * @return reinjected bean instace
	 */
	Object register(Object bean);

	/**
	 * Deregisters the given Spring-managed instance from the manager. Once
	 * deregistered, no configuration updates will be propagated to the given
	 * instance.
	 * 
	 * @param bean
	 */
	void unregister(Object bean);

	/**
	 * Re-applies injection on the Spring-managed instances using the given
	 * properties.
	 * 
	 * @param properties new properties
	 */
	void updated(Map properties);
}
