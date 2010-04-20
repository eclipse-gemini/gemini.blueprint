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

import org.eclipse.gemini.blueprint.compendium.internal.cm.ManagedServiceFactoryFactoryBean;
import org.eclipse.gemini.blueprint.config.internal.util.AttributeCallback;
import org.eclipse.gemini.blueprint.config.internal.util.ParserUtils;
import org.eclipse.gemini.blueprint.config.internal.util.ServiceAttributeCallback;
import org.eclipse.gemini.blueprint.config.internal.util.ServiceParsingUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Namespace parser for osgix:managed-service-factory.
 * 
 * @author Costin Leau
 */
public class ManagedServiceFactoryDefinitionParser extends AbstractSimpleBeanDefinitionParser {

	private static final String TEMPLATE_PROP = "templateDefinition";
	private static final String LISTENER = "registration-listener";
	private static final String LISTENERS_PROP = "listeners";
	private static final String LOCAL_OVERRIDE = "local-override";
	private static final String LOCAL_OVERRIDE_PROP = "localOverride";

	protected Class<?> getBeanClass(Element element) {
		return ManagedServiceFactoryFactoryBean.class;
	}

	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

		// do conversion for some of them (mainly enums)
		ParserUtils.parseCustomAttributes(element, builder, new AttributeCallback[] { new ServiceAttributeCallback() });

		// get nested elements
		NodeList children = element.getChildNodes();

		ManagedList listeners = new ManagedList(children.getLength());
		BeanDefinition nestedDefinition = null;

		for (int i = 0; i < children.getLength(); i++) {
			Node nd = children.item(i);
			if (nd instanceof Element) {
				Element nestedElement = (Element) nd;
				String name = nestedElement.getLocalName();

				// osgi:interface
				if (ServiceParsingUtils.parseInterfaces(element, nestedElement, parserContext, builder)) {
				}
				// osgi:service-properties
				else if (ServiceParsingUtils.parseServiceProperties(element, nestedElement, parserContext, builder)) {
				}
				// osgi:registration-listener
				else if (LISTENER.equals(name)) {
					listeners.add(ServiceParsingUtils.parseListener(parserContext, nestedElement, builder));
				}

				// nested bean reference/declaration
				else {
					String ns = nestedElement.getNamespaceURI();
					// it's a Spring Bean
					if ((ns == null && name.equals(BeanDefinitionParserDelegate.BEAN_ELEMENT))
							|| ns.equals(BeanDefinitionParserDelegate.BEANS_NAMESPACE_URI)) {
						nestedDefinition =
								parserContext.getDelegate().parseBeanDefinitionElement(nestedElement)
										.getBeanDefinition();
					}
					// it's non Spring
					else {
						nestedDefinition = parserContext.getDelegate().parseCustomElement(nestedElement);
					}

				}
			}

			// don't pass the properties as a bean definition since Spring tries to do conversion
			// and even if we mark the pv as being converted, the flag gets ignored (SPR-5293)
			builder.addPropertyValue(TEMPLATE_PROP, new BeanDefinition[] { nestedDefinition });
			builder.addPropertyValue(LISTENERS_PROP, listeners);
		}
	}
}