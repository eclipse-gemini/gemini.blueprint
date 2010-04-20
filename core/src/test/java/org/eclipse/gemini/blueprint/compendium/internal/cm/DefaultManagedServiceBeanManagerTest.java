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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.compendium.MultipleSetters;
import org.eclipse.gemini.blueprint.compendium.OneSetter;
import org.eclipse.gemini.blueprint.compendium.internal.cm.DefaultManagedServiceBeanManager;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * 
 * @author Costin Leau
 * 
 */
public class DefaultManagedServiceBeanManagerTest extends TestCase {

	private DefaultManagedServiceBeanManager msbm;
	private ConfigurationAdminManager cam;
	private Map configuration;

	protected void setUp() throws Exception {
		cam = new ConfigurationAdminManager("bla", new MockBundleContext()) {
			public Map getConfiguration() {
				return configuration;
			}
		};
	}

	protected void tearDown() throws Exception {
		msbm = null;
		cam = null;
	}

	private Object getUpdateCallback() {
		return TestUtils.getFieldValue(msbm, "updateCallback");
	}

	private Map getBeanMap() {
		return (Map) TestUtils.getFieldValue(msbm, "instanceRegistry");
	}

	public void testNoUpdateStrategy() {
		msbm = new DefaultManagedServiceBeanManager(false, null, cam, null);
		assertNull(getUpdateCallback());
	}

	public void testBeanManagedUpdateStrategy() {
		msbm = new DefaultManagedServiceBeanManager(false, "update", cam, null);
		assertTrue(getUpdateCallback().getClass().getName().endsWith("BeanManagedUpdate"));
	}

	public void testContainerManagedUpdateStrategy() {
		msbm = new DefaultManagedServiceBeanManager(true, null, cam, null);
		assertTrue(getUpdateCallback().getClass().getName().endsWith("ContainerManagedUpdate"));
	}

	public void testChainedManagedUpdateStrategy() {
		msbm = new DefaultManagedServiceBeanManager(true, "update", cam, null);
		assertTrue(getUpdateCallback().getClass().getName().endsWith("ChainedManagedUpdate"));
	}

	public void testRegister() {
		configuration = new HashMap();
		msbm = new DefaultManagedServiceBeanManager(false, null, cam, null);
		Object bean = new Object();
		assertSame(bean, msbm.register(bean));
		assertTrue(getBeanMap().containsValue(bean));
	}

	public void testUnregister() {
		msbm = new DefaultManagedServiceBeanManager(false, null, cam, null);
		Object bean = new Object();
		assertSame(bean, msbm.register(bean));
		assertTrue(getBeanMap().containsValue(bean));
		msbm.unregister(bean);
		assertFalse(getBeanMap().containsValue(bean));
	}

	public void testUpdated() {
		msbm = new DefaultManagedServiceBeanManager(false, null, cam, null);
	}

	public void testInjectInfoSimple() {
		Map props = new HashMap();
		props.put("prop", "14");
		OneSetter instance = new OneSetter();
		msbm = new DefaultManagedServiceBeanManager(true, null, cam, null);
		msbm.applyInitialInjection(instance, props);
		assertEquals(new Long(14), instance.getProp());
	}

	public void testMultipleSetters() {
		Map props = new HashMap();
		props.put("prop", "14");
		props.put("integer", new Double(14));
		props.put("dbl", new Float(14));
		props.put("none", "14");
		props.put("float", "14");
		MultipleSetters instance = new MultipleSetters();
		msbm = new DefaultManagedServiceBeanManager(true, null, cam, null);
		msbm.applyInitialInjection(instance, props);
		assertEquals(new Double(14), instance.getDbl());
		assertEquals(14, instance.getInteger());
		assertEquals(new Long(14), instance.getProp());
		assertEquals(new Float(0), new Float(instance.getFloat()));
	}
}