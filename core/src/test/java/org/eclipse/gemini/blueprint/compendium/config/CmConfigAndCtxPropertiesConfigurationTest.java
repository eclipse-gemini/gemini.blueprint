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

package org.eclipse.gemini.blueprint.compendium.config;

import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * @author Costin Leau
 * 
 */
public class CmConfigAndCtxPropertiesConfigurationTest extends TestCase {

	private GenericApplicationContext appContext;

	private BundleContext bundleContext;

	private MockControl adminControl;

	private ConfigurationAdmin admin;

	private Dictionary config;


	protected void setUp() throws Exception {

		adminControl = MockControl.createControl(ConfigurationAdmin.class);
		admin = (ConfigurationAdmin) adminControl.getMock();
		MockControl configMock = MockControl.createControl(Configuration.class);
		Configuration cfg = (Configuration) configMock.getMock();

		config = new Hashtable();

		adminControl.expectAndReturn(admin.getConfiguration("com.xyz.myapp"), cfg, MockControl.ONE_OR_MORE);
		configMock.expectAndReturn(cfg.getProperties(), config, MockControl.ONE_OR_MORE);

		adminControl.replay();
		configMock.replay();

		bundleContext = new MockBundleContext() {

			// add Configuration admin support
			public Object getService(ServiceReference reference) {
				return admin;
			}
		};

		appContext = new GenericApplicationContext();
		appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));

		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		// reader.setEventListener(this.listener);
		reader.loadBeanDefinitions(new ClassPathResource("osgiPropertyPlaceholder.xml", getClass()));
		appContext.refresh();
	}

	protected void tearDown() throws Exception {
		adminControl.verify();
	}

	public void testValidateConfiguration() throws Exception {

	}
}