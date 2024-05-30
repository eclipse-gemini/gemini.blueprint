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

package org.eclipse.gemini.blueprint.extender.internal.blueprint.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.support.BlueprintConfigUtils;
import org.springframework.beans.propertyeditors.CharacterEditor;
import org.springframework.util.ObjectUtils;

/**
 * Test intended mainly for checking the header parsing.
 * 
 * @author Costin Leau
 */
public class BlueprintConfigUtilsTest {

	private Dictionary headers;
	private String dir1 = "foo:=bar", dir2 = "ignored-directive:=true", dir3 = "version=123";
	private String loc1 = "OSGI-INF/foo.xml", loc2 = "/META-INF/spring/context.xml";

	@Before
	public void setup() throws Exception {
		headers = new Properties();
	}

	@After
	public void tearDown() throws Exception {
		headers = null;
	}

	private String[] getLocations(String location) {
		headers.put(BlueprintConfigUtils.BLUEPRINT_HEADER, location);
		return BlueprintConfigUtils.getBlueprintHeaderLocations(headers);
	}

	@Test
	public void testNoLocation() throws Exception {
		String[] locs = getLocations(dir1 + ";" + dir3);
		assertTrue(ObjectUtils.isEmpty(locs));
	}

	@Test
	public void testOneLocationWithNoDirective() throws Exception {
		String[] locs = getLocations(loc1);
		assertEquals(1, locs.length);
		assertEquals(loc1, locs[0]);
	}

	@Test
	public void testOneLocationWithOneDirective() throws Exception {
		String[] locs = getLocations(loc1 + ";" + dir1);
		assertEquals(1, locs.length);
		assertEquals(loc1, locs[0]);
	}

	@Test
	public void testOneLocationWithMultipleDirectives() throws Exception {
		String[] locs = getLocations(dir2 + ";" + dir3 + ";" + loc2 + ";" + dir1 + ";" + dir2);
		assertEquals(1, locs.length);
		assertEquals(loc2, locs[0]);
	}

	@Test
	public void testMultipleLocationsWODirectives() throws Exception {
		String[] locs = getLocations(loc1 + "," + loc2);
		assertEquals(2, locs.length);
		assertEquals(loc1, locs[0]);
		assertEquals(loc2, locs[1]);
	}

	@Test
	public void testMultipleLocationsWithMultipleDirectives() throws Exception {
		String[] locs = getLocations(dir1 + ";" + loc1 + ";" + dir2 + "," + dir3 + ";" + loc2);
		assertEquals(2, locs.length);
		assertEquals(loc1, locs[0]);
		assertEquals(loc2, locs[1]);
	}

	@Test
	public void testUnicodeChars() throws Exception {
		CharacterEditor editor = new CharacterEditor(false);
		editor.setAsText("\\u2122");
		System.out.println(editor.getAsText());
	}
}