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

package org.eclipse.gemini.blueprint.internal.util;

import org.eclipse.gemini.blueprint.util.internal.BeanFactoryUtils;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * @author Costin Leau
 * 
 */
@ContextConfiguration(locations = "/org/eclipse/gemini/blueprint/dependingBeans.xml")
public class BeanFactoryUtilsTest extends AbstractJUnit4SpringContextTests implements BeanFactoryAware {
	private ConfigurableListableBeanFactory bf;

	@Test
	public void testADependencies() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "a", false, null);
		assertTrue(ObjectUtils.isEmpty(deps));
	}

	@Test
	public void testBDependencies() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "b", false, null);
		assertTrue(ObjectUtils.isEmpty(deps));
	}

	@Test
	public void testCDependencies() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "c", false, null);
		assertTrue(Arrays.equals(new String[] { "b" }, deps));

	}

	@Test
	public void testIntDependencies() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "int", false, null);
		assertTrue(Arrays.equals(new String[] { "c", "b" }, deps));
	}

	@Test
	public void testTransitiveDependenciesForDependsOn() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "thread", false, null);
		assertTrue(Arrays.equals(new String[] { "buffer", "int", "c", "b" }, deps));
	}

	@Test
	public void testTransitiveFBDependencies() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "secondBuffer", true, null);
		assertTrue(Arrays.equals(new String[] { "&field", "thread", "buffer", "int", "c", "b" }, deps));
	}

	@Test
	public void testFiltering() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "secondBuffer", true, Number.class);
		assertTrue(Arrays.equals(new String[] { "int", "b" }, deps));
	}

	@Test
	public void testFilteringOnFB() {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "secondBuffer", true, FactoryBean.class);
		assertTrue(Arrays.equals(new String[] { "&field" }, deps));
	}

	@Test
	public void testNestedDependencies() throws Exception {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "nested", true, null);
		assertTrue(Arrays.equals(new String[] { "int", "c", "b" }, deps));
	}

	@Test
	public void testNestedFactoryDependencies() throws Exception {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "nestedFB", true, null);
		assertTrue(Arrays.equals(new String[] { "thread", "buffer", "int", "c", "b" }, deps));
	}

	@Test
	public void testNestedCycle() throws Exception {
		String[] deps = BeanFactoryUtils.getTransitiveDependenciesForBean(bf, "nestedCycle", true, null);
		assertTrue(Arrays.equals(new String[] { "nestedCycle", "thread", "buffer", "int", "c", "b" }, deps));
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.bf = (ConfigurableListableBeanFactory) beanFactory;
	}
}
