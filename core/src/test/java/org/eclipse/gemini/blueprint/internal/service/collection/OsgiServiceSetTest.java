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

package org.eclipse.gemini.blueprint.internal.service.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceCollection;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;

/**
 * @author Costin Leau
 * 
 */
public class OsgiServiceSetTest extends AbstractOsgiCollectionTest {

	private Iterator iter;

	private Dictionary serviceProps;

	@Before
	public void setup() throws Exception {
		super.setup();
		iter = col.iterator();

		serviceProps = new Properties();
		// set the id to test uniqueness
		serviceProps.put(Constants.SERVICE_ID, Long.valueOf(13));

	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		col = null;
		iter = null;
	}

	OsgiServiceCollection createCollection() {
		return new OsgiServiceSet(null, context, getClass().getClassLoader(), createProxyCreator(new Class<?>[] {
				Wrapper.class, Comparable.class }), false);
	}

	@Test
	public void testAddDuplicates() {
		long time1 = 123;

		Wrapper date = new DateWrapper(time1);

		assertEquals(0, col.size());

		addService(date, serviceProps);
		assertEquals(1, col.size());

		addService(date, serviceProps);
		assertEquals("set accepts duplicate services", 1, col.size());
	}

	@Test
	public void testAddEqualServiceInstances() {
		long time = 123;
		Wrapper date1 = new DateWrapper(time);
		Wrapper date2 = new DateWrapper(time);

		assertEquals(date1, date2);

		assertEquals(0, col.size());

		addService(date1, serviceProps);
		assertEquals(1, col.size());
		addService(date2, serviceProps);
		assertEquals("set accepts duplicate services", 1, col.size());
	}

	@Test
	public void testAddEqualServiceInstancesWithIterator() {
		long time = 123;
		Wrapper date1 = new DateWrapper(time);
		Wrapper date2 = new DateWrapper(time);

		assertEquals(date1, date2);

		assertEquals(0, col.size());

		assertFalse(iter.hasNext());
		addService(date1, serviceProps);
		assertTrue(iter.hasNext());
		assertEquals(date1.execute(), ((Wrapper) iter.next()).execute());
		assertFalse(iter.hasNext());
		addService(date1, serviceProps);
		assertFalse("set accepts duplicate services", iter.hasNext());
	}

	@Test
	public void testRemoveDuplicates() {
		long time1 = 123;
		Wrapper date = new DateWrapper(time1);
		Wrapper date2 = new DateWrapper(time1 * 2);

		assertEquals(0, col.size());
		addService(date);
		assertEquals(1, col.size());
		addService(date2);
		assertEquals(2, col.size());

		removeService(date2);
		assertEquals(1, col.size());
		removeService(date2);
		assertEquals(1, col.size());
	}
}
