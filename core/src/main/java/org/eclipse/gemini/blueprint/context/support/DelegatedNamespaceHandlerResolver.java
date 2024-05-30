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

package org.eclipse.gemini.blueprint.context.support;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.util.Assert;

/**
 * Delegated XML namespace handler resolver.
 * 
 * @author Costin Leau
 * 
 */
class DelegatedNamespaceHandlerResolver implements NamespaceHandlerResolver {

	/** logger */
	private static final Log log = LogFactory.getLog(DelegatedNamespaceHandlerResolver.class);

	private final Map<NamespaceHandlerResolver, String> resolvers = new LinkedHashMap<NamespaceHandlerResolver, String>(
		2);


	public void addNamespaceHandler(NamespaceHandlerResolver resolver, String resolverToString) {
		Assert.notNull(resolver, "resolver is required");
		resolvers.put(resolver, resolverToString);
	}

	public NamespaceHandler resolve(String namespaceUri) {
		boolean trace = log.isTraceEnabled();

		for (Iterator<Map.Entry<NamespaceHandlerResolver, String>> iterator = resolvers.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<NamespaceHandlerResolver, String> entry = iterator.next();
			NamespaceHandlerResolver handlerResolver = entry.getKey();
			if (trace)
				log.trace("Trying to resolve namespace [" + namespaceUri + "] through resolver " + entry.getValue());
			NamespaceHandler handler = handlerResolver.resolve(namespaceUri);

			String resolvedMsg = (handler != null ? "" : "not ");
			if (trace)
				log.trace("Namespace [" + namespaceUri + "] was " + resolvedMsg + "resolved through handler resolver "
						+ entry.getValue());

			if (handler != null) {
				return handler;
			}

		}
		return null;
	}
}
