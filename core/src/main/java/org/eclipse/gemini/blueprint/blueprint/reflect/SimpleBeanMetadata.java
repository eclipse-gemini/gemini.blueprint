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

import java.util.List;

import org.eclipse.gemini.blueprint.blueprint.config.internal.BlueprintParser;
import org.osgi.service.blueprint.reflect.BeanArgument;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.BeanProperty;
import org.osgi.service.blueprint.reflect.Target;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.StringUtils;

/**
 * Default {@link LocalComponentMetadata} implementation based on Spring's {@link BeanDefinition}.
 * 
 * @author Costin Leau
 */
class SimpleBeanMetadata extends SimpleComponentMetadata implements BeanMetadata {

	private final List<BeanArgument> arguments;
	private final List<BeanProperty> properties;

	private final String factoryMethod;
	private final Target factoryComponent;
	private final String scope;

	/**
	 * Constructs a new <code>SpringLocalComponentMetadata</code> instance.
	 * 
	 * @param name bean name
	 * @param definition Spring bean definition
	 */
	public SimpleBeanMetadata(String name, BeanDefinition definition) {
		super(name, definition);

		final String factoryMtd = definition.getFactoryMethodName();
		if (StringUtils.hasText(factoryMtd)) {
			factoryMethod = factoryMtd;
			String factory = definition.getFactoryBeanName();
			if (StringUtils.hasText(factory)) {
				factoryComponent = new SimpleRefMetadata(factory);
			} else {
				factoryComponent = null;
			}
		} else {
			factoryComponent = null;
			factoryMethod = null;
		}

		arguments = MetadataUtils.getBeanArguments(definition);
		properties = MetadataUtils.getBeanProperties(definition);

		// double check if the definition had "scope" declared
		boolean hasAttribute = definition.hasAttribute(BlueprintParser.DECLARED_SCOPE);
		scope = (hasAttribute ? (StringUtils.hasText(name) ? beanDefinition.getScope() : null) : null);
	}

	public List<BeanArgument> getArguments() {
		return arguments;
	}

	public String getClassName() {
		return beanDefinition.getBeanClassName();
	}

	public String getDestroyMethod() {
		return beanDefinition.getDestroyMethodName();
	}

	public Target getFactoryComponent() {
		return factoryComponent;
	}

	public String getFactoryMethod() {
		return factoryMethod;
	}

	public String getInitMethod() {
		return beanDefinition.getInitMethodName();
	}

	public List<BeanProperty> getProperties() {
		return properties;
	}

	public Class<?> getRuntimeClass() {
		return beanDefinition.getBeanClass();
	}

	public String getScope() {
		return scope;
	}
}