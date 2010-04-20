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

package org.eclipse.gemini.blueprint.blueprint;

import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.service.importer.ServiceReferenceProxy;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.StaticServiceReferenceProxy;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.FactoryBean;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * @author Costin Leau
 */
public class ReferenceDelegateFactory implements FactoryBean {

	private final ServiceReference ref;


	public ReferenceDelegateFactory() throws Exception {
		ref = new MockServiceReference();
	}

	public Object getObject() throws Exception {
		ImportedOsgiServiceProxy mockProxy = new ImportedOsgiServiceProxy() {

			public ServiceReferenceProxy getServiceReference() {
				return new StaticServiceReferenceProxy(ref);
			}
		};

		return mockProxy;
	}

	public Class getObjectType() {
		return ImportedOsgiServiceProxy.class;
	}

	public boolean isSingleton() {
		return false;
	}
}