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

package org.eclipse.gemini.blueprint.service.importer.support;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.osgi.framework.ServiceReference;

/**
 * {@link PropertyEditor} that converts an &lt;osgi:reference&gt; element into a
 * {@link ServiceReference}. That is, it allows conversion between a
 * Spring-managed OSGi service to a Spring-managed ServiceReference.
 * 
 * <p/> Automatically registered by
 * {@link org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext}
 * implementations.
 * 
 * @author Costin Leau
 * @see ImportedOsgiServiceProxy
 */
public class ServiceReferenceEditor extends PropertyEditorSupport {

	/**
	 * {@inheritDoc}
	 * 
	 * <p/> Converts the given text value to a ServiceReference.
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		throw new IllegalArgumentException("this property editor works only with "
				+ ImportedOsgiServiceProxy.class.getName());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p/> Converts the given value to a ServiceReference.
	 */
	public void setValue(Object value) {
		// nulls allowed
		if (value == null) {
			super.setValue(null);
			return;
		}

		if (value instanceof ImportedOsgiServiceProxy) {
			ImportedOsgiServiceProxy serviceProxy = (ImportedOsgiServiceProxy) value;
			super.setValue(serviceProxy.getServiceReference());
			return;
		}

		if (value instanceof ServiceReference) {
			super.setValue(value);
			return;
		}

		throw new IllegalArgumentException("Expected a service of type " + ImportedOsgiServiceProxy.class.getName()
				+ " but received type " + value.getClass());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p/> This implementation returns <code>null</code> to indicate that
	 * there is no appropriate text representation.
	 */
	public String getAsText() {
		return null;
	}
}