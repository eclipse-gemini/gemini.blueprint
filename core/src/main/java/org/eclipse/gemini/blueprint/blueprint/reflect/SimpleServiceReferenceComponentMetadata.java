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

package org.eclipse.gemini.blueprint.blueprint.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.osgi.service.blueprint.reflect.ReferenceListener;
import org.osgi.service.blueprint.reflect.ServiceReferenceMetadata;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * Default {@link ServiceReferenceComponentMetadata} implementation based on Spring's {@link BeanDefinition}.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
abstract class SimpleServiceReferenceComponentMetadata extends SimpleComponentMetadata implements
		ServiceReferenceMetadata {

	private static final String FILTER_PROP = "filter";
	private static final String INTERFACES_PROP = "interfaces";
	private static final String AVAILABILITY_PROP = "availability";
	private static final String SERVICE_NAME_PROP = "serviceBeanName";
	private static final String LISTENERS_PROP = "listeners";

	private final String componentName;
	private final String filter;
	private final int availability;
	private final String intf;
	private final Collection<ReferenceListener> listeners;

	/**
	 * Constructs a new <code>SpringServiceReferenceComponentMetadata</code> instance.
	 * 
	 * @param name bean name
	 * @param definition bean definition
	 */
	public SimpleServiceReferenceComponentMetadata(String name, BeanDefinition definition) {
		super(name, definition);

		MutablePropertyValues pvs = beanDefinition.getPropertyValues();
		componentName = (String) MetadataUtils.getValue(pvs, SERVICE_NAME_PROP);
		filter = (String) MetadataUtils.getValue(pvs, FILTER_PROP);

		Availability avail = (Availability) MetadataUtils.getValue(pvs, AVAILABILITY_PROP);

		availability =
				(Availability.OPTIONAL.equals(avail) ? ServiceReferenceMetadata.AVAILABILITY_OPTIONAL
						: ServiceReferenceMetadata.AVAILABILITY_MANDATORY);

		// interfaces
		Object value = MetadataUtils.getValue(pvs, INTERFACES_PROP);

		// interface attribute used
		if (value instanceof String) {
			intf = (String) value;
		}

		else {
			if (value instanceof Collection) {
				Collection<TypedStringValue> values = (Collection) value;

				intf = values.iterator().next().getValue();
			} else {
				intf = null;
			}
		}

		// listeners
		List<ReferenceListener> foundListeners = new ArrayList<ReferenceListener>(4);
		List<? extends AbstractBeanDefinition> listenerDefinitions = (List) MetadataUtils.getValue(pvs, LISTENERS_PROP);

		if (listenerDefinitions != null) {
			for (AbstractBeanDefinition beanDef : listenerDefinitions) {
				foundListeners.add(new SimpleReferenceListenerMetadata(beanDef));
			}
		}

		listeners = Collections.unmodifiableCollection(foundListeners);
	}

	public int getAvailability() {
		return availability;
	}

	public String getComponentName() {
		return componentName;
	}

	public String getFilter() {
		return filter;
	}

	public String getInterface() {
		return intf;
	}

	public Collection<ReferenceListener> getReferenceListeners() {
		return listeners;
	}
}