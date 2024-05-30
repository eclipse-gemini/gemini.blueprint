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

package org.eclipse.gemini.blueprint.context.support;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

import org.easymock.IMocksControl;

import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.beans.BeansException;

/**
 * 
 * @author Costin Leau
 */
public class AbstractBundleXmlApplicationContextTest {

	OsgiBundleXmlApplicationContext xmlContext;

	IMocksControl bundleCtxCtrl, bundleCtrl;

	BundleContext context;

	Bundle bundle;

	Dictionary dictionary;

	@Before
	public void setup() throws Exception {
		bundleCtxCtrl = createNiceControl();
		context = bundleCtxCtrl.createMock(BundleContext.class);
		bundleCtrl = createNiceControl();
		bundle = bundleCtrl.createMock(Bundle.class);

        expect(context.getBundle()).andReturn(bundle).atLeastOnce();

		dictionary = new Hashtable();

		// allow headers to be taken multiple times
        expect(bundle.getHeaders()).andReturn(dictionary).atLeastOnce();
	}

	private void createContext() {
		xmlContext = new OsgiBundleXmlApplicationContext(new String[] {}) {
			public void refresh() throws BeansException {
				// no-op
			}
		};
        xmlContext.setBundleContext(context);
    }

	@After
	public void tearDown() throws Exception {
		context = null;
		bundleCtxCtrl = null;
		xmlContext = null;
		bundle = null;
		bundleCtrl = null;
	}

	@Test
	public void testGetBundleName() {
		String symbolicName = "symbolic";
        expect(bundle.getSymbolicName()).andReturn(symbolicName).atLeastOnce();
		bundleCtxCtrl.replay();
		bundleCtrl.replay();

		// check default
		createContext();

		assertEquals(symbolicName, OsgiStringUtils.nullSafeSymbolicName(bundle));
	}

	@Test
	public void testGetBundleNameFallbackMechanism() {
		bundleCtxCtrl.replay();
		bundleCtrl.replay();

		String title = "Phat City";
		dictionary.put(Constants.BUNDLE_NAME, title);

		// check default
		createContext();

		// use the 2 symbolic name calls
		assertEquals(title, OsgiStringUtils.nullSafeName(bundle));
	}

	@Test
	public void testGetServiceName() {
		String symbolicName = "symbolic";
        expect(bundle.getSymbolicName()).andReturn(symbolicName).atLeastOnce();
		bundleCtxCtrl.replay();
		bundleCtrl.replay();

		createContext();
		assertEquals(symbolicName, OsgiStringUtils.nullSafeSymbolicName(bundle));
	}
}
