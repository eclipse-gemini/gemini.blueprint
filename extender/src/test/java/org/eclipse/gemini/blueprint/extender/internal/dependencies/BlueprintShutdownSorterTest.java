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

package org.eclipse.gemini.blueprint.extender.internal.dependencies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.extender.internal.DependencyMockBundle;
import org.eclipse.gemini.blueprint.extender.internal.dependencies.shutdown.ShutdownSorter;
import org.osgi.framework.Bundle;

/**
 * @author Costin Leau
 */
public class BlueprintShutdownSorterTest extends TestCase {

	// see tck-1.dot
	public void testCase1() throws Exception {
		DependencyMockBundle a = new DependencyMockBundle("A");
		DependencyMockBundle b = new DependencyMockBundle("B");
		DependencyMockBundle c = new DependencyMockBundle("C");
		DependencyMockBundle d = new DependencyMockBundle("D");
		DependencyMockBundle e = new DependencyMockBundle("E");

		b.setDependentOn(c);
		d.setDependentOn(e);
		e.setDependentOn(d);

		List<Bundle> order = getOrder(a, b, c, d, e);
		System.out.println("Shutdown order is " + order);
		assertOrder(new Bundle[] { c, a, b, e, d }, order);
	}

	// similar to tck 2 but with D publishes a service with a lower ranking and
	// needs to be destroyed first
	public void testCase2() throws Exception {
		DependencyMockBundle a = new DependencyMockBundle("A");
		DependencyMockBundle b = new DependencyMockBundle("B");
		DependencyMockBundle c = new DependencyMockBundle("C");
		DependencyMockBundle d = new DependencyMockBundle("D");
		DependencyMockBundle e = new DependencyMockBundle("E");

		b.setDependentOn(c);
		d.setDependentOn(e, -13, 12);
		e.setDependentOn(d, 0, 14);

		List<Bundle> order = getOrder(a, b, c, d, e);
		System.out.println("Shutdown order is " + order);
		assertOrder(new Bundle[] { c, a, b, d, e }, order);
	}

	/**
	 * If the service of a managed bundle are consumed by an unmanaged bundle,
	 * that dependency should not affect the shutdown ordering as gemini blueprint is only responsible for
	 * orderly shutting down the bundles it is managing.
	 */
	public void testUnmanagedBundlesAreIgnoredForShutdownOrdering() throws Exception {
		DependencyMockBundle a = new DependencyMockBundle("A");
		DependencyMockBundle b = new DependencyMockBundle("B");
		DependencyMockBundle c = new DependencyMockBundle("C");
		DependencyMockBundle d = new DependencyMockBundle("D");
		DependencyMockBundle e = new DependencyMockBundle("E");
		DependencyMockBundle unmanaged = new DependencyMockBundle("F");

		b.setDependentOn(c);
		d.setDependentOn(e, -13, 12);
		e.setDependentOn(d, 0, 14);
		a.setDependentOn(unmanaged);

		List<Bundle> order = getOrder(a, b, c, d, e);
		System.out.println("Shutdown order is " + order);
		assertOrder(new Bundle[] { c, a, b, d, e }, order);
	}


	private void assertOrder(Bundle[] expected, List<Bundle> ordered) {
		assertTrue("shutdown order is incorrect", Arrays.equals(expected, ordered.toArray()));
	}

	private List<Bundle> getOrder(Bundle... bundles) {
		List<Bundle> list = new ArrayList<Bundle>(bundles.length);
		list.addAll(Arrays.asList(bundles));
		List<Bundle> result = new ArrayList<Bundle>();

		while (!list.isEmpty()) {
			result.addAll(ShutdownSorter.getBundles(list));
			for (Bundle bundle : result) {
				try {
					bundle.stop();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		return result;
	}
}
