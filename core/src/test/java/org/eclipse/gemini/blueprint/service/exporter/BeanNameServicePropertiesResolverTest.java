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

package org.eclipse.gemini.blueprint.service.exporter;

import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import org.eclipse.gemini.blueprint.service.exporter.support.BeanNameServicePropertiesResolver;


/**
 * @author Adrian Colyer
 * @author Hal Hildebrand
 */
public class BeanNameServicePropertiesResolverTest extends TestCase {

	public void testAfterPropertiesSetNoBundleContext() throws Exception {
		try {
			new BeanNameServicePropertiesResolver().afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testGetServiceProperties() {
        BundleContext mockContext = createMock(BundleContext.class);
        Bundle mockBundle = createMock(Bundle.class);

		expect(mockContext.getBundle()).andReturn(mockBundle);
		expect(mockBundle.getSymbolicName()).andReturn("symbolic-name");
        expect(mockContext.getBundle()).andReturn(mockBundle);

        Properties props = new Properties();
        props.put(Constants.BUNDLE_VERSION, "1.0.0");

        expect(mockBundle.getHeaders()).andReturn(props);

        replay(mockBundle, mockContext);

		BeanNameServicePropertiesResolver resolver = new BeanNameServicePropertiesResolver();
		resolver.setBundleContext(mockContext);
		Map ret = resolver.getServiceProperties("myBean");

		verify(mockBundle, mockContext);

		assertEquals("5 properties", 5, ret.size());
		assertEquals("symbolic-name", ret.get("Bundle-SymbolicName"));
		assertEquals("1.0.0", ret.get("Bundle-Version"));
		assertEquals("myBean", ret.get("org.eclipse.gemini.blueprint.bean.name"));
		assertEquals("myBean", ret.get("org.springframework.osgi.bean.name"));
		assertEquals("myBean", ret.get("osgi.service.blueprint.compname"));
	}
}