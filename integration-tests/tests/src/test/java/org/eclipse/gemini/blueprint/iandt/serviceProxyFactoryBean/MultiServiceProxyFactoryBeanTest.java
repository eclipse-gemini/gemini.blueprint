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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.osgi.framework.ServiceRegistration;
import org.springframework.core.InfrastructureProxy;
import org.eclipse.gemini.blueprint.service.ServiceUnavailableException;
import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.service.importer.ServiceProxyDestroyedException;
import org.eclipse.gemini.blueprint.service.importer.support.Cardinality;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean;

public class MultiServiceProxyFactoryBeanTest extends ServiceBaseTest {

	private OsgiServiceCollectionProxyFactoryBean fb;


	protected void onSetUp() throws Exception {
		fb = new OsgiServiceCollectionProxyFactoryBean();
		fb.setBundleContext(bundleContext);
		fb.setBeanClassLoader(getClass().getClassLoader());
	}

	protected void onTearDown() throws Exception {
		fb.destroy();
		fb = null;
	}

	// causes CGLIB problems
	public void testFactoryBeanForMultipleServicesAsInterfaces() throws Exception {

		fb.setCardinality(Cardinality.C_0__N);
		// look for collections
		fb.setInterfaces(new Class<?>[] { ArrayList.class });
		fb.afterPropertiesSet();

		List registrations = new ArrayList(3);

		try {
			Object result = fb.getObject();
			assertTrue(result instanceof Collection);
			Collection col = (Collection) result;

			assertTrue(col.isEmpty());
			Iterator iter = col.iterator();

			assertFalse(iter.hasNext());

			ArrayList a = new ArrayList();
			a.add(new Long(10));

			registrations.add(publishService(a, ArrayList.class.getName()));

			assertTrue(iter.hasNext());
			Object service = iter.next();

			assertTrue(service instanceof ArrayList);
			assertEquals(10, ((Number) ((Collection) service).toArray()[0]).intValue());

			assertFalse(iter.hasNext());
			a = new ArrayList();
			a.add(new Long(100));
			registrations.add(publishService(a, ArrayList.class.getName()));
			assertTrue(iter.hasNext());
			service = iter.next();
			assertTrue(service instanceof ArrayList);
			assertEquals(100, ((Number) ((Collection) service).toArray()[0]).intValue());
		}
		finally {
			cleanRegistrations(registrations);
		}
	}

	public void testFactoryBeanForMultipleServicesAsClasses() throws Exception {

		fb.setCardinality(Cardinality.C_0__N);
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
			try {
				iter.next();
				fail("should have thrown exception");
			}
			catch (NoSuchElementException ex) {
				// expected
			}
			assertTrue(iter.hasNext());
			Object service = iter.next();
			assertTrue(service instanceof Date);
			assertEquals(time, ((Date) service).getTime());
			assertEquals(dateA, ((InfrastructureProxy) service).getWrappedObject());

			assertFalse(iter.hasNext());
			time = 111;
			Date dateB = new Date(time);
			registrations.add(publishService(dateB));
			assertTrue(iter.hasNext());
			service = iter.next();
			assertTrue(service instanceof Date);
			assertEquals(time, ((Date) service).getTime());
			assertFalse(dateA.equals(((InfrastructureProxy) service).getWrappedObject()));
			assertEquals(dateB, ((InfrastructureProxy) service).getWrappedObject());
		}
		finally {
			cleanRegistrations(registrations);
		}
	}

	public void testIteratorWhenServiceGoesDown() throws Exception {
		fb.setCardinality(Cardinality.C_0__N);
		fb.setInterfaces(new Class<?>[] { Date.class });
		fb.afterPropertiesSet();

		long time = 123;
		Date date = new Date(time);
		Properties props = new Properties();
		props.put("Moroccan", "Sunset");

		List registrations = new ArrayList(3);
		try {
			Collection col = (Collection) fb.getObject();
			Iterator iter = col.iterator();

			assertFalse(iter.hasNext());
			registrations.add(publishService(date, props));
			assertTrue(iter.hasNext());

			// deregister service
			((ServiceRegistration) registrations.remove(0)).unregister();

			// has to successed
			Object obj = iter.next();

			assertTrue(obj instanceof ImportedOsgiServiceProxy);
			assertTrue(obj instanceof Date);
			assertTrue(obj instanceof InfrastructureProxy);
			// the properties will contain the ObjectClass also
			assertEquals(((ImportedOsgiServiceProxy) obj).getServiceReference().getProperty("Moroccan"), "Sunset");
			try {
				// make sure the service is dead
				((Date) obj).getTime();
				fail("should have thrown exception");
			}
			catch (ServiceUnavailableException ex) {
				// proxy is dead
			}
		}
		finally {
			cleanRegistrations(registrations);
		}
	}

	public void testProxyDestruction() throws Exception {
		fb.setCardinality(Cardinality.C_0__N);
		fb.setInterfaces(new Class<?>[] { Date.class });
		fb.afterPropertiesSet();

		long time = 123;
		Date date = new Date(time);
		Properties props = new Properties();
		props.put("Moroccan", "Sunset");

		ServiceRegistration reg = publishService(date, props);
		try {
			Collection col = (Collection) fb.getObject();
			Iterator iter = col.iterator();

			assertTrue(iter.hasNext());
			Date proxy = (Date) iter.next();

			assertEquals(proxy.toString(), date.toString());

			fb.destroy();

			try {
				proxy.getTime();
				fail("should have thrown exception");
			}
			catch (ServiceProxyDestroyedException spde) {
				// expected 
			}
		}
		finally {
			reg.unregister();
		}
	}

	private void cleanRegistrations(Collection list) {
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			((ServiceRegistration) iter.next()).unregister();
		}
	}
}
