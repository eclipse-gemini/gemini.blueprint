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

package org.eclipse.gemini.blueprint.compendium.config;

import org.eclipse.gemini.blueprint.compendium.config.internal.ConfigPropertiesDefinitionParser;
import org.eclipse.gemini.blueprint.compendium.config.internal.ManagedPropertiesDefinitionParser;
import org.eclipse.gemini.blueprint.compendium.config.internal.ManagedServiceFactoryDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler for OSGi Compendium definitions.
 * 
 * @author Costin Leau
 * 
 */
class CompendiumNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("cm-properties", new ConfigPropertiesDefinitionParser());
		registerBeanDefinitionParser("managed-service-factory", new ManagedServiceFactoryDefinitionParser());
		registerBeanDefinitionDecorator("managed-properties", new ManagedPropertiesDefinitionParser());
	}
}
