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

package org.eclipse.gemini.blueprint.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Costin Leau
 * 
 */
public class EntryLookupControllingMockBundleTest {

	private EntryLookupControllingMockBundle bundle;

	@Before
	public void setup() throws Exception {
		bundle = new EntryLookupControllingMockBundle(new Properties());
	}

	@After
	public void tearDown() throws Exception {
		bundle = null;
	}

	@Test
	public void testGetEntry() throws Exception {
		URL url = new URL("http://bo/ho");
		bundle.setEntryReturnOnNextCallToGetEntry(url);
		assertSame(url, bundle.getEntry("bla"));
	}

	@Test
	public void testFindEntries() throws Exception {
		String[] source = new String[] {"A"};
		bundle.setResultsToReturnOnNextCallToFindEntries(source);
		Enumeration enm = bundle.findEntries(null, null, false);
		assertNotNull(enm);
		assertTrue(enm.hasMoreElements());
		assertEquals("A", enm.nextElement());
	}
}
