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

package org.eclipse.gemini.blueprint.blueprint.reflect;

import org.osgi.service.blueprint.reflect.RegistrationListener;
import org.osgi.service.blueprint.reflect.Target;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * Basic {@link RegistrationListener} implementation.
 * 
 * @author Costin Leau
 */
class SimpleRegistrationListener implements RegistrationListener {

	private static final String REG_PROP = "registrationMethod";
	private static final String UNREG_PROP = "unregistrationMethod";
	private static final String LISTENER_NAME_PROP = "targetBeanName";
	private static final String LISTENER_PROP = "target";

	private final Target listenerComponent;
	private final String registrationMethod, unregistrationMethod;

	public SimpleRegistrationListener(AbstractBeanDefinition beanDefinition) {
		MutablePropertyValues pvs = beanDefinition.getPropertyValues();
		registrationMethod = (String) MetadataUtils.getValue(pvs, REG_PROP);
		unregistrationMethod = (String) MetadataUtils.getValue(pvs, UNREG_PROP);

		// listener reference
		if (pvs.contains(LISTENER_NAME_PROP)) {
			listenerComponent = new SimpleRefMetadata((String) MetadataUtils.getValue(pvs, LISTENER_NAME_PROP));
		} else {
			// convert the BeanDefinitionHolder
			listenerComponent = (Target) ValueFactory.buildValue(MetadataUtils.getValue(pvs, LISTENER_PROP));
		}

	}

	public Target getListenerComponent() {
		return listenerComponent;
	}

	public String getRegistrationMethod() {
		return registrationMethod;
	}

	public String getUnregistrationMethod() {
		return unregistrationMethod;
	}
}