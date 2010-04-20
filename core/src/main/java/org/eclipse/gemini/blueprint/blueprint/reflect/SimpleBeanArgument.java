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

import org.osgi.service.blueprint.reflect.BeanArgument;
import org.osgi.service.blueprint.reflect.Metadata;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;

/**
 * Basic implementation for {@link BeanArgument} interface.
 * 
 * @author Costin Leau
 */
class SimpleBeanArgument implements BeanArgument {

	private final int index;
	private final String typeName;
	private final Metadata value;
	private static final int UNSPECIFIED_INDEX = -1;

	/**
	 * Constructs a new <code>SimpleBeanArgument</code> instance.
	 * 
	 * @param index
	 * @param typeName
	 * @param value
	 */
	public SimpleBeanArgument(int index, ValueHolder valueHolder) {
		this.index = index;
		this.typeName = valueHolder.getType();
		this.value = ValueFactory.buildValue(MetadataUtils.getValue(valueHolder));
	}

	public SimpleBeanArgument(ValueHolder valueHolder) {
		this(UNSPECIFIED_INDEX, valueHolder);
	}

	public int getIndex() {
		return index;
	}

	public Metadata getValue() {
		return value;
	}

	public String getValueType() {
		return typeName;
	}
}