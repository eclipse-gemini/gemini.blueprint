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

package org.eclipse.gemini.blueprint.iandt.bundleScope;

import java.security.AllPermission;
import java.util.List;
import java.util.Properties;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ConfigurableApplicationContext;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.iandt.scope.common.ScopeTestService;
import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.springframework.util.ObjectUtils;

/**
 * Integration tests for 'bundle' scoped beans.
 * 
 * @author Costin Leau
 * 
 */
public class ScopingTest extends BaseIntegrationTest {

	protected String[] getTestBundlesNames() {
		return new String[] { "net.sourceforge.cglib, com.springsource.net.sf.cglib, 2.1.3",
			"org.eclipse.gemini.blueprint.iandt, scoped.bundle.common," + getSpringDMVersion(),
			"org.eclipse.gemini.blueprint.iandt, scoped.bundle.a," + getSpringDMVersion(),
			"org.eclipse.gemini.blueprint.iandt, scoped.bundle.b," + getSpringDMVersion() };
	}

	protected String getManifestLocation() {
		return "org/eclipse/gemini/blueprint/iandt/bundleScope/ScopingTest.MF";
	}

	protected String[] getConfigLocations() {
		return new String[] { "/org/eclipse/gemini/blueprint/iandt/bundleScope/scope-context.xml" };
	}

	public void testEnvironmentValidity() throws Exception {
		assertNotNull(getServiceA());
		assertNotNull(getServiceB());
	}

	public void testServiceAScopeForCurrentBundle() throws Exception {
		ScopeTestService serviceAcopy1 = getServiceA();
		ScopeTestService serviceAcopy2 = getServiceA();

		assertEquals("different bean instances given for the same bundle", serviceAcopy1, serviceAcopy2);
	}

	public void testServiceAScopeForBundleA() throws Exception {
		ScopeTestService serviceAInBundleA = (ScopeTestService) org.eclipse.gemini.blueprint.iandt.scope.a.BeanReference.BEAN;

		System.out.println(serviceAInBundleA.getServiceIdentity());
		System.out.println(getServiceA().getServiceIdentity());

		assertFalse("same bean instance used for different bundles", serviceAInBundleA.getServiceIdentity().equals(
			getServiceA().getServiceIdentity()));
	}

	public void testServiceAScopeForBundleB() throws Exception {
		String symName = "org.eclipse.gemini.blueprint.iandt.scope.b";
		ScopeTestService serviceAInBundleB = (ScopeTestService) getAppCtx(symName).getBean("serviceFromA");

		assertFalse("same bean instance used for different bundles", serviceAInBundleB.getServiceIdentity().equals(
			getServiceA().getServiceIdentity()));
	}

	public void testServiceBInBundleBAndTestBundle() throws Exception {
		ScopeTestService serviceAInBundleB = (ScopeTestService) org.eclipse.gemini.blueprint.iandt.scope.b.BeanReference.BEAN;

		assertFalse("same bean instance used for different bundles", serviceAInBundleB.getServiceIdentity().equals(
			getServiceB().getServiceIdentity()));
	}

	public void testScopedBeanNotExported() throws Exception {
		Properties props = (Properties) applicationContext.getBean("props");
		// ask for it again
		Properties another = (Properties) applicationContext.getBean("props");
		assertSame("different instances returned for the same scope", props, another);
	}

	public void testBeanReferenceAndLocalScopeInstanceForBundleA() throws Exception {
		String symName = "org.eclipse.gemini.blueprint.iandt.scope.a";
		assertSame("local references are different", getAppCtx(symName).getBean("a.service"),
			org.eclipse.gemini.blueprint.iandt.scope.a.BeanReference.BEAN);
	}

	public void testBeanReferenceAndLocalScopeInstanceForBundleB() throws Exception {
		String symName = "org.eclipse.gemini.blueprint.iandt.scope.b";
		assertSame("local references are different", getAppCtx(symName).getBean("b.service"),
			org.eclipse.gemini.blueprint.iandt.scope.b.BeanReference.BEAN);
	}

	public void testScopedBeanDestructionCallbackDuringContextRefresh() throws Exception {
		Properties props = (Properties) applicationContext.getBean("props");
		// add some content
		props.put("foo", "bar");

		// check by asking again for the bean
		Properties another = (Properties) applicationContext.getBean("props");
		assertSame(props, another);
		assertTrue(another.containsKey("foo"));

		// refresh context
		applicationContext.refresh();
		Properties refreshed = (Properties) applicationContext.getBean("props");
		assertNotSame("context refresh does not clean scoped objects", props, refreshed);
		assertTrue(refreshed.isEmpty());
		// check that props object has been cleaned also
		assertTrue("destroy callback wasn't called/applied", props.isEmpty());
	}

	public void testExportedScopedBeansDestructionCallbackCalled() throws Exception {
		Object rawServiceA = getServiceA();
		assertTrue(rawServiceA instanceof Properties);
		Properties props = (Properties) rawServiceA;
		// modify properties
		props.put("service", "a");

		// check service again
		assertTrue(((Properties) getServiceA()).containsKey("service"));

		// refresh opposite service
		getAppCtx("org.eclipse.gemini.blueprint.iandt.scope.a").refresh();

		// wait for the context to refresh
		Thread.sleep(1000);
		// get service a again
		assertTrue("scoped bean a did not have its callback called", ((Properties) getServiceA()).isEmpty());
	}

	protected ScopeTestService getServiceA() throws Exception {
		return getService("a");
	}

	protected ScopeTestService getServiceB() throws Exception {
		return getService("b");
	}

	protected ScopeTestService getService(String bundleName) {
		ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(bundleContext,
			ScopeTestService.class.getName(), "(Bundle-SymbolicName=org.eclipse.gemini.blueprint.iandt.scope." + bundleName
					+ ")");
		if (ref == null) {
			String filter = OsgiFilterUtils.unifyFilter(ScopeTestService.class, null);
			System.out.println(ObjectUtils.nullSafeToString(OsgiServiceReferenceUtils.getServiceReferences(
				bundleContext, filter)));
			throw new IllegalStateException("cannot find service with owning bundle " + bundleName);
		}
		return (ScopeTestService) bundleContext.getService(ref);
	}

	private ConfigurableApplicationContext getAppCtx(String symBundle) {
		ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(bundleContext, "("
				+ ConfigurableOsgiBundleApplicationContext.APPLICATION_CONTEXT_SERVICE_PROPERTY_NAME + "=" + symBundle
				+ ")");

		if (ref == null)
			throw new IllegalArgumentException("cannot find appCtx for bundle " + symBundle);
		return (ConfigurableApplicationContext) bundleContext.getService(ref);
	}

	/**
	 * Since the test is creating some application contexts, give it some
	 * privileges.
	 */
	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		perms.add(new AdminPermission("(name=org.eclipse.gemini.blueprint.iandt.scope.a)", "*"));
		perms.add(new AllPermission());
		return perms;
	}
}