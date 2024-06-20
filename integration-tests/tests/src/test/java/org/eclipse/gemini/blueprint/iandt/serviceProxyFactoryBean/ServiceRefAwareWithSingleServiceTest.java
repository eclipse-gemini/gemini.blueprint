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
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;
import org.springframework.aop.SpringProxy;
import org.springframework.core.InfrastructureProxy;

import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Costin Leau
 * 
 */
public class ServiceRefAwareWithSingleServiceTest extends ServiceBaseTest {

	private OsgiServiceProxyFactoryBean fb;


	protected void onSetUp() throws Exception {
		fb = new OsgiServiceProxyFactoryBean();
		fb.setBundleContext(bundleContext);
		// execute retries fast
		fb.setTimeout(1);
		fb.setBeanClassLoader(getClass().getClassLoader());
	}

	protected void onTearDown() throws Exception {
		fb = null;
	}

	@Test
	public void testProxyForUnaryCardinality() throws Exception {
		long time = 1234;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);

		fb.setAvailability(Availability.MANDATORY);

		fb.setInterfaces(new Class<?>[] { Date.class });
		fb.afterPropertiesSet();

		ImportedOsgiServiceProxy refAware = null;
		try {
			Object result = fb.getObject();
			assertTrue(result instanceof Date);
			// check it's our object
			assertEquals(time, ((Date) result).getTime());
			assertTrue(result instanceof SpringProxy);
			assertTrue(result instanceof ImportedOsgiServiceProxy);
			assertTrue(result instanceof InfrastructureProxy);

			refAware = (ImportedOsgiServiceProxy) result;
			assertNotNull(refAware.getServiceReference());

			assertEquals("wrong target returned", date, ((InfrastructureProxy) result).getWrappedObject());
		}
		finally {
			if (reg != null)
				reg.unregister();
		}

		// test reference after the service went down
		assertNotNull(refAware.getServiceReference());
		assertNull(refAware.getServiceReference().getBundle());
	}
	
	@Test
	public void testServiceReferenceProperties() throws Exception {
		long time = 1234;
		Date date = new Date(time);
		Dictionary dict = new Properties();
		dict.put("foo", "bar");
		dict.put("george", "michael");

		ServiceRegistration reg = publishService(date, dict);

		fb.setAvailability(Availability.MANDATORY);
		fb.setFilter("(&(foo=bar)(george=michael))");
		fb.setInterfaces(new Class<?>[] { Date.class });
		fb.afterPropertiesSet();

		try {
			Object result = fb.getObject();
			assertTrue(result instanceof Date);
			// check it's our object
			assertEquals(time, ((Date) result).getTime());

			ImportedOsgiServiceProxy refAware = (ImportedOsgiServiceProxy) result;

			assertTrue(doesMapContainsDictionary(dict,
				OsgiServiceReferenceUtils.getServicePropertiesAsMap(refAware.getServiceReference())));

			InfrastructureProxy targetAware = (InfrastructureProxy) result;
			assertEquals(date, targetAware.getWrappedObject());
		}
		finally {
			if (reg != null)
				reg.unregister();
		}
	}

	/**
	 * Check if the 'test' map contains the original Dictionary.
	 * 
	 * @param original
	 * @param test
	 * @return
	 */
	private boolean doesMapContainsDictionary(Dictionary original, Map test) {
		Enumeration enm = original.keys();
		while (enm.hasMoreElements()) {
			if (!test.containsKey(enm.nextElement()))
				return false;
		}

		return true;
	}

}
