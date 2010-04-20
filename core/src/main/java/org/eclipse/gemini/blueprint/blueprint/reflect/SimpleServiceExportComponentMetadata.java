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
import java.util.Map;

import org.eclipse.gemini.blueprint.service.exporter.support.DefaultInterfaceDetector;
import org.osgi.service.blueprint.reflect.MapEntry;
import org.osgi.service.blueprint.reflect.RegistrationListener;
import org.osgi.service.blueprint.reflect.ServiceMetadata;
import org.osgi.service.blueprint.reflect.Target;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * Default {@link ServiceMetadata} implementation based on Spring's {@link BeanDefinition}.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
class SimpleServiceExportComponentMetadata extends SimpleComponentMetadata implements ServiceMetadata {

	private static final String AUTO_EXPORT_PROP = "interfaceDetector";
	private static final String RANKING_PROP = "ranking";
	private static final String INTERFACES_PROP = "interfaces";
	private static final String SERVICE_NAME_PROP = "targetBeanName";
	private static final String SERVICE_INSTANCE_PROP = "target";
	private static final String SERVICE_PROPERTIES_PROP = "serviceProperties";
	private static final String LISTENERS_PROP = "listeners";
	private static final String LAZY_LISTENERS = "lazyListeners";

	private final int autoExport;
	private final List<String> interfaces;
	private final int ranking;
	private final Target component;
	private final List<MapEntry> serviceProperties;
	private final Collection<RegistrationListener> listeners;
	private final int activation;

	/**
	 * Constructs a new <code>SpringServiceExportComponentMetadata</code> instance.
	 * 
	 * @param name bean name
	 * @param definition bean definition
	 */
	@SuppressWarnings("unchecked")
	public SimpleServiceExportComponentMetadata(String name, BeanDefinition definition) {
		super(name, definition);

		MutablePropertyValues pvs = definition.getPropertyValues();

		DefaultInterfaceDetector autoExp = (DefaultInterfaceDetector) MetadataUtils.getValue(pvs, AUTO_EXPORT_PROP);
		// convert the internal numbers
		autoExport = autoExp.ordinal() + 1;

		// ranking
		if (pvs.contains(RANKING_PROP)) {
			String rank = (String) MetadataUtils.getValue(pvs, RANKING_PROP);
			ranking = Integer.valueOf(rank).intValue();
		} else {
			ranking = 0;
		}

		// component
		if (pvs.contains(SERVICE_NAME_PROP)) {
			String compName = (String) MetadataUtils.getValue(pvs, SERVICE_NAME_PROP);
			component = new SimpleRefMetadata(compName);
		} else {
			component = (Target) ValueFactory.buildValue(MetadataUtils.getValue(pvs, SERVICE_INSTANCE_PROP));
		}

		// interfaces
		Object value = MetadataUtils.getValue(pvs, INTERFACES_PROP);

		if (value != null) {
			List<String> intfs = new ArrayList<String>(4);
			// interface attribute used
			if (value instanceof String) {
				intfs.add((String) value);
			}

			else {
				if (value instanceof Collection) {
					Collection<TypedStringValue> values = (Collection) value;

					for (TypedStringValue tsv : values) {
						intfs.add(tsv.getValue());
					}
				}
			}
			interfaces = Collections.unmodifiableList(intfs);
		} else {
			interfaces = Collections.emptyList();
		}

		// service properties
		if (pvs.contains(SERVICE_PROPERTIES_PROP)) {
			Map props = (Map) MetadataUtils.getValue(pvs, SERVICE_PROPERTIES_PROP);
			serviceProperties = ValueFactory.getEntries(props);
		} else {
			serviceProperties = Collections.emptyList();
		}

		// listeners
		List<RegistrationListener> foundListeners = new ArrayList<RegistrationListener>(4);
		List<? extends AbstractBeanDefinition> listenerDefinitions = (List) MetadataUtils.getValue(pvs, LISTENERS_PROP);

		if (listenerDefinitions != null) {
			for (AbstractBeanDefinition beanDef : listenerDefinitions) {
				foundListeners.add(new SimpleRegistrationListener(beanDef));
			}
		}

		listeners = Collections.unmodifiableCollection(foundListeners);

		Boolean bool = (Boolean) MetadataUtils.getValue(pvs, LAZY_LISTENERS);
		activation =
				(bool != null ? (bool.booleanValue() ? ACTIVATION_LAZY : ACTIVATION_EAGER) : super.getActivation());

	}

	public int getAutoExport() {
		return autoExport;
	}

	public List<String> getInterfaces() {
		return interfaces;
	}

	public int getRanking() {
		return ranking;
	}

	public Collection<RegistrationListener> getRegistrationListeners() {
		return listeners;
	}

	public Target getServiceComponent() {
		return component;
	}

	public List<MapEntry> getServiceProperties() {
		return serviceProperties;
	}

	@Override
	public int getActivation() {
		return activation;
	}
}