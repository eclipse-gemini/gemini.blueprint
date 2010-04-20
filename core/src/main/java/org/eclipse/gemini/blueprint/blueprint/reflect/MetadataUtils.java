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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.osgi.service.blueprint.reflect.BeanArgument;
import org.osgi.service.blueprint.reflect.BeanProperty;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;

/**
 * Internal utility class for dealing with metadata.
 * 
 * @author Costin Leau
 * 
 */
abstract class MetadataUtils {

	static Object getValue(PropertyValues pvs, String name) {
		if (pvs.contains(name)) {
			PropertyValue pv = pvs.getPropertyValue(name);
			// return (pv.isConverted() ? pv.getConvertedValue() : pv.getValue());
			return pv.getValue();
		}

		return null;
	}

	static Object getValue(PropertyValue pv) {
		// return (pv.isConverted() ? pv.getConvertedValue() : pv.getValue());
		return pv.getValue();
	}

	static Object getValue(ValueHolder valueHolder) {
		// return (valueHolder.isConverted() ? valueHolder.getConvertedValue() : valueHolder.getValue());
		return valueHolder.getValue();
	}

	static List<BeanArgument> getBeanArguments(BeanDefinition definition) {
		List<BeanArgument> temp;

		ConstructorArgumentValues ctorValues = definition.getConstructorArgumentValues();

		// get indexed values
		Map<Integer, ValueHolder> indexedArguments = ctorValues.getIndexedArgumentValues();

		// check first the indexed arguments
		if (!indexedArguments.isEmpty()) {
			temp = new ArrayList<BeanArgument>(indexedArguments.size());

			for (Map.Entry<Integer, ValueHolder> entry : indexedArguments.entrySet()) {
				temp.add(new SimpleBeanArgument(entry.getKey(), entry.getValue()));
			}
		} else {
			// followed by the generic arguments
			List<ValueHolder> args = ctorValues.getGenericArgumentValues();
			temp = new ArrayList<BeanArgument>(args.size());
			for (ValueHolder valueHolder : args) {
				temp.add(new SimpleBeanArgument(valueHolder));
			}
		}

		return Collections.unmodifiableList(temp);
	}

	static List<BeanProperty> getBeanProperties(BeanDefinition definition) {
		List<BeanProperty> temp;

		List<PropertyValue> pvs = definition.getPropertyValues().getPropertyValueList();

		if (pvs.isEmpty()) {
			return Collections.<BeanProperty> emptyList();
		} else {
			temp = new ArrayList<BeanProperty>(pvs.size());
		}

		for (PropertyValue propertyValue : pvs) {
			temp.add(new SimpleBeanProperty(propertyValue));
		}

		return Collections.unmodifiableList(temp);
	}
}