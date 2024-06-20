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

package org.eclipse.gemini.blueprint.compendium.internal.cm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.compendium.MultipleSetters;
import org.eclipse.gemini.blueprint.compendium.OneSetter;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Costin Leau
 * 
 */
public class DefaultManagedServiceBeanManagerTest {

	private DefaultManagedServiceBeanManager msbm;
	private ConfigurationAdminManager cam;
	private Map configuration;

	@Before
	public void setup() throws Exception {
		cam = new ConfigurationAdminManager("bla", new MockBundleContext()) {
			public Map getConfiguration() {
				return configuration;
			}
		};
	}

	@After
	public void tearDown() throws Exception {
		msbm = null;
		cam = null;
	}

	private Object getUpdateCallback() {
		return TestUtils.getFieldValue(msbm, "updateCallback");
	}

	private Map getBeanMap() {
		return (Map) TestUtils.getFieldValue(msbm, "instanceRegistry");
	}

	@Test
	public void testNoUpdateStrategy() {
		msbm = new DefaultManagedServiceBeanManager(false, null, cam, null);
		assertNull(getUpdateCallback());
	}

	@Test
	public void testBeanManagedUpdateStrategy() {
		msbm = new DefaultManagedServiceBeanManager(false, "update", cam, null);
		assertTrue(getUpdateCallback().getClass().getName().endsWith("BeanManagedUpdate"));
	}

	@Test
	public void testContainerManagedUpdateStrategy() {
		msbm = new DefaultManagedServiceBeanManager(true, null, cam, null);
		assertTrue(getUpdateCallback().getClass().getName().endsWith("ContainerManagedUpdate"));
	}

	@Test
	public void testChainedManagedUpdateStrategy() {
		msbm = new DefaultManagedServiceBeanManager(true, "update", cam, null);
		assertTrue(getUpdateCallback().getClass().getName().endsWith("ChainedManagedUpdate"));
	}

	@Test
	public void testRegister() {
		configuration = new HashMap();
		msbm = new DefaultManagedServiceBeanManager(false, null, cam, null);
		Object bean = new Object();
		assertSame(bean, msbm.register(bean));
		assertTrue(getBeanMap().containsValue(bean));
	}

	@Test
	public void testUnregister() {
		msbm = new DefaultManagedServiceBeanManager(false, null, cam, null);
		Object bean = new Object();
		assertSame(bean, msbm.register(bean));
		assertTrue(getBeanMap().containsValue(bean));
		msbm.unregister(bean);
		assertFalse(getBeanMap().containsValue(bean));
	}

	@Test
	public void testUpdated() {
		msbm = new DefaultManagedServiceBeanManager(false, null, cam, null);
	}

	@Test
	public void testInjectInfoSimple() {
		Map props = new HashMap();
		props.put("prop", "14");
		OneSetter instance = new OneSetter();
		msbm = new DefaultManagedServiceBeanManager(true, null, cam, null);
		msbm.applyInitialInjection(instance, props);
		assertEquals(Long.valueOf(14), instance.getProp());
	}

	@Test
	public void testMultipleSetters() {
		Map props = new HashMap();
		props.put("prop", "14");
		props.put("integer", Double.valueOf(14));
		props.put("dbl", Float.valueOf(14));
		props.put("none", "14");
		props.put("float", "14");
		MultipleSetters instance = new MultipleSetters();
		msbm = new DefaultManagedServiceBeanManager(true, null, cam, null);
		msbm.applyInitialInjection(instance, props);
		assertEquals(Double.valueOf(14), instance.getDbl());
		assertEquals(14, instance.getInteger());
		assertEquals(Long.valueOf(14), instance.getProp());
		assertEquals(Float.valueOf(0), Float.valueOf(instance.getFloat()));
	}
}