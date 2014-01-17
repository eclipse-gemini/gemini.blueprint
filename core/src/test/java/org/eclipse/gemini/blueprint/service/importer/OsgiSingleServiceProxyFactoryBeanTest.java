/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.service.importer;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;
import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.service.importer.support.ImportContextClassLoaderEnum;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.aop.SpringProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * @author Adrian Colyer
 * @author Hal Hildebrand
 * @author Costin Leau
 * 
 */
public class OsgiSingleServiceProxyFactoryBeanTest extends TestCase {

	private OsgiServiceProxyFactoryBean serviceFactoryBean;

	private BundleContext bundleContext;

	protected void setUp() throws Exception {
		super.setUp();
		this.serviceFactoryBean = new OsgiServiceProxyFactoryBean();
		this.serviceFactoryBean.setBeanClassLoader(getClass().getClassLoader());
		this.bundleContext = createMock(BundleContext.class);
	}

	public void testAfterPropertiesSetNoBundle() throws Exception {
		try {
			this.serviceFactoryBean.afterPropertiesSet();
			fail("should have throw IllegalArgumentException since bundle context was not set");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testAfterPropertiesSetNoClassLoader() throws Exception {
		this.serviceFactoryBean.setBundleContext(this.bundleContext);
		try {
			this.serviceFactoryBean.afterPropertiesSet();
			fail("should have throw IllegalArgumentException since classLoader was not set");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testAfterPropertiesSetNoServiceType() throws Exception {
		this.serviceFactoryBean.setBundleContext(this.bundleContext);
		try {
			this.serviceFactoryBean.afterPropertiesSet();
			fail("should have throw IllegalArgumentException since service type was not set");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testAfterPropertiesSetBadFilter() throws Exception {
		this.serviceFactoryBean.setBundleContext(this.bundleContext);
		this.serviceFactoryBean.setInterfaces(new Class<?>[] { ApplicationContext.class });
		this.serviceFactoryBean.setFilter("this is not a valid filter expression");
		try {
			this.serviceFactoryBean.afterPropertiesSet();
			fail("should have throw IllegalArgumentException since filter has invalid syntax");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testGetObjectTypeCompositeInterface() {
		this.serviceFactoryBean.setInterfaces(new Class<?>[] { ApplicationContext.class });
		this.serviceFactoryBean.setBundleContext(this.bundleContext);
		this.serviceFactoryBean.afterPropertiesSet();
		assertTrue("composite interface not properly created", ApplicationContext.class
				.isAssignableFrom(this.serviceFactoryBean.getObjectType()));
		assertTrue("mixing interface not introduced", ImportedOsgiServiceProxy.class
				.isAssignableFrom(this.serviceFactoryBean.getObjectType()));
	}

	public void testObjectTypeWOCompositeInterface() {
		this.serviceFactoryBean.setInterfaces(new Class<?>[] { AbstractApplicationContext.class });
		this.serviceFactoryBean.setBundleContext(this.bundleContext);
		this.serviceFactoryBean.afterPropertiesSet();

		try {
			this.serviceFactoryBean.getObjectType();
			fail("should not be able to create composite interface when a class is specified since CGLIB is not in the classpath");
		} catch (Exception ex) {

		}
	}

	// OsgiServiceUtils are tested independently in error cases, here we
	// test the
	// correct behaviour of the ProxyFactoryBean when OsgiServiceUtils
	// succesfully
	// finds the service.
	public void testGetObjectWithFilterOnly() throws Exception {
		this.serviceFactoryBean.setBundleContext(new MockBundleContext());
		this.serviceFactoryBean.setInterfaces(new Class<?>[] { Serializable.class });
		String filter = "(beanName=myBean)";
		this.serviceFactoryBean.setFilter(filter);

		MockServiceReference ref = new MockServiceReference();
		Dictionary dict = new Hashtable();
		dict.put(Constants.OBJECTCLASS, new String[] { Serializable.class.getName() });
		ref.setProperties(dict);

		serviceFactoryBean.setBeanClassLoader(getClass().getClassLoader());
		serviceFactoryBean.afterPropertiesSet();

		Object proxy = serviceFactoryBean.getObject();
		assertTrue(proxy instanceof Serializable);
		assertTrue("should be proxied", proxy instanceof SpringProxy);
	}

	public void testClassLoadingOptionsConstant() throws Exception {
		serviceFactoryBean.setImportContextClassLoader(ImportContextClassLoaderEnum.CLIENT);
		serviceFactoryBean.setImportContextClassLoader(ImportContextClassLoaderEnum.SERVICE_PROVIDER);
		serviceFactoryBean.setImportContextClassLoader(ImportContextClassLoaderEnum.UNMANAGED);
	}
	
	public void testNoInterfaceSpecified() throws Exception {
		serviceFactoryBean.setBundleContext(new MockBundleContext());
		serviceFactoryBean.setInterfaces(null);
		serviceFactoryBean.setFilter(null);
		serviceFactoryBean.setServiceBeanName("foo");
		serviceFactoryBean.afterPropertiesSet();
		serviceFactoryBean.getObject();
	}
}