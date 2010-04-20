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

import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.util.Assert;

/**
 * Adapter class between Spring {@link ApplicationEventMulticaster} and Spring-DM
 * {@link OsgiBundleApplicationContextEventMulticaster}. Allows reusage (especially considering the contractual
 * similarities between the two interfaces) of existing implementations for propagating Spring-DM events.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiBundleApplicationContextEventMulticasterAdapter implements
		OsgiBundleApplicationContextEventMulticaster {

	private final ApplicationEventMulticaster delegatedMulticaster;

	/**
	 * Constructs a new <code>OsgiBundleApplicationContextEventMulticasterAdapter</code> instance.
	 * 
	 * @param delegatedMulticaster
	 */
	public OsgiBundleApplicationContextEventMulticasterAdapter(ApplicationEventMulticaster delegatedMulticaster) {
		Assert.notNull(delegatedMulticaster);
		this.delegatedMulticaster = delegatedMulticaster;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The given listener is wrapped with an adapter class that delegates the equals/hashcode methods to the wrapped
	 * listener instance. However, depending on the equals implementation, this might affect the object identity.
	 */
	public void addApplicationListener(OsgiBundleApplicationContextListener osgiListener) {
		Assert.notNull(osgiListener);
		delegatedMulticaster.addApplicationListener(ApplicationListenerAdapter.createAdapter(osgiListener));
	}

	public void multicastEvent(OsgiBundleApplicationContextEvent osgiEvent) {
		delegatedMulticaster.multicastEvent(osgiEvent);
	}

	public void removeAllListeners() {
		delegatedMulticaster.removeAllListeners();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The given listener is wrapped with an adapter class that delegates the equals/hashcode methods to the wrapped
	 * listener instance. However, depending on the equals implementation, this might affect the object identity.
	 */
	public void removeApplicationListener(OsgiBundleApplicationContextListener osgiListener) {
		Assert.notNull(null);
		delegatedMulticaster.removeApplicationListener(ApplicationListenerAdapter.createAdapter(osgiListener));
	}
}