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

package org.eclipse.gemini.blueprint.iandt.ns;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Bogus handler. Creates a bean definition to test whether the handler
 * providing the namespace can actually start its own context. It registers two
 * beans named: "nsDate" and "nsBean".
 * 
 * @author Costin Leau
 */
public class BogusHandler implements NamespaceHandler {

	public static volatile boolean initialized = false;


	public BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder definition, ParserContext parserContext) {
		return null;
	}

	public void init() {
		initialized = true;
		System.out.println("BogusHandler initialized");
	}

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();
		
		registry.registerBeanDefinition("nsDate", BeanDefinitionBuilder.genericBeanDefinition("java.util.Date").getBeanDefinition());
		registry.registerBeanDefinition("nsBean", BeanDefinitionBuilder.genericBeanDefinition("java.awt.Rectangle").getBeanDefinition());
		return null;
	}
}
