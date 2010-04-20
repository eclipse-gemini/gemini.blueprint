/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.config.internal;

import java.util.Locale;

import org.eclipse.gemini.blueprint.bundle.BundleActionEnum;
import org.eclipse.gemini.blueprint.bundle.BundleFactoryBean;
import org.eclipse.gemini.blueprint.config.internal.util.AttributeCallback;
import org.eclipse.gemini.blueprint.config.internal.util.ParserUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * BundleFactoryBean definition.
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
public class BundleBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	static class BundleActionCallback implements AttributeCallback {

		public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder builder) {
			String name = attribute.getLocalName();
			if (ACTION.equals(name)) {
				builder.addPropertyValue(ACTION_PROP, parseAction(parent, attribute));
				return false;
			}

			if (DESTROY_ACTION.equals(name)) {
				builder.addPropertyValue(DESTROY_ACTION_PROP, parseAction(parent, attribute));
				return false;
			}

			return true;
		}

		// do upper case to make sure the constants match
		private Object parseAction(Element parent, Attr attribute) {
			return Enum.valueOf(BundleActionEnum.class, attribute.getValue().toUpperCase(Locale.ENGLISH));
		}
	};

	private static final String ACTION = "action";

	private static final String DESTROY_ACTION = "destroy-action";

	// class properties

	private static final String ACTION_PROP = "bundleAction";

	private static final String DESTROY_ACTION_PROP = "bundleDestroyAction";

	private static final String BUNDLE_PROP = "bundle";

	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		BundleActionCallback callback = new BundleActionCallback();

		ParserUtils.parseCustomAttributes(element, builder, new AttributeCallback[] { callback });

		// parse nested definition (in case there is any)

		if (element.hasChildNodes()) {
			NodeList nodes = element.getChildNodes();
			boolean foundElement = false;
			for (int i = 0; i < nodes.getLength() && !foundElement; i++) {
				Node nd = nodes.item(i);
				if (nd instanceof Element) {
					foundElement = true;
					Object obj =
							parserContext.getDelegate().parsePropertySubElement((Element) nd,
									builder.getBeanDefinition());
					builder.addPropertyValue(BUNDLE_PROP, obj);
				}
			}
		}
		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
	}

	protected Class getBeanClass(Element element) {
		return BundleFactoryBean.class;
	}
}