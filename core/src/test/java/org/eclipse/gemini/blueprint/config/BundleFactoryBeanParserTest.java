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

package org.eclipse.gemini.blueprint.config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;
import org.eclipse.gemini.blueprint.TestUtils;
import org.eclipse.gemini.blueprint.bundle.BundleActionEnum;
import org.eclipse.gemini.blueprint.bundle.BundleFactoryBean;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * @author Costin Leau
 * 
 */
public class BundleFactoryBeanParserTest extends TestCase {

	private GenericApplicationContext appContext;

	private Bundle startBundle, installBundle, updateBundle, bundleA;

	private static List INSTALL_BUNDLE_ACTION;

	private Bundle[] bundleToInstall = new Bundle[1];

	private static final String STREAM_TAG = "| stream |";

	protected void setUp() throws Exception {
		INSTALL_BUNDLE_ACTION = new ArrayList();

		installBundle = createMock("installBundle", Bundle.class);
		expect(installBundle.getSymbolicName()).andReturn("installBundle").anyTimes();

		updateBundle = createMock("updateBundle", Bundle.class);
		expect(updateBundle.getSymbolicName()).andReturn("updateBundle").atLeastOnce();

		startBundle = createMock("startBundle", Bundle.class);
        expect(startBundle.getSymbolicName()).andReturn("startBundle").atLeastOnce();


		bundleA = new MockBundle("bundleA");

		final Bundle[] bundles = new Bundle[] { installBundle, startBundle, updateBundle, bundleA };

		BundleContext bundleContext = new MockBundleContext() {
			// return proper bundles
			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[0];
			}

			public Bundle[] getBundles() {
				return bundles;
			}

			public Bundle installBundle(String location, InputStream input) throws BundleException {
				INSTALL_BUNDLE_ACTION.add(location + STREAM_TAG + input);
				return bundleToInstall[0];
			}

			public Bundle installBundle(String location) throws BundleException {
				INSTALL_BUNDLE_ACTION.add(location);
				return bundleToInstall[0];
			}

		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));
		appContext.setClassLoader(getClass().getClassLoader());

	}

	protected void tearDown() throws Exception {
		appContext.close();
	}

	private void refresh() {
		replay(installBundle, startBundle, updateBundle);

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		reader.loadBeanDefinitions(new ClassPathResource("bundleBeanFactoryTest.xml", getClass()));

		appContext.refresh();
	}

	public void testWithSymName() throws Exception {
		refresh();
		BundleFactoryBean fb = appContext.getBean("&wSymName", BundleFactoryBean.class);
		assertSame(bundleA, fb.getObject());
		assertNull(fb.getLocation());
		assertNull(fb.getResource());

	}

	public void testLocationAndResource() throws Exception {
		refresh();
		BundleFactoryBean fb = appContext.getBean("&wLocation", BundleFactoryBean.class);
		assertEquals("fromServer", fb.getLocation());
		assertNull(fb.getSymbolicName());
		assertNotNull(fb.getResource());
	}

	public void testStartBundle() throws Exception {
		bundleToInstall[0] = startBundle;
		startBundle.start();

		refresh();

		BundleFactoryBean fb = appContext.getBean("&start", BundleFactoryBean.class);

		BundleActionEnum action = getAction(fb);
		assertSame(BundleActionEnum.START, action);
		assertNull(getDestroyAction(fb));

		assertSame(startBundle, appContext.getBean("start"));
		verify(startBundle);
	}

	public void testStopBundle() throws Exception {
		bundleToInstall[0] = startBundle;

		// invoked on shutdown
		startBundle.stop();

		refresh();

		BundleFactoryBean fb = appContext.getBean("&stop", BundleFactoryBean.class);
		assertSame(BundleActionEnum.STOP, getDestroyAction(fb));

		assertSame(startBundle, appContext.getBean("stop"));

		appContext.close();
		verify(startBundle);
	}

	public void testUpdateBundle() throws Exception {
		bundleToInstall[0] = updateBundle;

		updateBundle.update();
		updateBundle.stop();
		refresh();

		BundleFactoryBean fb = appContext.getBean("&update", BundleFactoryBean.class);

		BundleActionEnum action = getAction(fb);
		assertSame(BundleActionEnum.UPDATE, action);
		assertSame(BundleActionEnum.STOP, getDestroyAction(fb));

		assertSame(updateBundle, appContext.getBean("update"));
		appContext.close();
		verify(updateBundle);
	}

	public void testInstall() throws Exception {
		bundleToInstall[0] = installBundle;

		installBundle.start();
		installBundle.uninstall();

		refresh();

		BundleFactoryBean fb = appContext.getBean("&install", BundleFactoryBean.class);
		assertEquals("fromClient", fb.getLocation());
		assertEquals(1, INSTALL_BUNDLE_ACTION.size());
		assertEquals("fromClient", INSTALL_BUNDLE_ACTION.get(0));

		assertSame(installBundle, appContext.getBean("install"));
		appContext.close();
		verify(installBundle);
	}

	public void testInstallImpliedByUpdateUsingRealLocation() throws Exception {
		bundleToInstall[0] = installBundle;

		installBundle.update();
		installBundle.uninstall();

		refresh();

		BundleFactoryBean fb =
                appContext.getBean("&updateFromActualLocation", BundleFactoryBean.class);
		assertEquals(1, INSTALL_BUNDLE_ACTION.size());

		assertSame(BundleActionEnum.UPDATE, getAction(fb));
		assertSame(BundleActionEnum.UNINSTALL, getDestroyAction(fb));

		assertTrue(((String) INSTALL_BUNDLE_ACTION.get(0)).indexOf(STREAM_TAG) >= -1);

		assertSame(installBundle, appContext.getBean("updateFromActualLocation"));
		appContext.close();
		verify(installBundle);
	}

	public void testNestedBundleDeclaration() throws Exception {
		Bundle bnd = createMock(Bundle.class);

		bnd.start();

        replay(bnd);

		appContext.getBeanFactory().registerSingleton("createdByTheTest", bnd);
		refresh();

		appContext.getBean("nested");
		BundleFactoryBean fb = appContext.getBean("&nested", BundleFactoryBean.class);

		verify(bnd);
	}

	private BundleActionEnum getAction(BundleFactoryBean fb) {
		return (BundleActionEnum) TestUtils.getFieldValue(fb, "action");
	}

	private BundleActionEnum getDestroyAction(BundleFactoryBean fb) {
		return (BundleActionEnum) TestUtils.getFieldValue(fb, "destroyAction");
	}
}
