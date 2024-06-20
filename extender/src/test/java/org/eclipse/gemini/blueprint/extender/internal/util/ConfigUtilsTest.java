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

package org.eclipse.gemini.blueprint.extender.internal.util;

import static org.junit.Assert.assertEquals;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Costin Leau
 * 
 */
public class ConfigUtilsTest {

	private Dictionary headers;

	@Before
	public void setup() throws Exception {
		headers = new Hashtable();
	}

	@After
	public void tearDown() throws Exception {
		headers = null;
	}

	@Test
	public void testGetCompletelyEmptySpringContextHeader() throws Exception {
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(0, locations.length);

	}

	@Test
	public void testGetEmptyConfigLocations() throws Exception {
		String entry = ";early-init-importers=true";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, entry);
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(0, locations.length);
	}

	@Test
	public void testGetNotExistingConfigLocations() throws Exception {
		String location = "osgibundle:/META-INF/non-existing.xml";
		String entry = location + "; early-init-importers=true";

		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, entry);
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(1, locations.length);
		assertEquals(location, locations[0]);

	}

	@Test
	public void testGetWildcardConfigLocs() throws Exception {
		String location = "classpath:/META-INF/spring/*.xml";
		String entry = location + "; early-init-importers=true";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, entry);
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(1, locations.length);
		assertEquals(location, locations[0]);
	}

	@Test
	public void testMultipleConfigLocs() throws Exception {
		String location1 = "classpath:/META-INF/spring/*.xml";
		String location2 = "osgibundle:/META-INF/non-existing.xml";

		String entry = location1 + "," + location2 + "; early-init-importers=true";
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER, entry);
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(2, locations.length);
		assertEquals(location1, locations[0]);
		assertEquals(location2, locations[1]);
	}

	@Test
	public void testLocationWithMultipleDots() throws Exception {
		headers.put(ConfigUtils.SPRING_CONTEXT_HEADER,
			"META-INF/file.with.multiple.dots.xml, META-INF/another.file.xml");
		String[] locations = ConfigUtils.getHeaderLocations(headers);
		assertEquals(2, locations.length);
	}
}
