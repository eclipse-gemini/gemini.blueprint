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

package org.eclipse.gemini.blueprint.config;

import org.eclipse.gemini.blueprint.config.internal.BundleBeanDefinitionParser;
import org.eclipse.gemini.blueprint.config.internal.CollectionBeanDefinitionParser;
import org.eclipse.gemini.blueprint.config.internal.ReferenceBeanDefinitionParser;
import org.eclipse.gemini.blueprint.config.internal.ServiceBeanDefinitionParser;
import org.eclipse.gemini.blueprint.service.importer.support.CollectionType;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler for Osgi definitions.
 * 
 * @author Hal Hildebrand
 * @author Andy Piper
 * @author Costin Leau
 */
class OsgiNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		//
		// Importer definitions
		//

		// a. single reference
		registerBeanDefinitionParser("reference", new ReferenceBeanDefinitionParser());

		registerBeanDefinitionParser("list", new CollectionBeanDefinitionParser() {

			protected CollectionType collectionType() {
				return CollectionType.LIST;
			}
		});

		registerBeanDefinitionParser("set", new CollectionBeanDefinitionParser() {

			protected CollectionType collectionType() {
				return CollectionType.SET;
			}
		});

		//
		// Exporter
		//
		registerBeanDefinitionParser("service", new ServiceBeanDefinitionParser());

		//
		// Bundle FB
		//
		registerBeanDefinitionParser("bundle", new BundleBeanDefinitionParser());
	}
}
