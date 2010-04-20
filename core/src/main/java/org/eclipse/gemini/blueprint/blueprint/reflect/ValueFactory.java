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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.gemini.blueprint.blueprint.reflect.SimpleCollectionMetadata.CollectionType;
import org.osgi.service.blueprint.reflect.MapEntry;
import org.osgi.service.blueprint.reflect.Metadata;
import org.osgi.service.blueprint.reflect.NonNullMetadata;
import org.osgi.service.blueprint.reflect.NullMetadata;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReferenceFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedArray;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.ManagedSet;

/**
 * Adapter between Spring {@link BeanMetadataElement} and OSGi's Blueprint {@link Value}.
 * 
 * @see BeanMetadataElementFactory
 * @author Costin Leau
 */
class ValueFactory {

	private static final String BEAN_REF_FB_CLASS_NAME = BeanReferenceFactoryBean.class.getName();
	private static final String BEAN_REF_NAME_PROP = "targetBeanName";

	/**
	 * Creates the equivalent value for the given Spring metadata. Since Spring's metadata is a superset of the
	 * Blueprint spec, not all Spring types are supported.
	 * 
	 * @param metadata
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static Metadata buildValue(Object metadata) {

		if (metadata instanceof BeanMetadataElement) {

			// reference
			if (metadata instanceof RuntimeBeanReference) {
				RuntimeBeanReference reference = (RuntimeBeanReference) metadata;
				// check the special case of nested references being promoted
				return new SimpleRefMetadata(reference.getBeanName());
			}
			// reference name
			if (metadata instanceof RuntimeBeanNameReference) {
				RuntimeBeanNameReference reference = (RuntimeBeanNameReference) metadata;
				return new SimpleIdRefMetadata(reference.getBeanName());
			}
			// typed String
			if (metadata instanceof TypedStringValue) {
				// check if it's a <null/>
				TypedStringValue typedString = (TypedStringValue) metadata;
				return (typedString.getValue() == null ? NullMetadata.NULL : new SimpleValueMetadata(typedString));
			}

			// bean definition
			if (metadata instanceof BeanDefinition) {
				// check special alias case
				BeanDefinition def = (BeanDefinition) metadata;

				if (BEAN_REF_FB_CLASS_NAME.equals(def.getBeanClassName())) {
					BeanDefinition unwrapped = ComponentMetadataFactory.unwrapImporterReference(def);
					if (unwrapped != null) {
						return ComponentMetadataFactory.buildMetadata(null, unwrapped);
					} else {
						return new SimpleRefMetadata((String) MetadataUtils.getValue(def.getPropertyValues(),
								BEAN_REF_NAME_PROP));
					}
				}
				return MetadataFactory.buildComponentMetadataFor(null, def);
			}

			// bean definition holder (used for inner beans/components)
			if (metadata instanceof BeanDefinitionHolder) {
				BeanDefinitionHolder holder = (BeanDefinitionHolder) metadata;

				// we ignore the name even though one was specified
				return MetadataFactory.buildComponentMetadataFor(null, holder.getBeanDefinition());
			}

			// managedXXX...
			if (metadata instanceof ManagedArray) {
				ManagedArray array = (ManagedArray) metadata;
				return new SimpleCollectionMetadata(getMetadata(array), CollectionType.ARRAY, array
						.getElementTypeName());
			}

			if (metadata instanceof ManagedList) {
				ManagedList list = (ManagedList) metadata;
				return new SimpleCollectionMetadata(getMetadata(list), CollectionType.LIST, list.getElementTypeName());
			}

			if (metadata instanceof ManagedSet) {
				ManagedSet set = (ManagedSet) metadata;
				return new SimpleCollectionMetadata(getMetadata(set), CollectionType.SET, set.getElementTypeName());
			}

			if (metadata instanceof ManagedMap) {
				ManagedMap<Object, Object> map = (ManagedMap) metadata;
				return new SimpleMapMetadata(ValueFactory.getEntries(map), map.getKeyTypeName(), map.getValueTypeName());
			}

			if (metadata instanceof ManagedProperties) {
				ManagedProperties properties = (ManagedProperties) metadata;
				return new SimplePropsMetadata(ValueFactory.getEntries(properties));
			}

			throw new IllegalArgumentException("Unsupported metadata type " + metadata.getClass());
		}

		// no metadata - probably some parser added the object directly
		// try to convert it into a String
		return new SimpleValueMetadata(null, metadata.toString());
	}

	static <E> List<Metadata> getMetadata(Collection<E> collection) {
		if (collection.isEmpty()) {
			return Collections.emptyList();
		}

		List<Metadata> list = new ArrayList<Metadata>(collection.size());

		for (Object value : collection) {
			list.add(ValueFactory.buildValue(value));
		}

		return Collections.unmodifiableList(list);
	}

	static <K, V> List<MapEntry> getEntries(Map<K, V> map) {
		if (map.isEmpty())
			return Collections.emptyList();

		List<MapEntry> entries = new ArrayList<MapEntry>(map.size());
		// convert map objects
		for (Map.Entry<K, V> entry : map.entrySet()) {
			NonNullMetadata key = (NonNullMetadata) ValueFactory.buildValue(entry.getKey());
			Metadata value = (Metadata) ValueFactory.buildValue(entry.getValue());

			entries.add(new SimpleMapEntry(key, value));
		}
		return Collections.unmodifiableList(entries);
	}
}