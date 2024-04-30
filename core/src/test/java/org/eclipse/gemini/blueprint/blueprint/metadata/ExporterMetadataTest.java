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
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.MapEntry;
import org.osgi.service.blueprint.reflect.RefMetadata;
import org.osgi.service.blueprint.reflect.RegistrationListener;
import org.osgi.service.blueprint.reflect.ServiceMetadata;
import org.osgi.service.blueprint.reflect.Target;

/**
 * @author Costin Leau
 */

public class ExporterMetadataTest extends BaseMetadataTest {

	@Override
	protected String getConfig() {
		return "/org/eclipse/gemini/blueprint/blueprint/config/exporter-elements.xml";
	}

	private ServiceMetadata getReferenceMetadata(String name) {
		ComponentMetadata metadata = blueprintContainer.getComponentMetadata(name);
		assertTrue(metadata instanceof ServiceMetadata);
		ServiceMetadata referenceMetadata = (ServiceMetadata) metadata;
		assertEquals("the registered name doesn't match the component name", name, referenceMetadata.getId());
		return referenceMetadata;
	}

	@Test
	public void testSimpleBean() throws Exception {
		ServiceMetadata metadata = getReferenceMetadata("simple");
		assertEquals(ServiceMetadata.AUTO_EXPORT_DISABLED, metadata.getAutoExport());
		List<String> intfs = metadata.getInterfaces();
		assertEquals(1, intfs.size());
		assertEquals(Map.class.getName(), intfs.iterator().next());
		assertEquals(123, metadata.getRanking());
		assertTrue(metadata.getRegistrationListeners().isEmpty());

		assertTrue(metadata.getServiceComponent() instanceof RefMetadata);
		List<MapEntry> props = metadata.getServiceProperties();
		System.out.println(props);
		// assertEquals("lip", props.get("fat"));
	}

	@Test
	public void testNestedBean() throws Exception {
		ServiceMetadata metadata = getReferenceMetadata("nested");
		//assertEquals(ServiceMetadata.AUTO_EXPORT_ALL_CLASSES, metadata.getAutoExport());

		List<String> intfs = metadata.getInterfaces();
		assertEquals(2, intfs.size());
		Iterator<String> iterator = intfs.iterator();
		assertEquals(Map.class.getName(), iterator.next());
		assertEquals(Serializable.class.getName(), iterator.next());

		assertEquals(0, metadata.getRanking());

		Collection<RegistrationListener> listeners = metadata.getRegistrationListeners();
		Iterator<RegistrationListener> iter = listeners.iterator();
		RegistrationListener listener = iter.next();

		assertEquals("up", listener.getRegistrationMethod());
		assertEquals("down", listener.getUnregistrationMethod());
		assertEquals("listener", ((RefMetadata) listener.getListenerComponent()).getComponentId());

		listener = iter.next();
		assertEquals("up", listener.getRegistrationMethod());
		assertEquals("down", listener.getUnregistrationMethod());
		assertTrue(listener.getListenerComponent() instanceof Target);

		assertTrue(metadata.getServiceComponent() instanceof Target);
	}
}