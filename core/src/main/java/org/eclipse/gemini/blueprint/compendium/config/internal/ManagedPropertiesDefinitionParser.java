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

package org.eclipse.gemini.blueprint.compendium.config.internal;

import org.eclipse.gemini.blueprint.compendium.internal.cm.ManagedServiceInstanceTrackerPostProcessor;
import org.eclipse.gemini.blueprint.config.internal.util.AttributeCallback;
import org.eclipse.gemini.blueprint.config.internal.util.ParserUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Bean definition parser for 'managed-service' element. Configures the infrastructure beans and adds tracking.
 * 
 * @author Costin Leau
 * 
 */
public class ManagedPropertiesDefinitionParser implements BeanDefinitionDecorator {

	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
		BeanDefinition trackingBppDef = createTrackerBpp((Element) node, definition);
		// append the tracked bean name to the generated name for easier debugging
		String generatedName =
				parserContext.getReaderContext().generateBeanName(trackingBppDef)
						+ BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + definition.getBeanName();

		parserContext.getRegistry().registerBeanDefinition(generatedName, trackingBppDef);
		return definition;
	}

	private BeanDefinition createTrackerBpp(Element elem, BeanDefinitionHolder definition) {
		BeanDefinitionBuilder builder =
				BeanDefinitionBuilder.genericBeanDefinition(ManagedServiceInstanceTrackerPostProcessor.class).setRole(
						BeanDefinition.ROLE_INFRASTRUCTURE);
		builder.addConstructorArgValue(definition.getBeanName());
		ParserUtils.parseCustomAttributes(elem, builder, (AttributeCallback[]) null);
		return builder.getBeanDefinition();
	}
}
