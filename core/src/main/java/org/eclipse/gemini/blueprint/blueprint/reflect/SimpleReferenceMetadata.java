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

import org.osgi.service.blueprint.reflect.ReferenceMetadata;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;

/**
 * @author Costin Leau
 */
class SimpleReferenceMetadata extends SimpleServiceReferenceComponentMetadata implements ReferenceMetadata {

	private static final String TIMEOUT_PROP = "timeout";
	private static final long DEFAULT_TIMEOUT = 300000;
	private final long timeout;

	/**
	 * Constructs a new <code>SpringUnaryServiceReferenceComponentMetadata</code> instance.
	 * 
	 * @param name
	 * @param definition
	 */
	public SimpleReferenceMetadata(String name, BeanDefinition definition) {
		super(name, definition);

		MutablePropertyValues pvs = beanDefinition.getPropertyValues();
		if (pvs.contains(TIMEOUT_PROP)) {
			Object value = MetadataUtils.getValue(pvs, TIMEOUT_PROP);

			timeout = Long
					.parseLong((value instanceof String ? (String) value : ((TypedStringValue) value).getValue()));
		} else {
			timeout = DEFAULT_TIMEOUT;
		}
	}

	public long getTimeout() {
		return timeout;
	}
}