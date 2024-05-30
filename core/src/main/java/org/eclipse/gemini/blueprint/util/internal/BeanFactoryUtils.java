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

package org.eclipse.gemini.blueprint.util.internal;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;

/**
 * Utility class for beans operations.
 * 
 * @author Costin Leau
 * 
 */
public abstract class BeanFactoryUtils {

	/**
	 * Return all beans depending directly or indirectly (transitively), on the bean identified by the beanName. When
	 * dealing with a FactoryBean, the factory itself can be returned or its product. Additional filtering can be
	 * executed through the type parameter. If no filtering is required, then null can be passed.
	 * 
	 * Note that depending on #rawFactoryBeans parameter, the type of the factory or its product can be used when doing
	 * the filtering.
	 * 
	 * @param beanFactory beans bean factory
	 * @param beanName root bean name
	 * @param rawFactoryBeans consider the factory bean itself or the its product
	 * @param type type of the beans returned (null to return all beans)
	 * @return bean names
	 */
	public static String[] getTransitiveDependenciesForBean(ConfigurableListableBeanFactory beanFactory,
			String beanName, boolean rawFactoryBeans, Class<?> type) {
		Assert.notNull(beanFactory, "beanFactory is required");
		Assert.hasText(beanName, "beanName is required");

		Assert.isTrue(beanFactory.containsBean(beanName), "no bean by name [" + beanName + "] can be found");

		Set<String> beans = new LinkedHashSet<String>(8);
		// used to break cycles between nested beans
		Set<String> innerBeans = new LinkedHashSet<String>(4);

		getTransitiveBeans(beanFactory, beanName, rawFactoryBeans, beans, innerBeans);

		if (type != null) {
			// filter by type
			for (Iterator<String> iter = beans.iterator(); iter.hasNext();) {
				String bean = iter.next();
				if (!beanFactory.isTypeMatch(bean, type)) {
					iter.remove();
				}
			}
		}

		return beans.toArray(new String[beans.size()]);
	}

	private static void getTransitiveBeans(ConfigurableListableBeanFactory beanFactory, String beanName,
			boolean rawFactoryBeans, Set<String> beanNames, Set<String> innerBeans) {
		String transformedBeanName = org.springframework.beans.factory.BeanFactoryUtils.transformedBeanName(beanName);
		// strip out '&' just in case
		String[] beans = beanFactory.getDependenciesForBean(transformedBeanName);

		for (int i = 0; i < beans.length; i++) {
			String bean = beans[i];
			// top-level beans
			if (beanFactory.containsBean(bean)) {
				// & if needed
				if (rawFactoryBeans && beanFactory.isFactoryBean(bean))
					bean = BeanFactory.FACTORY_BEAN_PREFIX + beans[i];

				if (!beanNames.contains(bean)) {
					beanNames.add(bean);
					getTransitiveBeans(beanFactory, bean, rawFactoryBeans, beanNames, innerBeans);
				}
			}
			// nested-beans are discarded from the main list but are tracked for dependencies to
			// top-level beans
			else {
				if (innerBeans.add(bean)) {
					getTransitiveBeans(beanFactory, bean, rawFactoryBeans, beanNames, innerBeans);
				}
			}
		}
	}
}