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

import java.io.Serializable;
import java.util.Date;

import org.osgi.framework.ServiceRegistration;
import org.eclipse.gemini.blueprint.service.importer.support.Cardinality;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;

/**
 * @author Costin Leau
 * 
 */
public class ServiceProxyFactoryBeanTest extends ServiceBaseTest {

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

	public void testFactoryBeanForOneServiceAsClass() throws Exception {
		long time = 1234;
		Date date = new Date(time);
		ServiceRegistration reg = publishService(date);

		fb.setCardinality(Cardinality.C_1__1);
		fb.setInterfaces(new Class<?>[] { Date.class });
		fb.afterPropertiesSet();

		try {
			Object result = fb.getObject();
			assertTrue(result instanceof Date);
			assertEquals(time, ((Date) result).getTime());
		}
		finally {
			if (reg != null)
				reg.unregister();
		}
	}

	public void testFactoryBeanForOneServiceAsInterface() throws Exception {
		long time = 1234;
		Date date = new Date(time);

		Class<?>[] intfs = new Class<?>[] { Comparable.class, Serializable.class, Cloneable.class };

		String[] classes = new String[] { Comparable.class.getName(), Serializable.class.getName(),
			Cloneable.class.getName(), Date.class.getName() };

		ServiceRegistration reg = publishService(date, classes);

		fb.setCardinality(Cardinality.C_1__1);
		fb.setInterfaces(intfs);
		fb.setFilter(OsgiFilterUtils.unifyFilter(Date.class, null));
		fb.afterPropertiesSet();

		try {
			Object result = fb.getObject();
			// the interfaces are implemented
			assertTrue(result instanceof Comparable);
			assertTrue(result instanceof Serializable);
			assertTrue(result instanceof Cloneable);
			// but not the class
			assertFalse(result instanceof Date);
			// compare the strings
			assertEquals(result.toString(), date.toString());
		}
		finally {
			if (reg != null)
				reg.unregister();
		}
	}

}
