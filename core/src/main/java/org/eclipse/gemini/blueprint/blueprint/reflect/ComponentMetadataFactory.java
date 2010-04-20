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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.gemini.blueprint.config.internal.AbstractReferenceDefinitionParser;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReferenceFactoryBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * Internal class used for adapting Spring's bean definition to OSGi Blueprint metadata. Used by {@link MetadataFactory}
 * which acts as a facade.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
class ComponentMetadataFactory implements MetadataConstants {

	private static final String BEAN_REF_FB_CLASS_NAME = BeanReferenceFactoryBean.class.getName();
	private static final String GENERATED_REF = AbstractReferenceDefinitionParser.GENERATED_REF;
	private static final String PROMOTED_REF = AbstractReferenceDefinitionParser.PROMOTED_REF;
	private static final String REGEX =
			"\\.org\\.springframework\\.osgi\\.service\\.importer\\.support\\.OsgiService(?:Collection)*ProxyFactoryBean#\\d+#\\d+";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	private static final String GENERATED_END = "#generated";
	private static final String GENERATED_START = ".org.eclipse.gemini.blueprint.service.importer.support.OsgiService";
	private static final String GENERATED_MIDDLE = "ProxyFactoryBean#";

	/**
	 * Builds a component metadata from the given bean definition.
	 * 
	 * @param name bean name
	 * @param beanDefinition
	 * @return
	 */
	static ComponentMetadata buildMetadata(String name, BeanDefinition beanDefinition) {
		// shortcut (to avoid re-re-wrapping)
		Object metadata = beanDefinition.getAttribute(COMPONENT_METADATA_ATTRIBUTE);
		if (metadata instanceof ComponentMetadata)
			return (ComponentMetadata) metadata;

		// if no name has been given, look for one
		if (name == null) {
			name = (String) beanDefinition.getAttribute(COMPONENT_NAME);
		}

		if (isServiceExporter(beanDefinition)) {
			return new SimpleServiceExportComponentMetadata(name, beanDefinition);
		}

		if (isSingleServiceImporter(beanDefinition)) {
			return new SimpleReferenceMetadata(name, beanDefinition);
		}
		if (isCollectionImporter(beanDefinition)) {
			return new SimpleReferenceListMetadata(name, beanDefinition);
		}

		BeanDefinition original = unwrapImporterReference(beanDefinition);
		if (original != null) {
			return buildMetadata(null, original);
		}

		if (isEnvironmentManager(beanDefinition)) {
			return new EnvironmentManagerMetadata(name);
		}

		return new SimpleBeanMetadata(name, beanDefinition);
	}

	private static boolean isServiceExporter(BeanDefinition beanDefinition) {
		return checkBeanDefinitionClassCompatibility(beanDefinition, EXPORTER_CLASS);
	}

	private static boolean isSingleServiceImporter(BeanDefinition beanDefinition) {
		return checkBeanDefinitionClassCompatibility(beanDefinition, SINGLE_SERVICE_IMPORTER_CLASS);
	}

	private static boolean isCollectionImporter(BeanDefinition beanDefinition) {
		return checkBeanDefinitionClassCompatibility(beanDefinition, MULTI_SERVICE_IMPORTER_CLASS);
	}

	static BeanDefinition unwrapImporterReference(BeanDefinition beanDefinition) {
		if (BEAN_REF_FB_CLASS_NAME.equals(beanDefinition.getBeanClassName())) {
			// check special DM case of nested mandatory
			// references being promoted to top level beans
			if (beanDefinition instanceof AbstractBeanDefinition) {
				AbstractBeanDefinition abd = (AbstractBeanDefinition) beanDefinition;
				if (abd.isSynthetic() && abd.hasAttribute(GENERATED_REF)) {
					BeanDefinition actual = abd.getOriginatingBeanDefinition();
					return actual;
				}
			}
		}

		return null;
	}

	private static boolean isEnvironmentManager(BeanDefinition beanDefinition) {
		return checkBeanDefinitionClassCompatibility(beanDefinition, ENV_FB_CLASS);
	}

	private static boolean checkBeanDefinitionClassCompatibility(BeanDefinition definition, Class<?> clazz) {
		if (definition instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition abstractDefinition = (AbstractBeanDefinition) definition;
			if (abstractDefinition.hasBeanClass()) {
				Class<?> beanClass = abstractDefinition.getBeanClass();
				return clazz.isAssignableFrom(beanClass);
			}
		}
		return (clazz.getName().equals(definition.getBeanClassName()));
	}

	static Collection<ComponentMetadata> buildNestedMetadata(BeanDefinition beanDefinition) {
		List<ComponentMetadata> col = new ArrayList<ComponentMetadata>(4);
		processBeanDefinition(beanDefinition, col);
		// remove the first definition
		col.remove(0);
		return col;
	}

	private static void processBeanMetadata(BeanMetadataElement metadata, Collection<ComponentMetadata> to) {
		if (metadata instanceof BeanDefinition) {
			processBeanDefinition((BeanDefinition) metadata, to);
		}

		else if (metadata instanceof BeanDefinitionHolder) {
			BeanDefinitionHolder bh = (BeanDefinitionHolder) metadata;
			processBeanDefinition(bh.getBeanDefinition(), to);
		}

		else if (metadata instanceof Mergeable && metadata instanceof Iterable) {
			processIterable((Iterable) metadata, to);
		}
	}

	private static void processBeanDefinition(BeanDefinition definition, Collection<ComponentMetadata> to) {
		to.add(buildMetadata(null, definition));

		// start with constructors
		ConstructorArgumentValues cavs = definition.getConstructorArgumentValues();
		// generic values
		List<ValueHolder> genericValues = cavs.getGenericArgumentValues();
		for (ValueHolder valueHolder : genericValues) {
			Object value = MetadataUtils.getValue(valueHolder);
			if (value instanceof BeanMetadataElement) {
				processBeanMetadata((BeanMetadataElement) value, to);
			}
		}
		// indexed ones
		Map<Integer, ValueHolder> indexedValues = cavs.getIndexedArgumentValues();
		for (ValueHolder valueHolder : indexedValues.values()) {
			Object value = MetadataUtils.getValue(valueHolder);
			if (value instanceof BeanMetadataElement) {
				processBeanMetadata((BeanMetadataElement) value, to);
			}
		}

		// now property values
		PropertyValues pvs = definition.getPropertyValues();
		for (PropertyValue pv : pvs.getPropertyValues()) {
			Object value = MetadataUtils.getValue(pv);
			if (value instanceof BeanMetadataElement) {
				processBeanMetadata((BeanMetadataElement) value, to);
			}
		}
	}

	private static void processIterable(Iterable iterableMetadata, Collection<ComponentMetadata> to) {
		for (Object value : iterableMetadata) {
			if (value instanceof BeanMetadataElement) {
				processBeanMetadata((BeanMetadataElement) value, to);
			}
		}
	}

	public static List<ComponentMetadata> buildComponentMetadataFor(ConfigurableListableBeanFactory factory) {
		List<ComponentMetadata> metadata = new ArrayList<ComponentMetadata>();
		String[] components = factory.getBeanDefinitionNames();

		for (String beanName : components) {
			BeanDefinition definition = factory.getBeanDefinition(beanName);

			// filter generated definitions
			if (!definition.hasAttribute(PROMOTED_REF)) {
				// add metadata for top-level definitions
				metadata.add(MetadataFactory.buildComponentMetadataFor(beanName, definition));
				// look for nested ones
				metadata.addAll(MetadataFactory.buildNestedComponentMetadataFor(definition));
			}
		}

		return metadata;
	}

	// eliminate the names of promoted importers
	public static Set<String> filterIds(Set<String> components) {
		// search for pattern "
		// .org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean#N#N and
		// .org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean#N#N

		Set<String> filtered = new LinkedHashSet<String>(components.size());

		for (String string : components) {
			if (!(string.startsWith(GENERATED_START) && string.endsWith(GENERATED_END) && string
					.contains(GENERATED_MIDDLE))) {
				filtered.add(string);
			}
		}

		return filtered;
	}
}