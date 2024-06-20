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

package org.eclipse.gemini.blueprint.iandt.serviceproxy;

import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceList;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;
import org.junit.Test;

/**
 * @author Costin Leau
 * 
 */
public abstract class ServiceListTst extends ServiceCollectionTest {

	protected Collection createCollection() {
		ClassLoader classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundleContext.getBundle());
		OsgiServiceList col = new OsgiServiceList(null, bundleContext, classLoader, null, false);
		col.setRequiredAtStartup(false);
		// col.setInterfaces(new Class<?>[] { Date.class });
		col.afterPropertiesSet();
		return col;
	}

	@Test
	public void testListContent() throws Exception {
		List list = (List) createCollection();

		// test the list iterator
		ListIterator iter = list.listIterator();
		Object b = iter.next();

		assertSame(b, iter.previous());
	}

}
