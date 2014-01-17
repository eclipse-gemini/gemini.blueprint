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

import static org.easymock.EasyMock.*;

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
 */
public class CmConfigAndCtxPropertiesConfigurationTest extends TestCase {

    private ConfigurationAdmin admin;


    protected void setUp() throws Exception {
        admin = createMock(ConfigurationAdmin.class);
        Configuration cfg = createMock(Configuration.class);

        Dictionary config = new Hashtable();

        expect(admin.getConfiguration("com.xyz.myapp")).andReturn(cfg).atLeastOnce();
        expect(cfg.getProperties()).andReturn(config).atLeastOnce();

        replay(admin, cfg);

        BundleContext bundleContext = new MockBundleContext() {

            // add Configuration admin support
            public Object getService(ServiceReference reference) {
                return admin;
            }
        };

        GenericApplicationContext appContext = new GenericApplicationContext();
        appContext.getBeanFactory().addBeanPostProcessor(new BundleContextAwareProcessor(bundleContext));

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
        // reader.setEventListener(this.listener);
        reader.loadBeanDefinitions(new ClassPathResource("osgiPropertyPlaceholder.xml", getClass()));
        appContext.refresh();
    }

    protected void tearDown() throws Exception {
        verify(admin);
    }

    public void testValidateConfiguration() throws Exception {

    }
}