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

import org.osgi.service.blueprint.reflect.MapEntry;
import org.osgi.service.blueprint.reflect.MapMetadata;
import org.springframework.util.StringUtils;

/**
 * Simple implementation for {@link MapValue} interface.
 * 
 * @author Costin Leau
 * 
 */
class SimpleMapMetadata implements MapMetadata {

	private final List<MapEntry> entries;
	private final String keyValueType, valueValueType;

	/**
	 * 
	 * Constructs a new <code>SimpleMapMetadata</code> instance.
	 * 
	 * @param entries
	 * @param keyTypeName
	 * @param valueTypeName
	 */
	public SimpleMapMetadata(List<MapEntry> entries, String keyTypeName, String valueTypeName) {
		this.entries = entries;
		this.keyValueType = (StringUtils.hasText(keyTypeName) ? keyTypeName : null);
		this.valueValueType = (StringUtils.hasText(valueTypeName) ? valueTypeName : null);
		;
	}

	public List<MapEntry> getEntries() {
		return entries;
	}

	public String getKeyType() {
		return keyValueType;
	}

	public String getValueType() {
		return valueValueType;
	}
}