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

package org.eclipse.gemini.blueprint.blueprint.container.support.internal.config;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.blueprint.config.internal.ParsingUtils;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.core.Ordered;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Simple processor for sorting out cycles between beans. Inspects the construction relationship between beans to
 * provide hints to the container. Specifically, it forces the creation of any beans referred inside the construction
 * through the 'depends-on' attribute on the inspected bean.
 * 
 * @author Costin Leau
 */
public class CycleOrderingProcessor implements BeanFactoryPostProcessor, Ordered {

	public static final String SYNTHETIC_DEPENDS_ON =
			"org.eclipse.gemini.blueprint.blueprint.container.support.internal.config.dependson";

	/** logger */
	private static final Log log = LogFactory.getLog(CycleOrderingProcessor.class);

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

		boolean trace = log.isTraceEnabled();

		String[] names = beanFactory.getBeanDefinitionNames();
		for (String name : names) {
			BeanDefinition definition = beanFactory.getBeanDefinition(name);
			if (definition.hasAttribute(ParsingUtils.BLUEPRINT_MARKER_NAME)) {
				ConstructorArgumentValues cArgs = definition.getConstructorArgumentValues();
				if (trace)
					log.trace("Inspecting cycles for (blueprint) bean " + name);

				tag(cArgs.getGenericArgumentValues(), name, definition);
				tag(cArgs.getIndexedArgumentValues().values(), name, definition);
			}
		}
	}

	private void tag(Collection<ValueHolder> values, String name, BeanDefinition definition) {
		boolean trace = log.isTraceEnabled();

		for (ValueHolder value : values) {
			Object val = value.getValue();
			if (val instanceof BeanMetadataElement) {
				if (val instanceof RuntimeBeanReference) {
					String beanName = ((RuntimeBeanReference) val).getBeanName();

					if (trace) {
						log.trace("Adding (cycle breaking) depends-on on " + name + " to " + beanName);
					}

					addSyntheticDependsOn(definition, beanName);
				}
			}
		}
	}

	private void addSyntheticDependsOn(BeanDefinition definition, String beanName) {
		if (StringUtils.hasText(beanName)) {
			String[] dependsOn = definition.getDependsOn();
			if (dependsOn != null && dependsOn.length > 0) {
				for (String dependOn : dependsOn) {
					if (beanName.equals(dependOn)) {
						return;
					}
				}
			}

			// add depends on
			dependsOn = (String[]) ObjectUtils.addObjectToArray(dependsOn, beanName);
			definition.setDependsOn(dependsOn);
			Collection<String> markers = (Collection<String>) definition.getAttribute(SYNTHETIC_DEPENDS_ON);
			if (markers == null) {
				markers = new ArrayList<String>(2);
				definition.setAttribute(SYNTHETIC_DEPENDS_ON, markers);
			}
			markers.add(beanName);
		}
	}

	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
