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

import java.util.Collection;
import java.util.Set;

import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Adapter factory that translates Blueprint {@link ComponentMetadata} into Spring {@link BeanDefinition}s (and vice
 * versa).
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
public class MetadataFactory {

	private static final BeanDefinitionFactory springFactory = new BeanDefinitionFactory();
	private static final ComponentMetadataFactory blueprintFactory = new ComponentMetadataFactory();

	public static BeanDefinition buildBeanDefinitionFor(ComponentMetadata metadata) {
		return springFactory.buildBeanDefinitionFor(metadata);
	}

	/**
	 * Inspects the given {@link BeanDefinition beanDefinition} and returns the appropriate {@link ComponentMetadata
	 * metadata} (can be one of {@link LocalComponentMetadata}, {@link ServiceExportComponentMetadata}, or
	 * {@link ServiceReferenceComponentMetadata}).
	 * 
	 * @param name bean name
	 * @param beanDefinition Spring bean definition
	 * @return an OSGi component metadata.
	 */
	public static ComponentMetadata buildComponentMetadataFor(String name, BeanDefinition beanDefinition) {
		return blueprintFactory.buildMetadata(name, beanDefinition);
	}

	/**
	 * Inspects the given {@link ConfigurableListableBeanFactory factory} and returns the appropriate OSGi 4.2 Blueprint
	 * {@link ComponentMetadata metadata}.
	 * 
	 * @param factory Spring bean factory
	 * @return collection of blueprint metadata
	 */
	public static Collection<ComponentMetadata> buildComponentMetadataFor(ConfigurableListableBeanFactory factory) {
		return blueprintFactory.buildComponentMetadataFor(factory);
	}

	/**
	 * Builds the Blueprint metadata based on the nested elements contained by the given bean definition.
	 * 
	 * @param beanDefinition
	 * @return
	 */
	static Collection<ComponentMetadata> buildNestedComponentMetadataFor(BeanDefinition beanDefinition) {
		return blueprintFactory.buildNestedMetadata(beanDefinition);
	}

	public static Set<String> filterIds(Set<String> components) {
		return blueprintFactory.filterIds(components);
	}
}