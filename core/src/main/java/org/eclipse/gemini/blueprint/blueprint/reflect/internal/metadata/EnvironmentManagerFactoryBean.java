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

package org.eclipse.gemini.blueprint.blueprint.reflect.internal.metadata;

import org.springframework.beans.factory.FactoryBean;

/**
 * Basic FactoryBean acting as a wrapper around environment beans. Since usually these are already instantiated, to
 * allow registration of bean definitions inside the container, this 'special' class is used so it can be identified
 * when creating the blueprint environment metadata.
 * 
 * @author Costin Leau
 */
public class EnvironmentManagerFactoryBean implements FactoryBean<Object> {

	private final Object instance;

	public EnvironmentManagerFactoryBean(Object instance) {
		this.instance = instance;
	}

	public Object getObject() throws Exception {
		return instance;
	}

	public Class<?> getObjectType() {
		return instance.getClass();
	}

	public boolean isSingleton() {
		return true;
	}
}
