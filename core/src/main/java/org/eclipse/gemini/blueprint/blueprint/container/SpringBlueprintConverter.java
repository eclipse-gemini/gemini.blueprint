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

package org.eclipse.gemini.blueprint.blueprint.container;

import org.osgi.service.blueprint.container.Converter;
import org.osgi.service.blueprint.container.ReifiedType;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Blueprint converter exposing the backing container conversion capabilities.
 * 
 * @author Costin Leau
 */
public class SpringBlueprintConverter implements Converter {

	private final ConfigurableBeanFactory beanFactory;
	private volatile TypeConverter typeConverter;

	public SpringBlueprintConverter(ConfigurableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public boolean canConvert(Object source, ReifiedType targetType) {
		Class<?> required = targetType.getRawClass();
		try {
			getConverter().convertIfNecessary(source, required);
			return true;
		} catch (TypeMismatchException ex) {
			return false;
		}
	}

	public Object convert(Object source, ReifiedType targetType) throws Exception {
		Class<?> target = (targetType != null ? targetType.getRawClass() : null);
		return getConverter().convertIfNecessary(source, target);
	}

	private TypeConverter getConverter() {
		if (typeConverter == null) {
			SimpleTypeConverter simpleConverter = new SimpleTypeConverter();
			beanFactory.copyRegisteredEditorsTo(simpleConverter);
			simpleConverter.setConversionService(beanFactory.getConversionService());
			typeConverter = simpleConverter;
		}
		return typeConverter;
	}
}
