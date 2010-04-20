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

package org.eclipse.gemini.blueprint.extender.internal.support;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.extender.internal.support.NamespacePlugins;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Tests for NamespacePlugins support
 * 
 * @author Adrian Colyer
 */
public class NamespacePluginsTest extends TestCase {

	private NamespacePlugins namespacePlugins;


	protected void setUp() throws Exception {
		super.setUp();
		this.namespacePlugins = new NamespacePlugins();
	}

	public void testCantResolveWithNoPlugins() throws IOException, SAXException {
		assertNull("Should be unable to resolve namespace", this.namespacePlugins.resolve("http://org.xyz"));
		assertNull("Should be unable to resolve entity", this.namespacePlugins.resolveEntity("pub-id", "sys-id"));
	}

	public void testCanResolveNamespaceFromBundleAfterAddingPlugin() throws IOException, SAXException {
		Bundle b = new MockBundle();
		this.namespacePlugins.addPlugin(b, false, true);
		NamespaceHandler handler = this.namespacePlugins.resolve("http://www.springframework.org/schema/testme");
		assertNotNull("should find handler", handler);
		assertTrue("should be TestHandler", handler instanceof TestHandler);
	}

	public void testCantResolveNamespaceAfterRemovingPlugin() throws IOException, SAXException {
		Bundle b = new MockBundle();
		this.namespacePlugins.addPlugin(b, false, true);
		this.namespacePlugins.removePlugin(b);
		assertNull("Should be unable to resolve namespace",
			this.namespacePlugins.resolve("http://www.springframework.org/schema/testme"));
	}

	public void testCanResolveEntityAfterAddingPlugin() throws IOException, SAXException {
		Bundle b = new MockBundle();
		this.namespacePlugins.addPlugin(b, false, true);
		InputSource resolver = this.namespacePlugins.resolveEntity("public-id",
			"http://www.springframework.org/schema/beans/testme.xsd");
		assertNotNull("Should find resolver", resolver);
	}

	public void testCantResolveEntityAfterRemovingPlugin() throws IOException, SAXException {
		Bundle b = new MockBundle();
		this.namespacePlugins.addPlugin(b, false, true);
		this.namespacePlugins.removePlugin(b);
		InputSource resolver = this.namespacePlugins.resolveEntity("public-id",
			"http://www.springframework.org/schema/beans/testme.xsd");
		assertNull("Should be unable to resolve entity", resolver);
	}
}