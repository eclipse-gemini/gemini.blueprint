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

package org.eclipse.gemini.blueprint.blueprint.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.BeanProperty;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.Metadata;
import org.osgi.service.blueprint.reflect.RefMetadata;
import org.osgi.service.blueprint.reflect.ReferenceListMetadata;
import org.osgi.service.blueprint.reflect.ReferenceListener;
import org.osgi.service.blueprint.reflect.ReferenceMetadata;
import org.osgi.service.blueprint.reflect.ServiceReferenceMetadata;
import org.osgi.service.blueprint.reflect.Target;

/**
 * @author Costin Leau
 */

public class ImporterMetadataTest extends BaseMetadataTest {

	@Override
	protected String getConfig() {
		return "/org/eclipse/gemini/blueprint/blueprint/config/importer-elements.xml";
	}

	private ServiceReferenceMetadata getReferenceMetadata(String name) {
		ComponentMetadata metadata = blueprintContainer.getComponentMetadata(name);
		assertTrue(metadata instanceof ServiceReferenceMetadata);
		ServiceReferenceMetadata referenceMetadata = (ServiceReferenceMetadata) metadata;
		assertEquals("the registered name doesn't match the component name", name, referenceMetadata.getId());
		return referenceMetadata;
	}

	@Test
	public void testSimpleBean() throws Exception {
		ServiceReferenceMetadata metadata = getReferenceMetadata("simple");
		System.out.println(metadata.getClass().getName());
		assertNull(metadata.getFilter());
		String intf = metadata.getInterface();
		assertEquals(Cloneable.class.getName(), intf);
		assertEquals(ReferenceMetadata.AVAILABILITY_MANDATORY, metadata.getAvailability());
		assertEquals(0, metadata.getReferenceListeners().size());
	}

	@Test
	public void testBeanWithOptions() throws Exception {
		ServiceReferenceMetadata metadata = getReferenceMetadata("options");
		assertEquals("(name=foo)", metadata.getFilter());
		String intf = metadata.getInterface();
		assertEquals(Serializable.class.getName(), intf);
		assertEquals(ReferenceMetadata.AVAILABILITY_OPTIONAL, metadata.getAvailability());
		Collection<ReferenceListener> listeners = metadata.getReferenceListeners();
		assertEquals(1, listeners.size());
	}

	@Test
	public void testMultipleInterfaces() throws Exception {
		ServiceReferenceMetadata metadata = getReferenceMetadata("multipleInterfaces");
		String intf = metadata.getInterface();
		assertEquals(Cloneable.class.getName(), intf);
		assertEquals(ReferenceMetadata.AVAILABILITY_MANDATORY, metadata.getAvailability());
		assertEquals(0, metadata.getReferenceListeners().size());
	}

	@Test
	public void testMultipleListeners() throws Exception {
		ServiceReferenceMetadata metadata = getReferenceMetadata("multipleListeners");
		Collection<ReferenceListener> listeners = metadata.getReferenceListeners();
		assertEquals(3, listeners.size());

		Iterator<ReferenceListener> iterator = listeners.iterator();
		ReferenceListener listener = iterator.next();
		assertEquals("bindM", listener.getBindMethod());
		assertEquals("unbindM", listener.getUnbindMethod());
		assertTrue(listener.getListenerComponent() instanceof RefMetadata);
		listener = iterator.next();
		assertTrue(listener.getListenerComponent() instanceof Target);
		listener = iterator.next();
		assertTrue(listener.getListenerComponent() instanceof RefMetadata);
	}

	@Test
	public void testTimeout() throws Exception {
		ServiceReferenceMetadata metadata = getReferenceMetadata("timeout");
		assertTrue(metadata instanceof ReferenceMetadata);
		assertEquals(1234, ((ReferenceMetadata) metadata).getTimeout());
	}

	@Test
	public void testNestedMandatoryReference() throws Exception {
		BeanMetadata metadata = (BeanMetadata) blueprintContainer.getComponentMetadata("nestedReference");
		BeanProperty prop = (BeanProperty) metadata.getProperties().get(0);
		Metadata value = prop.getValue();
		assertTrue(value instanceof ReferenceMetadata);
		ReferenceMetadata ref = (ReferenceMetadata) value;
		assertEquals(1000, ref.getTimeout());
		assertEquals(ReferenceMetadata.ACTIVATION_LAZY, ref.getActivation());
	}

	@Test
	public void testNestedMandatoryCollectionReference() throws Exception {
		BeanMetadata metadata = (BeanMetadata) blueprintContainer.getComponentMetadata("nestedCollectionReference");
		BeanProperty prop = (BeanProperty) metadata.getProperties().get(0);
		Metadata value = prop.getValue();
		assertTrue(value instanceof ReferenceListMetadata);
		ReferenceListMetadata ref = (ReferenceListMetadata) value;
		assertEquals(ReferenceListMetadata.USE_SERVICE_REFERENCE, ref.getMemberType());
		assertEquals(ReferenceMetadata.ACTIVATION_LAZY, ref.getActivation());
	}

}