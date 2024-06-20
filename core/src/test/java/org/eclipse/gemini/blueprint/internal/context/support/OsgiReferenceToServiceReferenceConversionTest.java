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

package org.eclipse.gemini.blueprint.internal.context.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Polygon;
import java.awt.Shape;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.osgi.framework.ServiceReference;
import org.springframework.core.io.ClassPathResource;
import org.eclipse.gemini.blueprint.mock.ArrayEnumerator;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * 
 * @author Costin Leau
 * 
 */
public class OsgiReferenceToServiceReferenceConversionTest {

	private MockBundleContext context;

	private OsgiBundleXmlApplicationContext appCtx;

	private Shape service;

	public static class RefContainer {
		private static ServiceReference reference;

		public void setServiceReference(ServiceReference ref) {
			RefContainer.reference = ref;
		}
	}

	@Before
	public void setup() throws Exception {
		service = new Polygon();
		RefContainer.reference = null;

		MockBundle bundle = new MockBundle() {
			public Enumeration findEntries(String path, String filePattern, boolean recurse) {
				try {
					return new ArrayEnumerator(
							new URL[] { new ClassPathResource(
									"/org/eclipse/gemini/blueprint/internal/context/support/serviceReferenceConversion.xml").getURL() });
				}
				catch (IOException io) {
					throw new RuntimeException(io);
				}
			}
		};

		context = new MockBundleContext(bundle) {
			public Object getService(ServiceReference reference) {
				String[] classes = OsgiServiceReferenceUtils.getServiceObjectClasses(reference);
				if (Arrays.equals(classes, new String[] { Shape.class.getName() }))
					return service;
				else
					return null;
			}
		};

		appCtx = new OsgiBundleXmlApplicationContext(new String[] { "serviceReferenceConversion.xml" });
		appCtx.setBundleContext(context);
		appCtx.setPublishContextAsService(false);
		appCtx.refresh();
	}

	@After
	public void tearDown() throws Exception {
		context = null;
		appCtx.close();
		appCtx = null;
		service = null;
		RefContainer.reference = null;
	}

	@Test
	public void testApplicationContextStarted() throws Exception {
		assertEquals(2, appCtx.getBeanDefinitionCount());
	}

	@Test
	public void testConversion() throws Exception {
		assertNotNull(RefContainer.reference);
		System.out.println(RefContainer.reference);
	}

}
