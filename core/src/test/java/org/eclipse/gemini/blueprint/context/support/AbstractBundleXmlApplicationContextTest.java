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

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.beans.BeansException;

/**
 * 
 * @author Costin Leau
 */
public class AbstractBundleXmlApplicationContextTest extends TestCase {

	OsgiBundleXmlApplicationContext xmlContext;

	MockControl bundleCtxCtrl, bundleCtrl;

	BundleContext context;

	Bundle bundle;

	Dictionary dictionary;

	protected void setUp() throws Exception {
		bundleCtxCtrl = MockControl.createNiceControl(BundleContext.class);
		context = (BundleContext) bundleCtxCtrl.getMock();
		bundleCtrl = MockControl.createNiceControl(Bundle.class);
		bundle = (Bundle) bundleCtrl.getMock();

		bundleCtxCtrl.expectAndReturn(context.getBundle(), bundle, MockControl.ONE_OR_MORE);

		dictionary = new Hashtable();

		// allow headers to be taken multiple times
		bundleCtrl.expectAndReturn(bundle.getHeaders(), dictionary, MockControl.ONE_OR_MORE);
	}

	private void createContext() {
		xmlContext = new OsgiBundleXmlApplicationContext(new String[] {}) {
			public void refresh() throws BeansException {
				// no-op
			}
		};
        xmlContext.setBundleContext(context);
    }

	protected void tearDown() throws Exception {
		// bundleCtxCtrl.verify();
		// bundleCtrl.verify();
		context = null;
		bundleCtxCtrl = null;
		xmlContext = null;
		bundle = null;
		bundleCtrl = null;
	}

	public void testGetBundleName() {
		String symbolicName = "symbolic";
		// bundleCtrl.reset();
		bundleCtrl.expectAndReturn(bundle.getSymbolicName(), symbolicName, MockControl.ONE_OR_MORE);
		bundleCtxCtrl.replay();
		bundleCtrl.replay();

		// check default
		createContext();

		assertEquals(symbolicName, OsgiStringUtils.nullSafeSymbolicName(bundle));
	}

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

	public void testGetServiceName() {
		String symbolicName = "symbolic";
		// bundleCtrl.reset();
		bundleCtrl.expectAndReturn(bundle.getSymbolicName(), symbolicName, MockControl.ONE_OR_MORE);
		bundleCtxCtrl.replay();
		bundleCtrl.replay();

		createContext();
		assertEquals(symbolicName, OsgiStringUtils.nullSafeSymbolicName(bundle));

	}

}
