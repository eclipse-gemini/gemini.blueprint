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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;

/**
 * Container-managed update. Locks the instance and performs autowire by name
 * injection.
 * 
 * @author Costin Leau
 */
class ContainerManagedUpdate implements UpdateCallback {

	private final AbstractBeanFactory beanFactory;


	/**
	 * Constructs a new <code>ContainerManagedUpdate</code> instance.
	 * 
	 * @param beanFactory
	 */
	public ContainerManagedUpdate(BeanFactory beanFactory) {
		super();
		this.beanFactory = (beanFactory instanceof AbstractBeanFactory ? (AbstractBeanFactory) beanFactory : null);
	}

	public void update(Object instance, Map properties) {
		synchronized (instance) {
			CMUtils.applyMapOntoInstance(instance, properties, beanFactory);
		}
	}
}