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

import java.util.Properties;

import org.eclipse.gemini.blueprint.compendium.cm.ConfigAdminPropertiesFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Simple namespace parser for osgix:cm-properties. Extends Single bean
 * definition parser (instead of the simpleBeanDefParser) to properly filter
 * attributes based on the declared namespace.
 * 
 * @author Costin Leau
 */
public class ConfigPropertiesDefinitionParser extends AbstractSimpleBeanDefinitionParser {

	private static final String PROPERTIES_PROP = "properties";


	protected Class<?> getBeanClass(Element element) {
		return ConfigAdminPropertiesFactoryBean.class;
	}

	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		// parse attributes using conventions
		super.doParse(element, parserContext, builder);

		// parse nested element (if any)
		Properties parsedProps = parserContext.getDelegate().parsePropsElement(element);
		if (!parsedProps.isEmpty()) {
			if (builder.getRawBeanDefinition().getPropertyValues().contains(PROPERTIES_PROP)) {
				parserContext.getReaderContext().error(
					"Property '" + PROPERTIES_PROP
							+ "' is defined more then once. Only one approach may be used per property.", element);

			}
			builder.addPropertyValue(PROPERTIES_PROP, parsedProps);
		}
	}
}
