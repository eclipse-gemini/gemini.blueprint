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

package org.eclipse.gemini.blueprint.mock;

import java.util.Dictionary;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * ServiceRegistration mock.
 * 
 * <p/> The mock allows the service properties modification (through
 * {@link #setProperties(Dictionary)}) as long as the underlying reference is
 * of type {@link MockServiceReference}.
 * 
 * @author Costin Leau
 * @param <S>
 * 
 */
public class MockServiceRegistration<S> implements ServiceRegistration<S> {

	private ServiceReference<S> reference;


	/**
	 * Constructs a new <code>MockServiceRegistration</code> instance using
	 * defaults.
	 * 
	 */
	public MockServiceRegistration() {
		this(null);
	}

	/**
	 * Constructs a new <code>MockServiceRegistration</code> instance with the
	 * given properties.
	 * 
	 * @param props registration properties
	 */
	public MockServiceRegistration(Dictionary props) {
		this(null, props);
	}

	/**
	 * Constructs a new <code>MockServiceRegistration</code> instance using
	 * the given class names and properties.
	 * 
	 * @param clazz
	 * @param props
	 */
	public MockServiceRegistration(String[] clazz, Dictionary props) {
		reference = new MockServiceReference(null, props, this, clazz);
	}

	public ServiceReference<S> getReference() {
		return reference;
	}

	/**
	 * Sets the service reference associated with this registration.
	 * 
	 * @param reference service reference
	 */
	public void setReference(ServiceReference<S> reference) {
		this.reference = reference;
	}

	public void setProperties(Dictionary props) {
		if (reference instanceof MockServiceReference)
			((MockServiceReference) reference).setProperties(props);
		else
			throw new IllegalArgumentException("cannot update properties - service reference is not a "
					+ MockServiceReference.class.getName());
	}

	public void unregister() {
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof MockServiceRegistration)
			return this.reference.equals(((MockServiceRegistration) obj).reference);
		return false;
	}

	public int hashCode() {
		return MockServiceRegistration.class.hashCode() * 13 + reference.hashCode();
	}
}