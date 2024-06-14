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

package org.eclipse.gemini.blueprint.iandt.propertyplaceholder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilePermission;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.PropertyPermission;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationPermission;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.junit.Test;

/**
 * Integration test for OsgiPropertyPlaceholder.
 * 
 * @author Costin Leau
 */
public class PropertyPlaceholderTest extends BaseIntegrationTest {

	private final static String ID = "PropertyPlaceholderTest-123";

	private final static Dictionary DICT = new Hashtable();

	private ConfigurableOsgiBundleApplicationContext ctx;

	private static String CONFIG_DIR = "test-config";


	protected static void initializeDirectory(String dir) {
		File directory = new File(dir);
		remove(directory);
		assertTrue(dir + " directory successfully created", directory.mkdirs());
	}

	private static void remove(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isDirectory()) {
					remove(file);
				}
				else {
					assertTrue(file + " deleted", file.delete());
				}
			}
			assertTrue(directory + " directory successfully cleared", directory.delete());
		}
	}

	protected String[] getTestBundlesNames() {
		return new String[] {
		// required by cm_all for logging
		"org.apache.felix, org.apache.felix.configadmin, 1.4.0" };
	}

	protected void onSetUp() throws Exception {
		DICT.put("foo", "bar");
		DICT.put("white", "horse");
		// Set up the bundle storage dirctory
		System.setProperty("com.gatespace.bundle.cm.store", CONFIG_DIR);
		System.setProperty("felix.cm.dir", CONFIG_DIR);
		initializeDirectory(CONFIG_DIR);
		prepareConfiguration();

		String[] locations = new String[] { "org/eclipse/gemini/blueprint/iandt/propertyplaceholder/placeholder.xml" };
		ctx = new OsgiBundleXmlApplicationContext(locations);
		ctx.setBundleContext(bundleContext);
		ctx.refresh();
	}

	protected void onTearDown() throws Exception {
		if (ctx != null)
			ctx.close();
	}

	// add a default table into OSGi
	private void prepareConfiguration() throws Exception {

		ServiceReference ref = OsgiServiceReferenceUtils.getServiceReference(bundleContext,
			ConfigurationAdmin.class.getName(), null);

		ConfigurationAdmin admin = (ConfigurationAdmin) bundleContext.getService(ref);
		Configuration config = admin.getConfiguration(ID);
		config.update(DICT);
	}

	@Test
	public void testFoundProperties() throws Exception {
		String bean = (String) ctx.getBean("bean1");
		assertEquals("horse", bean);
	}

	@Test
	public void testFallbackProperties() throws Exception {
		String bean = (String) ctx.getBean("bean2");
		assertEquals("treasures", bean);
	}

	protected List getTestPermissions() {
		List perms = super.getTestPermissions();
		// export package
		perms.add(new AdminPermission("*", AdminPermission.EXECUTE));
		perms.add(new PropertyPermission("*", "write"));
		perms.add(new PropertyPermission("*", "read"));
		perms.add(new FilePermission("<<ALL FILES>>", "read"));
		perms.add(new FilePermission("<<ALL FILES>>", "delete"));
		perms.add(new FilePermission("<<ALL FILES>>", "write"));
		perms.add(new ConfigurationPermission("*", ConfigurationPermission.CONFIGURE));
		return perms;
	}
}