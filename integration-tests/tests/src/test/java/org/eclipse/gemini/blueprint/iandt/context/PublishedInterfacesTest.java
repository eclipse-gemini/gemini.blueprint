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

package org.eclipse.gemini.blueprint.iandt.context;

import static org.junit.Assert.assertEquals;

import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * Test checking the context published interfaces.
 * 
 * @author Costin Leau
 * 
 */
public class PublishedInterfacesTest extends BaseIntegrationTest {
	@Test
	public void testXmlOsgiContext() throws Exception {
		// At this point, only the automatically generated test application context should be published as an OSGi service.
		checkedPublishedOSGiService(1);

		OsgiBundleXmlApplicationContext context = new OsgiBundleXmlApplicationContext(
			new String[] { "/org/eclipse/gemini/blueprint/iandt/context/no-op-context.xml" });
		context.setBundleContext(bundleContext);
		context.refresh();

		// Now, the default and the explicitly create application context should be present
		checkedPublishedOSGiService(2);
		context.close();

		// When an application context is closed, the respective service must also be unpublished. Thus, there should only be one service again.
		checkedPublishedOSGiService(1);
	}

	private void checkedPublishedOSGiService(int expectedContexts) throws Exception {
		ServiceReference[] refs = bundleContext.getServiceReferences(
			ConfigurableOsgiBundleApplicationContext.class.getName(), null);
		assertEquals("different number of published contexts encountered", expectedContexts, refs.length);

		for (int i = 0; i < refs.length; i++) {
			ServiceReference serviceReference = refs[i];
			String[] interfaces = (String[]) serviceReference.getProperty(Constants.OBJECTCLASS);
			assertEquals("not enough interfaces published", 15, interfaces.length);
			assertEquals(Version.emptyVersion, serviceReference.getProperty(Constants.BUNDLE_VERSION));
			assertEquals(bundleContext.getBundle().getSymbolicName(),
				serviceReference.getProperty(Constants.BUNDLE_SYMBOLICNAME));
		}
	}
}
