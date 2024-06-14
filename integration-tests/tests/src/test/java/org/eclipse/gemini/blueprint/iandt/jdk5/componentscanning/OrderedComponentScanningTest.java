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

package org.eclipse.gemini.blueprint.iandt.jdk5.componentscanning;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Shape;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.junit.Test;

/**
 * @author Costin Leau
 */
public class OrderedComponentScanningTest extends BaseIntegrationTest {

	private static final String BEAN_NAME = "componentBean";

	private ComponentBean bean;

	private Shape shape;

	@Override
	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt, sync-tail-bundle," + getSpringDMVersion() };
	}

	@Test
	public void testComponentExistence() throws Exception {
		assertTrue("component not found", applicationContext.containsBean(BEAN_NAME));
		assertNotNull("component not injected in the test", bean);
		assertNotNull("shape not injected in the test", shape);
	}

	@Test
	public void testAutowireInjection() throws Exception {
		assertNotNull(bean.getSetterInjection());
		assertNotNull(bean.getConstructorInjection());
		assertNotNull(bean.getFieldInjection());
	}

	@Test
	public void testInjectionIdentity() throws Exception {
		assertSame(shape, bean.getSetterInjection());
		assertSame(shape, bean.getConstructorInjection());
		assertSame(shape, bean.getFieldInjection());
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "/org/eclipse/gemini/blueprint/iandt/jdk5/componentscanning/context.xml" };
	}

	/**
	 * @param bean The bean to set.
	 */
	public void setBean(ComponentBean bean) {
		this.bean = bean;
	}

	/**
	 * @param shape The shape to set.
	 */
	public void setShape(Shape shape) {
		this.shape = shape;
	}

	protected boolean createManifestOnlyFromTestClass() {
		return false;
	}
}