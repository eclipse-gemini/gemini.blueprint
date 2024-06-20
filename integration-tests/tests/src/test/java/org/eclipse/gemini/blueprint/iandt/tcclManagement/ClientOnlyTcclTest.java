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

package org.eclipse.gemini.blueprint.iandt.tcclManagement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.eclipse.gemini.blueprint.iandt.tccl.TCCLService;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.junit.Test;

/**
 * Test for TCCL handling only on the client side. That is the service doesn't provide any handling.
 * 
 * @author Costin Leau
 * 
 */
public class ClientOnlyTcclTest extends BaseIntegrationTest {

	private static final String CLIENT_RESOURCE =
			"/org/eclipse/gemini/blueprint/iandt/tcclManagement/client-resource.properties";

	private static final String SERVICE_RESOURCE =
			"/org/eclipse/gemini/blueprint/iandt/tccl/internal/internal-resource.file";

	private static final String SERVICE_PUBLIC_RESOURCE = "/org/eclipse/gemini/blueprint/iandt/tccl/service-resource.file";

	private static final String CLIENT_CLASS = "org.eclipse.gemini.blueprint.iandt.tcclManagement.ClientOnlyTcclTest";

	private static final String SERVICE_CLASS =
			"org.eclipse.gemini.blueprint.iandt.tccl.internal.PrivateTCCLServiceImplementation";

	private static final String SERVICE_PUBLIC_CLASS = "org.eclipse.gemini.blueprint.iandt.tccl.TCCLService";

	protected String[] getConfigLocations() {
		return new String[] { "/org/eclipse/gemini/blueprint/iandt/tcclManagement/client-context.xml" };
	}

	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt,tccl.intf," + getSpringDMVersion(),
				"org.eclipse.gemini.blueprint.iandt,tccl," + getSpringDMVersion() };
	}

	@Test
	public void testTCCLUnmanaged() throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		TCCLService tccl = getUnmanagedTCCL();
		assertSame(loader, tccl.getTCCL());
	}

	@Test
	public void testTCCLUnmanagedWithNullClassLoader() throws Exception {
		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(null);
			ClassLoader cl = getUnmanagedTCCL().getTCCL();
			assertNull(cl);
		} finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	@Test
	public void testTCCLUnmanagedWithPredefinedClassLoader() throws Exception {
		URLClassLoader dummyCL = new URLClassLoader(new URL[0]);

		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(dummyCL);
			ClassLoader cl = getUnmanagedTCCL().getTCCL();
			assertSame(dummyCL, cl);
		} finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	@Test
	public void testClientTCCLOnClientClasses() throws Exception {
		ClassLoader clientCL = getClientTCCL().getTCCL();
		assertNotNull(clientCL);
		assertNotNull(clientCL.loadClass(CLIENT_CLASS));
	}

	@Test
	public void testClientTCCLOnClientResources() throws Exception {
		ClassLoader clientCL = getClientTCCL().getTCCL();
		assertNotNull(clientCL);
		assertNotNull(clientCL.getResource(CLIENT_RESOURCE));
	}

	@Test
	public void testClientTCCLWithServiceClasses() throws Exception {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		ClassLoader cl = getClientTCCL().getTCCL();
		System.out.println("current :" + current);
		System.out.println("cl : " + cl);
		cl.loadClass(SERVICE_PUBLIC_CLASS);
		failToLoadClass(cl, SERVICE_CLASS);
	}

	@Test
	public void testClientTCCLWithServiceResource() throws Exception {
		assertNull(getClientTCCL().getTCCL().getResource(SERVICE_RESOURCE));
	}

	@Test
	public void testServiceProvidedTCCLOnClasses() throws Exception {
		refreshTCCLBundle();
		ClassLoader cl = getServiceProviderTCCL().getTCCL();
		cl.loadClass(SERVICE_PUBLIC_CLASS);
		cl.loadClass(SERVICE_CLASS);
	}

	@Test
	public void testServiceProvidedTCCLOnResources() throws Exception {
		assertNotNull(getServiceProviderTCCL().getTCCL().getResource(SERVICE_RESOURCE));
	}

	@Test
	public void testServiceProviderTCCLOnClientClasses() throws Exception {
		failToLoadClass(getServiceProviderTCCL().getTCCL(), CLIENT_CLASS);
	}

	@Test
	public void testServiceProviderTCCLOnClientResources() throws Exception {
		assertNull(getServiceProviderTCCL().getTCCL().getResource(CLIENT_RESOURCE));
	}

	private void failToLoadClass(ClassLoader cl, String className) {
		try {
			cl.loadClass(className);
			fail("shouldn't be able to load class " + className);
		} catch (ClassNotFoundException cnfe) {
			// expected
		}
	}

	private TCCLService getUnmanagedTCCL() {
		return (TCCLService) applicationContext.getBean("unmanaged");
	}

	private TCCLService getServiceProviderTCCL() {
		return (TCCLService) applicationContext.getBean("service-provider");
	}

	private TCCLService getClientTCCL() {
		return (TCCLService) applicationContext.getBean("client");
	}

	private void refreshTCCLBundle() {
		Bundle bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, "org.eclipse.gemini.blueprint.iandt.tccl");
		try {
			bundle.update();
		} catch (BundleException be) {
		}
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		perms.add(new AdminPermission("(name=org.eclipse.gemini.blueprint.iandt.tccl)", AdminPermission.RESOURCE));
		perms.add(new AdminPermission("(name=org.eclipse.gemini.blueprint.iandt.tccl)", AdminPermission.CLASS));
		return perms;
	}
}
