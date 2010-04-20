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
import java.util.List;

import org.eclipse.gemini.blueprint.blueprint.TestComponent;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.BeanProperty;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.ReferenceListMetadata;
import org.osgi.service.blueprint.reflect.ReferenceMetadata;

/**
 * 
 * @author Costin Leau
 * 
 */
public class DefaultsTest extends BaseMetadataTest {

	@Override
	protected String getConfig() {
		return "/org/eclipse/gemini/blueprint/blueprint/config/blueprint-defaults.xml";
	}

	public void testDefaultsOnNestedBeans() throws Exception {
		ComponentMetadata metadata = blueprintContainer.getComponentMetadata("nested");
		assertEquals(ComponentMetadata.ACTIVATION_LAZY, metadata.getActivation());
		assertNull("null scope expected", ((BeanMetadata)metadata).getScope());
		BeanMetadata meta = (BeanMetadata) metadata;
		List<BeanProperty> props = meta.getProperties();
		assertEquals(2, props.size());
		BeanProperty propA = props.get(0);
		ReferenceMetadata nestedRef = (ReferenceMetadata) propA.getValue();
		assertEquals(ReferenceMetadata.AVAILABILITY_MANDATORY, nestedRef.getAvailability());
		assertEquals(300, nestedRef.getTimeout());

		BeanProperty propB = props.get(1);
		ReferenceListMetadata nestedList = (ReferenceListMetadata) propB.getValue();
		assertEquals(ReferenceMetadata.AVAILABILITY_OPTIONAL, nestedList.getAvailability());
		assertEquals(ReferenceListMetadata.USE_SERVICE_REFERENCE, nestedList.getMemberType());
	}

	public void testBeanInstances() throws Exception {
		TestComponent componentInstance = (TestComponent) blueprintContainer.getComponentInstance("nested");
		Collection propB = (Collection) componentInstance.getPropB();
		System.out.println(propB.size());
	}

	public void testDefaultActivation() throws Exception {
		ComponentMetadata metadata = blueprintContainer.getComponentMetadata("lazy-ref");
		assertEquals(ReferenceMetadata.ACTIVATION_LAZY, metadata.getActivation());
		metadata = blueprintContainer.getComponentMetadata("lazy-col");
		assertEquals(ReferenceMetadata.ACTIVATION_LAZY, metadata.getActivation());
		metadata = blueprintContainer.getComponentMetadata("lazy-service");
		assertEquals(ReferenceMetadata.ACTIVATION_LAZY, metadata.getActivation());
	}
	
	public void testOverriddenActivation() throws Exception {
		ComponentMetadata metadata = blueprintContainer.getComponentMetadata("overriden-activation");
		assertEquals(ReferenceMetadata.ACTIVATION_EAGER, metadata.getActivation());
		metadata = blueprintContainer.getComponentMetadata("overriden-service");
		assertEquals(ReferenceMetadata.ACTIVATION_EAGER, metadata.getActivation());
	}
}