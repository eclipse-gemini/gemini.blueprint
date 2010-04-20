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
import java.util.Set;

import org.osgi.service.blueprint.reflect.CollectionMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.springframework.util.StringUtils;

/**
 * Basic {@link CollectionMetadata} implementation.
 * 
 * @author Costin Leau
 */
class SimpleCollectionMetadata implements CollectionMetadata {

	public enum CollectionType {
		ARRAY(Object[].class), LIST(List.class), SET(Set.class);

		private final Class<?> type;

		private CollectionType(Class<?> type) {
			this.type = type;
		}

		static CollectionType resolve(Class<?> type) {

			for (CollectionType supportedType : CollectionType.values()) {
				if (supportedType.type.equals(type)) {
					return supportedType;
				}
			}

			throw new IllegalArgumentException("Unsupported class type " + type);
		}
	}

	private final List<Metadata> values;
	private final CollectionType collectionType;
	private final String typeName;

	public SimpleCollectionMetadata(List<Metadata> values, CollectionType type, String valueTypeName) {
		this.values = values;
		this.collectionType = type;
		this.typeName = (StringUtils.hasText(valueTypeName) ? valueTypeName : null);
	}

	public SimpleCollectionMetadata(List<Metadata> values, Class<?> type, String valueTypeName) {
		this(values, CollectionType.resolve(type), valueTypeName);
	}

	public Class<?> getCollectionClass() {
		return collectionType.type;
	}

	public String getValueType() {
		return typeName;
	}

	public List<Metadata> getValues() {
		return values;
	}
}