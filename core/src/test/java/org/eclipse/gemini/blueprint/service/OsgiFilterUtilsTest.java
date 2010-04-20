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

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

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
public class OsgiFilterUtilsTest extends TestCase {

	private String[] classes;

	private Dictionary dictionary;

	protected void setUp() throws Exception {
		classes = new String[] { Object.class.getName(), Cloneable.class.getName(), Serializable.class.getName() };
		dictionary = new Hashtable();
		dictionary.put(Constants.OBJECTCLASS, classes);
	}

	protected void tearDown() throws Exception {
		dictionary = null;
		classes = null;
	}

	public void testNoArgument() {
		try {
			OsgiFilterUtils.unifyFilter((String) null, null);
			fail("should have thrown exception");
		}
		catch (Exception ex) {
			// expected
		}
	}

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

	public void testOnlyClassArgument() {
		String filter = OsgiFilterUtils.unifyFilter(classes[0], null);
		assertNotNull(filter);
		assertTrue(OsgiFilterUtils.isValidFilter(filter));
	}

	public void testJustClassWithNoFilter() {
		String fl = OsgiFilterUtils.unifyFilter(classes[0], null);
		String filter = OsgiFilterUtils.unifyFilter((String) null, fl);

		assertEquals("filter shouldn't have been modified", fl, filter);
	}

	public void testClassWithExistingFilter() {
		String filter = "(o=univ*of*mich*)";
		String fl = OsgiFilterUtils.unifyFilter(classes[0], filter);
		assertTrue(OsgiFilterUtils.isValidFilter(fl));
	}

	public void testMultipleClassesWithExistingFilter() {
		String filter = "(|(sn=Jensen)(cn=Babs J*))";
		String fl = OsgiFilterUtils.unifyFilter(classes, filter);
		assertTrue(OsgiFilterUtils.isValidFilter(fl));
	}

	public void testMultipleClassesAddedOneByOne() {
		String filter = OsgiFilterUtils.unifyFilter(classes[0], null);
		filter = OsgiFilterUtils.unifyFilter(classes[1], filter);
		filter = OsgiFilterUtils.unifyFilter(classes[2], filter);

		Filter osgiFilter = OsgiFilterUtils.createFilter(filter);

		// verify the filter using the matching against a dictionary
		assertTrue(osgiFilter.matchCase(dictionary));
	}

	public void testMultipleClassesAddedAtOnce() {
		String filter = OsgiFilterUtils.unifyFilter(classes, null);
		Filter osgiFilter = OsgiFilterUtils.createFilter(filter);
		// verify the filter using the matching against a dictionary
		assertTrue(osgiFilter.matchCase(dictionary));
	}

	public void testNonMatching() {
		String filter = OsgiFilterUtils.unifyFilter(classes, null);
		Filter osgiFilter = OsgiFilterUtils.createFilter(filter);
		dictionary.put(Constants.OBJECTCLASS, new String[] { classes[0] });

		// verify the filter using the matching against a dictionary
		assertFalse(osgiFilter.matchCase(dictionary));
	}

	public void testNoKeyOrItemSpecified() {
		try {
			OsgiFilterUtils.unifyFilter(null, null, null);
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testNoKeySpecified() {
		try {
			OsgiFilterUtils.unifyFilter(null, new String[] { classes[0] }, null);
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
		}
	}

	public void testNoItemSpecified() {
		try {
			OsgiFilterUtils.unifyFilter(Constants.OBJECTCLASS, null, null);
			fail("should have thrown exception");
		}
		catch (IllegalArgumentException ex) {
		}
	}

	public void testNoKeyOrItemButFilterSpecified() {
		String filter = OsgiFilterUtils.unifyFilter("beanName", new String[] { "myBean" }, null);
		assertTrue(OsgiFilterUtils.isValidFilter(filter));
	}

	public void testAddItemsUnderMultipleKeys() {
		String filterA = OsgiFilterUtils.unifyFilter("firstKey", new String[] { "A", "B", "valueA" }, "(c=*)");
		String filterB = OsgiFilterUtils.unifyFilter("secondKey", new String[] { "X", "Y", "valueZ" }, filterA);
		assertTrue(OsgiFilterUtils.isValidFilter(filterB));
	}

	public void testUnifyWhenNoItemIsSpecified() {
		String fl = "(c=*)";
		String filter = OsgiFilterUtils.unifyFilter("someKey", null, fl);
		assertEquals(fl, filter);

		filter = OsgiFilterUtils.unifyFilter("someKey", new String[0], fl);
		assertEquals(fl, filter);

		filter = OsgiFilterUtils.unifyFilter("someKey", new String[] { null }, fl);
		assertEquals(fl, filter);

	}

    protected BundleContext getBundleContext() {
        return new MockBundleContext() {
            public Filter createFilter(String filter) throws InvalidSyntaxException {
                return FrameworkUtil.createFilter(filter);
            }
        };
    }
}
