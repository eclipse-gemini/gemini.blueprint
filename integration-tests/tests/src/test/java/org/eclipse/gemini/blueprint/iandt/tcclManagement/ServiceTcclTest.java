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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.iandt.tccl.TCCLService;
import org.junit.Test;

/**
 * Test for TCCL handling from the server side. This test checks that the service provider has always priority no matter
 * the client setting.
 * 
 * @author Costin Leau
 * 
 */
public class ServiceTcclTest extends BaseIntegrationTest {

	private static final String CLIENT_RESOURCE =
			"/org/eclipse/gemini/blueprint/iandt/tcclManagement/client-resource.properties";

	private static final String SERVICE_RESOURCE =
			"/org/eclipse/gemini/blueprint/iandt/tccl/internal/internal-resource.file";

	private static final String SERVICE_PUBLIC_RESOURCE = "/org/eclipse/gemini/blueprint/iandt/tccl/service-resource.file";

	private static final String CLIENT_CLASS = "org.eclipse.gemini.blueprint.iandt.tcclManagement.ServiceTcclTest";

	private static final String SERVICE_CLASS =
			"org.eclipse.gemini.blueprint.iandt.tccl.internal.PrivateTCCLServiceImplementation";

	private static final String SERVICE_PUBLIC_CLASS = "org.eclipse.gemini.blueprint.iandt.tccl.TCCLService";

	protected String[] getConfigLocations() {
		return new String[] { "/org/eclipse/gemini/blueprint/iandt/tcclManagement/service-context.xml" };
	}

	protected String[] getTestBundlesNames() {
		return new String[] { "org.eclipse.gemini.blueprint.iandt,tccl.intf," + getSpringDMVersion(),
				"org.eclipse.gemini.blueprint.iandt,tccl," + getSpringDMVersion() };
	}

	@Test
	public void testSanity() throws Exception {
		ServiceReference[] refs =
				bundleContext.getServiceReferences("org.eclipse.gemini.blueprint.iandt.tccl.TCCLService",
						"(tccl=service-provider)");
		System.out.println(bundleContext.getService(refs[0]));
	}

	@Test
	public void testServiceProviderTCCLAndUnmanagedClient() throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		TCCLService tccl = getUnmanagedTCCL();
		assertNotSame("service provide CL hasn't been set", loader, tccl.getTCCL());
	}

	@Test
	public void testServiceProviderTCCLWithUnmanagedClientWithNullClassLoader() throws Exception {
		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(null);
			ClassLoader cl = getUnmanagedTCCL().getTCCL();
			assertNotNull("service provide CL hasn't been set", cl);
		} finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	@Test
	public void testServiceProviderTCCLAndUnmanagedClientWithPredefinedClassLoader() throws Exception {
		URLClassLoader dummyCL = new URLClassLoader(new URL[0]);

		ClassLoader previous = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(dummyCL);
			ClassLoader cl = getUnmanagedTCCL().getTCCL();
			assertNotSame(dummyCL, cl);
		} finally {
			Thread.currentThread().setContextClassLoader(previous);
		}
	}

	@Test
	public void testServiceProviderTCCLWithClientTCCLOnClasses() throws Exception {
		failToLoadClass(getClientTCCL().getTCCL(), CLIENT_CLASS);
	}

	@Test
	public void testServiceProviderTCCLWithClientTCCLOnResources() throws Exception {
		assertNull(getClientTCCL().getTCCL().getResource(CLIENT_RESOURCE));
	}

	@Test
	public void testServiceProviderTCCLWithClientTCCLWithServiceClasses() throws Exception {
		ClassLoader cl = getClientTCCL().getTCCL();
		cl.loadClass(SERVICE_PUBLIC_CLASS);
		cl.loadClass(SERVICE_CLASS);
	}

	@Test
	public void testServiceProviderTCCLWithClientTCCLWithServiceResource() throws Exception {
		assertNotNull(getClientTCCL().getTCCL().getResource(SERVICE_PUBLIC_CLASS.replace(".", "/").concat(".class")));
		assertNotNull(getClientTCCL().getTCCL().getResource(SERVICE_RESOURCE));
	}

	@Test
	public void testServiceProvidedTCCLOnClasses() throws Exception {
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

	// provide permission for loading class using the service bundle
	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		perms.add(new AdminPermission("(name=org.eclipse.gemini.blueprint.iandt.tccl)", AdminPermission.CLASS));
		perms.add(new AdminPermission("(name=org.eclipse.gemini.blueprint.iandt.tccl)", AdminPermission.RESOURCE));
		return perms;
	}
}