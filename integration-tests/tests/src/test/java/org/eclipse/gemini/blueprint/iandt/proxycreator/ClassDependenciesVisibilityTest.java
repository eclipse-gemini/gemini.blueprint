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

package org.eclipse.gemini.blueprint.iandt.proxycreator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Permission;
import java.util.List;

import javax.swing.event.DocumentEvent;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.eclipse.gemini.blueprint.service.importer.support.ImportContextClassLoaderEnum;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.junit.Test;

/**
 *
 * FIXME:
 * TODO: this is not an applicable test anymore.  As equinox now exports javax.swing.* as part of org.osgi.framework.system.packages
 *
 * Integration test for bug OSGI-597.
 * 
 * This test tries to create a proxy for DocumentEvent w/o importing its
 * dependency, namely javax.swing.text.Element.
 * 
 * @author Costin Leau
 */
public class ClassDependenciesVisibilityTest extends BaseIntegrationTest {

	private static String DEPENDENCY_CLASS = "javax.swing.text.Element";

	@Test
	public void testPackageDependency() throws Exception {
		ClassLoader cl = applicationContext.getClassLoader();
		System.out.println(cl);
		OsgiServiceProxyFactoryBean fb = new OsgiServiceProxyFactoryBean();
		fb.setBundleContext(bundleContext);
        fb.setAvailability(Availability.OPTIONAL);
		fb.setImportContextClassLoader(ImportContextClassLoaderEnum.UNMANAGED);
		fb.setInterfaces(new Class<?>[] { DocumentEvent.class });
		fb.setBeanClassLoader(cl);
		fb.setApplicationEventPublisher(applicationContext);
		fb.afterPropertiesSet();

		checkPackageVisibility(cl);

		Object proxy = fb.getObject();
		assertNotNull(proxy);
		assertTrue(proxy instanceof DocumentEvent);
		System.out.println(proxy.getClass());

	}

	@Test
	public void testJdkProxy() throws Exception {
		InvocationHandler ih = new InvocationHandler() {

			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return null;
			}
		};
		ClassLoader cl = applicationContext.getClassLoader();
		checkPackageVisibility(cl);

		try {
			Object proxy = Proxy.newProxyInstance(cl, new Class<?>[] { DocumentEvent.class }, ih);
			assertNotNull(proxy);
			System.out.println(proxy.getClass());

			fail("should have failed");
		}
		catch (Throwable cnfe) {
			// expected
		}
	}

	private void checkPackageVisibility(ClassLoader cl) throws Exception {

		try {
			cl.loadClass(DEPENDENCY_CLASS);
			fail("should not be able to load " + DEPENDENCY_CLASS);
		}catch (ClassNotFoundException cnfe) {
			// expected
		}
	}

	// remove the javax.* boot delegation
	protected List getBootDelegationPackages() {
		List packages = super.getBootDelegationPackages();
		packages.remove("javax.*");
		packages.remove("javax.swing.*");

		return packages;
	}

	protected List<Permission> getTestPermissions() {
		List<Permission> perms = super.getTestPermissions();
		// export package
		perms.add(new RuntimePermission("*", "getClassLoader"));
		return perms;
	}

    @Override
    public boolean isDisabledInThisEnvironment(String testMethodName) {
        // TODO: disabling this set of tests for now.
        return true;
    }
}
