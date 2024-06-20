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

package org.eclipse.gemini.blueprint.iandt.serviceProxyFactoryBean;

import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;
import org.springframework.core.InfrastructureProxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.*;

/**
 * @author Costin Leau
 * 
 */
public class ServiceRefAwareWithMultiServiceTest extends ServiceBaseTest {

	private OsgiServiceCollectionProxyFactoryBean fb;


	protected void onSetUp() throws Exception {
		fb = new OsgiServiceCollectionProxyFactoryBean();
		fb.setBundleContext(bundleContext);
		fb.setBeanClassLoader(getClass().getClassLoader());
	}

	protected void onTearDown() throws Exception {
		fb = null;
	}

	@Test
	public void testProxyForMultipleCardinality() throws Exception {
		fb.setAvailability(Availability.OPTIONAL);
		fb.setInterfaces(new Class<?>[] { Date.class });
		fb.afterPropertiesSet();

		List registrations = new ArrayList(3);

		long time = 321;
		Date dateA = new Date(time);

		try {
			Object result = fb.getObject();
			assertTrue(result instanceof Collection);
			Collection col = (Collection) result;

			assertTrue(col.isEmpty());
			Iterator iter = col.iterator();

			assertFalse(iter.hasNext());
			registrations.add(publishService(dateA));
			assertTrue(iter.hasNext());
			Object service = iter.next();
			assertTrue(service instanceof Date);
			assertEquals(time, ((Date) service).getTime());

			assertTrue(service instanceof ImportedOsgiServiceProxy);
			assertNotNull(((ImportedOsgiServiceProxy) service).getServiceReference());
			assertSame(dateA, ((InfrastructureProxy) service).getWrappedObject());

			assertFalse(iter.hasNext());
			time = 111;
			Date dateB = new Date(time);
			registrations.add(publishService(dateB));
			assertTrue(iter.hasNext());
			service = iter.next();
			assertTrue(service instanceof Date);
			assertEquals(time, ((Date) service).getTime());
			assertTrue(service instanceof ImportedOsgiServiceProxy);
			assertNotNull(((ImportedOsgiServiceProxy) service).getServiceReference());

			assertTrue(service instanceof InfrastructureProxy);
			assertSame(dateB, ((InfrastructureProxy) service).getWrappedObject());
		}
		finally {
			for (int i = 0; i < registrations.size(); i++) {
				((ServiceRegistration) registrations.get(i)).unregister();
			}
		}
	}
}
