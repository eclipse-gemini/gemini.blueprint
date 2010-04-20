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

import org.osgi.service.blueprint.reflect.MapEntry;
import org.osgi.service.blueprint.reflect.Metadata;
import org.osgi.service.blueprint.reflect.NonNullMetadata;

/**
 * Basic MapEntry implementation.
 * 
 * @author Costin Leau
 */
class SimpleMapEntry implements MapEntry {

	private final NonNullMetadata key;
	private final Metadata value;

	public SimpleMapEntry(NonNullMetadata key, Metadata value) {
		this.key = key;
		this.value = value;
	}

	public NonNullMetadata getKey() {
		return key;
	}

	public Metadata getValue() {
		return value;
	}
}
