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

import java.beans.PropertyDescriptor;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * 
 * @author Costin Leau
 */
public class BlueprintFieldsTest extends TestCase {

	public void testUseBlueprintExceptions() throws Exception {
		OsgiServiceProxyFactoryBean fb = new OsgiServiceProxyFactoryBean();
		BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(fb);
		String propertyName = "useBlueprintExceptions";
		for (PropertyDescriptor desc : wrapper.getPropertyDescriptors()) {
			System.out.println(desc.getName());
		}
		Class type = wrapper.getPropertyType(propertyName);
		System.out.println("type is " + type);
		assertTrue(wrapper.isWritableProperty(propertyName));
		assertFalse(wrapper.isReadableProperty(propertyName));
	}
}
