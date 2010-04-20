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

import org.eclipse.gemini.blueprint.service.importer.support.MemberType;
import org.osgi.service.blueprint.reflect.ReferenceListMetadata;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * @author Costin Leau
 */
class SimpleReferenceListMetadata extends SimpleServiceReferenceComponentMetadata implements ReferenceListMetadata {

	private final int memberType;
	private static final String MEMBER_TYPE_PROP = "memberType";

	/**
	 * Constructs a new <code>SpringRefCollectionMetadata</code> instance.
	 * 
	 * @param name
	 * @param definition
	 */
	public SimpleReferenceListMetadata(String name, BeanDefinition definition) {
		super(name, definition);

		MemberType type = MemberType.SERVICE_OBJECT;

		MutablePropertyValues pvs = beanDefinition.getPropertyValues();
		if (pvs.contains(MEMBER_TYPE_PROP)) {
			type = (MemberType) MetadataUtils.getValue(pvs, MEMBER_TYPE_PROP);
		}

		memberType = type.ordinal() + 1;
	}

	public int getMemberType() {
		return memberType;
	}
}