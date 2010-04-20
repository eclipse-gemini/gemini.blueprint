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

import org.osgi.service.blueprint.reflect.ReferenceListener;
import org.osgi.service.blueprint.reflect.Target;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * Simple implementation for {@link BindingListenerMetadata} interface.
 * 
 * @author Costin Leau
 */
class SimpleReferenceListenerMetadata implements ReferenceListener {

	private static final String BIND_PROP = "bindMethod";
	private static final String UNBIND_PROP = "unbindMethod";
	private static final String LISTENER_NAME_PROP = "targetBeanName";
	private static final String LISTENER_PROP = "target";
	private final String bindMethodName, unbindMethodName;
	private final Target listenerComponent;

	public SimpleReferenceListenerMetadata(AbstractBeanDefinition beanDefinition) {
		MutablePropertyValues pvs = beanDefinition.getPropertyValues();
		bindMethodName = (String) MetadataUtils.getValue(pvs, BIND_PROP);
		unbindMethodName = (String) MetadataUtils.getValue(pvs, UNBIND_PROP);

		// listener reference
		if (pvs.contains(LISTENER_NAME_PROP)) {
			listenerComponent = new SimpleRefMetadata((String) MetadataUtils.getValue(pvs, LISTENER_NAME_PROP));
		} else {
			// convert the BeanDefinitionHolder
			listenerComponent = (Target) ValueFactory.buildValue(MetadataUtils.getValue(pvs, LISTENER_PROP));
		}
	}

	/**
	 * Constructs a new <code>SimpleBindingListenerMetadata</code> instance.
	 * 
	 * @param bindMethodName
	 * @param unbindMethodName
	 * @param listenerComponent
	 */
	public SimpleReferenceListenerMetadata(String bindMethodName, String unbindMethodName, Target listenerComponent) {
		this.bindMethodName = bindMethodName;
		this.unbindMethodName = unbindMethodName;
		this.listenerComponent = listenerComponent;
	}

	public String getBindMethod() {
		return bindMethodName;
	}

	public Target getListenerComponent() {
		return listenerComponent;
	}

	public String getUnbindMethod() {
		return unbindMethodName;
	}
}