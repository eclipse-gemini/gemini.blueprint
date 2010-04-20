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

import java.util.Collection;
import java.util.Iterator;

import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.RefMetadata;
import org.osgi.service.blueprint.reflect.ReferenceListMetadata;
import org.osgi.service.blueprint.reflect.ReferenceListener;
import org.osgi.service.blueprint.reflect.ServiceReferenceMetadata;
import org.osgi.service.blueprint.reflect.Target;

/**
 * @author Costin Leau
 */
public class ImporterCollectionsMetadataTest extends BaseMetadataTest {

	@Override
	protected String getConfig() {
		return "/org/eclipse/gemini/blueprint/blueprint/config/importer-collections-elements.xml";
	}

	private ServiceReferenceMetadata getReferenceMetadata(String name) {
		ComponentMetadata metadata = blueprintContainer.getComponentMetadata(name);
		assertTrue(metadata instanceof ServiceReferenceMetadata);
		ServiceReferenceMetadata referenceMetadata = (ServiceReferenceMetadata) metadata;
		assertEquals("the registered name doesn't match the component name", name, referenceMetadata.getId());
		return referenceMetadata;
	}

	public void testSimpleList() throws Exception {
		ReferenceListMetadata metadata = (ReferenceListMetadata) getReferenceMetadata("simpleList");
	}

	public void testListeners() throws Exception {
		ReferenceListMetadata metadata = (ReferenceListMetadata) getReferenceMetadata("listeners");
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
}
