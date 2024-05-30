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

package org.eclipse.gemini.blueprint.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.assertj.core.api.Assertions;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;

/**
 * @author Costin Leau
 * 
 */
public class OsgiFilterUtilsTest {

	private String[] classes;

	private Dictionary dictionary;

	@Before
	public void setup() throws Exception {
		classes = new String[] { Object.class.getName(), Cloneable.class.getName(), Serializable.class.getName() };
		dictionary = new Hashtable();
		dictionary.put(Constants.OBJECTCLASS, classes);
	}

	@After
	public void tearDown() throws Exception {
		dictionary = null;
		classes = null;
	}

	@Test
	public void testNoArgument() {
		try {
			OsgiFilterUtils.unifyFilter((String) null, null);
			fail("should have thrown exception");
		}
		catch (Exception ex) {
			// expected
		}
	}

	@Test
	public void testClassArrayWithGarbage() {
		String[] garbage = new String[] { null, null };
		try {
			OsgiFilterUtils.unifyFilter(garbage, null);
			fail("should have thrown exception " + IllegalArgumentException.class.getName());
		}
		catch (IllegalArgumentException iae) {
			// expected
		}
	}

	@Test
	public void testOnlyClassArgument() {
		String filter = OsgiFilterUtils.unifyFilter(classes[0], null);
		assertNotNull(filter);
		assertTrue(OsgiFilterUtils.isValidFilter(filter));
	}

	@Test
	public void testJustClassWithNoFilter() {
		String fl = OsgiFilterUtils.unifyFilter(classes[0], null);
		String filter = OsgiFilterUtils.unifyFilter((String) null, fl);

		assertEquals("filter shouldn't have been modified", fl, filter);
	}

	@Test
	public void testClassWithExistingFilter() {
		String filter = "(o=univ*of*mich*)";
		String fl = OsgiFilterUtils.unifyFilter(classes[0], filter);
		assertTrue(OsgiFilterUtils.isValidFilter(fl));
	}

	@Test
	public void testMultipleClassesWithExistingFilter() {
		String filter = "(|(sn=Jensen)(cn=Babs J*))";
		String fl = OsgiFilterUtils.unifyFilter(classes, filter);
		assertTrue(OsgiFilterUtils.isValidFilter(fl));
	}

	@Test
	public void testMultipleClassesAddedOneByOne() {
		String filter = OsgiFilterUtils.unifyFilter(classes[0], null);
		filter = OsgiFilterUtils.unifyFilter(classes[1], filter);
		filter = OsgiFilterUtils.unifyFilter(classes[2], filter);

		Filter osgiFilter = OsgiFilterUtils.createFilter(filter);

		// verify the filter using the matching against a dictionary
		assertTrue(osgiFilter.matchCase(dictionary));
	}

	@Test
	public void testMultipleClassesAddedAtOnce() {
		String filter = OsgiFilterUtils.unifyFilter(classes, null);
		Filter osgiFilter = OsgiFilterUtils.createFilter(filter);
		// verify the filter using the matching against a dictionary
		assertTrue(osgiFilter.matchCase(dictionary));
	}

	@Test
	public void testNonMatching() {
		String filter = OsgiFilterUtils.unifyFilter(classes, null);
		Filter osgiFilter = OsgiFilterUtils.createFilter(filter);
		dictionary.put(Constants.OBJECTCLASS, new String[] { classes[0] });

		// verify the filter using the matching against a dictionary
		assertFalse(osgiFilter.matchCase(dictionary));
	}

	@Test
	public void testNoKeyOrItemSpecified() {
		try {
			OsgiFilterUtils.unifyFilter(null, null, null);
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	@Test
	public void testNoKeySpecified() {
		try {
			OsgiFilterUtils.unifyFilter(null, new String[] { classes[0] }, null);
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
		}
	}

	@Test
	public void testNoItemSpecified() {
		try {
			OsgiFilterUtils.unifyFilter(Constants.OBJECTCLASS, null, null);
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
		}
	}

	@Test
	public void testNoKeyOrItemButFilterSpecified() {
		String filter = OsgiFilterUtils.unifyFilter("beanName", new String[] { "myBean" }, null);
		assertTrue(OsgiFilterUtils.isValidFilter(filter));
	}

	@Test
	public void testAddItemsUnderMultipleKeys() {
		String filterA = OsgiFilterUtils.unifyFilter("firstKey", new String[] { "A", "B", "valueA" }, "(c=*)");
		String filterB = OsgiFilterUtils.unifyFilter("secondKey", new String[] { "X", "Y", "valueZ" }, filterA);
		assertTrue(OsgiFilterUtils.isValidFilter(filterB));
	}

	@Test
	public void testUnifyWhenNoItemIsSpecified() {
		String fl = "(c=*)";
		String filter = OsgiFilterUtils.unifyFilter("someKey", null, fl);
		assertEquals(fl, filter);

		filter = OsgiFilterUtils.unifyFilter("someKey", new String[0], fl);
		assertEquals(fl, filter);

		filter = OsgiFilterUtils.unifyFilter("someKey", new String[] { null }, fl);
		assertEquals(fl, filter);

	}

	/**
	 * As per OSGI r5 spec, https://www.scribd.com/document/137122057/osgi-core-5-0-0, the characters
	 * <code> \ * ( )</code>  must be escaped using a \ character.
	 */
	@Test
	public void testFiltersFromServiceReferencesAreEscaped() {
		MockServiceReference serviceReference = new MockServiceReference();
		Dictionary<String, String> properties = new Hashtable<>();
		properties.put("ds.target", "(thing=ball)");
		properties.put("all.escapable", "*()\\");
		serviceReference.setProperties(properties);

		String actual = OsgiFilterUtils.getFilter(serviceReference);

		Assertions.assertThat(actual).contains("(ds.target=\\(thing=ball\\)");
		Assertions.assertThat(actual).contains("\\*\\(\\)\\");
	}

    protected BundleContext getBundleContext() {
        return new MockBundleContext() {
            public Filter createFilter(String filter) throws InvalidSyntaxException {
                return FrameworkUtil.createFilter(filter);
            }
        };
    }
}
