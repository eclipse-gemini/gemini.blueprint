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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.gemini.blueprint.blueprint.reflect.MetadataFactory;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.container.ComponentDefinitionException;
import org.osgi.service.blueprint.container.NoSuchComponentException;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;

/**
 * Default {@link BlueprintContainer} implementation. Wraps Spring's {@link ConfigurableListableBeanFactory} in the
 * BlueprintContainer interface.
 * 
 * <b>Note</b>: This class does not fully implements the Blueprint contract: for example it does not fire any of the
 * Blueprint events nor performs exception handling - these concerns are left to the Blueprint extender.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
public class SpringBlueprintContainer implements BlueprintContainer {

	// cannot use a ConfigurableBeanFactory since the context is not yet refreshed at construction time
	private final ConfigurableApplicationContext applicationContext;
	private volatile ConfigurableListableBeanFactory beanFactory;

	public SpringBlueprintContainer(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public Object getComponentInstance(String name) throws NoSuchComponentException {
		if (getBeanFactory().containsBean(name)) {
			try {
				return getBeanFactory().getBean(name);
			} catch (RuntimeException ex) {
				throw new ComponentDefinitionException("Cannot get component instance " + name, ex);
			}
		} else {
			throw new NoSuchComponentException(name);
		}
	}

	public ComponentMetadata getComponentMetadata(String name) throws NoSuchComponentException {
		if (getBeanFactory().containsBeanDefinition(name)) {
			BeanDefinition beanDefinition = getBeanFactory().getBeanDefinition(name);
			return MetadataFactory.buildComponentMetadataFor(name, beanDefinition);
		} else {
			throw new NoSuchComponentException(name);
		}
	}

	public Set<String> getComponentIds() {
		String[] names = getBeanFactory().getBeanDefinitionNames();
		Set<String> components = new LinkedHashSet<String>(names.length);
		CollectionUtils.mergeArrayIntoCollection(names, components);
		Set<String> filtered = MetadataFactory.filterIds(components);
		return Collections.unmodifiableSet(filtered);
	}

	@SuppressWarnings("unchecked")
	public Collection<?> getMetadata(Class type) {
		return getComponentMetadata(type);
	}

	@SuppressWarnings("unchecked")
	private <T extends ComponentMetadata> Collection<T> getComponentMetadata(Class<T> clazz) {
		Collection<ComponentMetadata> metadatas = getComponentMetadataForAllComponents();
		Collection<T> filteredMetadata = new ArrayList<T>(metadatas.size());

		for (ComponentMetadata metadata : metadatas) {
			if (clazz.isInstance(metadata)) {
				filteredMetadata.add((T) metadata);
			}
		}

		return Collections.unmodifiableCollection(filteredMetadata);
	}

	private Collection<ComponentMetadata> getComponentMetadataForAllComponents() {
		return MetadataFactory.buildComponentMetadataFor(getBeanFactory());
	}

	private ConfigurableListableBeanFactory getBeanFactory() {
		if (beanFactory == null) {
			beanFactory = applicationContext.getBeanFactory();
		}
		return beanFactory;
	}
}