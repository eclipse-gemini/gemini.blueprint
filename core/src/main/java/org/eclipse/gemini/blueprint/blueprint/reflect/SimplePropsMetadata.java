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
import org.osgi.service.blueprint.reflect.PropsMetadata;

/**
 * Simple implementation for {@link PropertiesValue} interface.
 * 
 * @author Costin Leau
 */
class SimplePropsMetadata implements PropsMetadata {

	private final List<MapEntry> entries;

	/**
	 * Constructs a new <code>SimplePropertiesValue</code> instance.
	 * 
	 * @param entries
	 */
	public SimplePropsMetadata(List<MapEntry> entries) {
		this.entries = entries;
	}

	public List<MapEntry> getEntries() {
		return entries;
	}
}
