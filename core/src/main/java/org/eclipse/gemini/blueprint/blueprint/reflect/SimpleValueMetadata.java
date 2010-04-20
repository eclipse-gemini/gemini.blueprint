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

import org.osgi.service.blueprint.reflect.ValueMetadata;
import org.springframework.util.StringUtils;

/**
 * Simple implementation for {@link SimpleValueMetadata} interface. Understands Spring's
 * {@link org.springframework.beans.factory.config.TypedStringValue}.
 * 
 * @author Costin Leau
 * 
 */
class SimpleValueMetadata implements ValueMetadata {

	private final String typeName, value;

	/**
	 * Constructs a new <code>SimpleValueMetadata</code> instance.
	 * 
	 * @param typeName
	 * @param value
	 */
	public SimpleValueMetadata(String typeName, String value) {
		this.typeName = (StringUtils.hasText(typeName) ? typeName : null);
		this.value = value;
	}

	public SimpleValueMetadata(org.springframework.beans.factory.config.TypedStringValue typedStringValue) {
		String specifiedType = typedStringValue.getSpecifiedTypeName();
		this.typeName = (StringUtils.hasText(specifiedType) ? specifiedType : null);
		this.value = typedStringValue.getValue();
	}

	public String getStringValue() {
		return value;
	}

	public String getType() {
		return typeName;
	}
}