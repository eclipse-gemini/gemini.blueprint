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

import java.util.Collection;
import java.util.List;

import org.eclipse.gemini.blueprint.service.exporter.support.DefaultInterfaceDetector;
import org.osgi.service.blueprint.reflect.BeanArgument;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.BeanProperty;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.ReferenceListMetadata;
import org.osgi.service.blueprint.reflect.ReferenceMetadata;
import org.osgi.service.blueprint.reflect.ServiceMetadata;
import org.osgi.service.blueprint.reflect.ServiceReferenceMetadata;
import org.osgi.service.blueprint.reflect.Target;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;

/**
 * Adapter factory that allows translating OSGi Blueprint metadata into Spring's {@link BeanDefinition}.
 * 
 * @author Costin Leau
 */
class BeanDefinitionFactory implements MetadataConstants {

	BeanDefinition buildBeanDefinitionFor(ComponentMetadata metadata) {
		// shortcut (to avoid re-rewrapping)
		if (metadata instanceof SimpleComponentMetadata) {
			return ((SimpleComponentMetadata) metadata).getBeanDefinition();
		}

		AbstractBeanDefinition definition = buildBeanDefinition(metadata);
		// add common properties
		definition.setAttribute(MetadataConstants.COMPONENT_METADATA_ATTRIBUTE, metadata);
		definition.setAttribute(MetadataConstants.COMPONENT_NAME, metadata.getId());

		// Set<String> dependencies = metadata.;
		// definition.setDependsOn(dependencies.toArray(new String[dependencies.size()]));
		throw new UnsupportedOperationException("move depends on for BeanMetadata");

		// return definition;
	}

	private AbstractBeanDefinition buildBeanDefinition(ComponentMetadata metadata) {

		if (metadata instanceof BeanMetadata) {
			return buildLocalComponent((BeanMetadata) metadata);
		}

		if (metadata instanceof ServiceMetadata) {
			return buildExporter((ServiceMetadata) metadata);
		}

		if (metadata instanceof ServiceReferenceMetadata) {
			if (metadata instanceof ReferenceListMetadata) {
				return buildReferenceCollection((ReferenceListMetadata) metadata);
			}
			if (metadata instanceof ReferenceMetadata) {
				return buildReferenceProxy((ReferenceMetadata) metadata);
			}
		}

		// unknown rich metadata type, unable to perform conversion
		throw new IllegalArgumentException("Unknown metadata type" + metadata.getClass());
	}

	@SuppressWarnings("unchecked")
	private AbstractBeanDefinition buildLocalComponent(BeanMetadata metadata) {
		// add basic definition properties
		BeanDefinitionBuilder builder =
				BeanDefinitionBuilder.genericBeanDefinition(metadata.getClassName()).setInitMethodName(
						metadata.getInitMethod()).setDestroyMethodName(metadata.getDestroyMethod()).setLazyInit(
						getLazy(metadata)).setScope(metadata.getScope());

		// add factory-method/factory-bean
		String factoryMethod = metadata.getFactoryMethod();
		if (StringUtils.hasText(factoryMethod)) {
			builder.setFactoryMethod(factoryMethod);
			Target factory = metadata.getFactoryComponent();
			if (factory != null) {
				builder.getRawBeanDefinition().setFactoryBeanName(((ComponentMetadata) factory).getId());
			}
		}

		// add constructor props
		List<BeanArgument> beanArguments = metadata.getArguments();
		ConstructorArgumentValues cargs = builder.getRawBeanDefinition().getConstructorArgumentValues();

		for (BeanArgument arg : beanArguments) {
			int index = arg.getIndex();
			Object val = BeanMetadataElementFactory.buildBeanMetadata(arg.getValue());
			if (index > -1) {
				cargs.addGenericArgumentValue(val, arg.getValueType());
			} else {
				cargs.addIndexedArgumentValue(index, val, arg.getValueType());
			}

		}

		// add property values
		Collection<BeanProperty> props = metadata.getProperties();
		MutablePropertyValues pvs = new MutablePropertyValues();

		for (BeanProperty injection : props) {
			pvs.addPropertyValue(injection.getName(), BeanMetadataElementFactory
					.buildBeanMetadata(injection.getValue()));
		}

		return builder.getBeanDefinition();
	}

	private boolean getLazy(ComponentMetadata metadata) {
		return (metadata.getActivation() == ComponentMetadata.ACTIVATION_LAZY ? true : false);
	}

	private AbstractBeanDefinition buildExporter(ServiceMetadata metadata) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(EXPORTER_CLASS);
		// add properties
		builder.addPropertyValue(EXPORTER_RANKING_PROP, metadata.getRanking());
		builder.addPropertyValue(EXPORTER_INTFS_PROP, metadata.getInterfaces());
		builder.addPropertyValue(EXPORTER_PROPS_PROP, metadata.getServiceProperties());
		builder.addPropertyValue(EXPORTER_AUTO_EXPORT_PROP,
				DefaultInterfaceDetector.values()[metadata.getAutoExport() - 1]);

		BeanMetadataElement beanMetadata = BeanMetadataElementFactory.buildBeanMetadata(metadata.getServiceComponent());
		if (beanMetadata instanceof RuntimeBeanReference) {
			builder.addPropertyValue(EXPORTER_TARGET_BEAN_NAME_PROP, beanMetadata);
		} else {
			builder.addPropertyValue(EXPORTER_TARGET_BEAN_PROP, beanMetadata);
		}

		// FIXME: add registration listeners

		return builder.getBeanDefinition();
	}

	private AbstractBeanDefinition buildReferenceCollection(ReferenceListMetadata metadata) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MULTI_SERVICE_IMPORTER_CLASS);
		addServiceReferenceProperties(metadata, builder);
		throw new UnsupportedOperationException("not implemented yet");
		// return builder.getBeanDefinition();
	}

	private AbstractBeanDefinition buildReferenceProxy(ReferenceMetadata metadata) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SINGLE_SERVICE_IMPORTER_CLASS);
		addServiceReferenceProperties(metadata, builder);
		throw new UnsupportedOperationException("not implemented yet");
		// return builder.getBeanDefinition();
	}

	private void addServiceReferenceProperties(ServiceReferenceMetadata referenceMetadata, BeanDefinitionBuilder builder) {
		builder.addPropertyValue(IMPORTER_FILTER_PROP, referenceMetadata.getFilter());
		builder.addPropertyValue(IMPORTER_INTFS_PROP, referenceMetadata.getInterface());
		// FIXME: add binding listeners
	}
}