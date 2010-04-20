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

import org.osgi.service.blueprint.reflect.BeanProperty;
import org.osgi.service.blueprint.reflect.Metadata;
import org.springframework.beans.PropertyValue;

/**
 * Basic implementation for {@link BeanProperty} interface.
 * 
 * @author Costin Leau
 * 
 */
class SimpleBeanProperty implements BeanProperty {

	private final String name;
	private final Metadata value;

	/**
	 * Constructs a new <code>SimpleBeanProperty</code> instance.
	 * 
	 * @param name
	 * @param value
	 */
	public SimpleBeanProperty(String name, Metadata value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Constructs a new <code>SimpleBeanProperty</code> instance.
	 * 
	 * @param propertyValue
	 */
	public SimpleBeanProperty(PropertyValue propertyValue) {
		this.name = propertyValue.getName();
		Object value = propertyValue.getValue();
		this.value = ValueFactory.buildValue(value);
	}

	public String getName() {
		return name;
	}

	public Metadata getValue() {
		return value;
	}
}