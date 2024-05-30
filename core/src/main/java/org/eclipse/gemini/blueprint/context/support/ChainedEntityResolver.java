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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Delegated XML entity resolver.
 * 
 * @author Costin Leau
 * 
 */
class ChainedEntityResolver implements EntityResolver {

	/** logger */
	private static final Log log = LogFactory.getLog(ChainedEntityResolver.class);

	private final Map<EntityResolver, String> resolvers = new LinkedHashMap<EntityResolver, String>(2);


	public void addEntityResolver(EntityResolver resolver, String resolverToString) {
		Assert.notNull(resolver, "resolver is required");
		resolvers.put(resolver, resolverToString);
	}

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		boolean trace = log.isTraceEnabled();

		for (Map.Entry<EntityResolver, String> entry : resolvers.entrySet()) {
			EntityResolver entityResolver = entry.getKey();
			if (trace)
				log.trace("Trying to resolve entity [" + publicId + "|" + systemId + "] through resolver "
						+ entry.getValue());
			InputSource entity = entityResolver.resolveEntity(publicId, systemId);

			String resolvedMsg = (entity != null ? "" : "not ");
			if (trace)
				log.trace("Entity [" + publicId + "|" + systemId + "] was " + resolvedMsg
						+ "resolved through entity resolver " + entry.getValue());

			if (entity != null) {
				return entity;
			}
		}
		return null;
	}
}
