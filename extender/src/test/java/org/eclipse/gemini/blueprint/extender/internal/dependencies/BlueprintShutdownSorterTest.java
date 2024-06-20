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
import java.util.List;
import org.junit.Test;
import org.eclipse.gemini.blueprint.extender.internal.DependencyMockBundle;
import org.eclipse.gemini.blueprint.extender.internal.dependencies.shutdown.ShutdownSorter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;


import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Costin Leau
 */
public class BlueprintShutdownSorterTest {

    /**
     * <pre>
     * digraph G{
     *  A;
     *  B -> C;
     *  D -> E;
     *  E -> D;
     * }
     * </pre>
     * Expected order is C, A, B, E, D
     */
	@Test
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
        assertOrder(order, c, a, b, e, d);
    }

    /**
     * similar to tck 2 but with D publishes a service with a lower ranking and
     * needs to be destroyed first:
     *
     * <pre>
     * digraph G{
     *  A;
     *  B -> C;
     *  D -> E; (lower rank)
     *  E -> D; (high rank)
     * }
     * </pre>
     */
	@Test
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
        assertOrder(order, c, a, b, d, e);
    }

    /**
     * If the service of a managed bundle are consumed by an unmanaged bundle,
     * that dependency should not affect the shutdown ordering as gemini blueprint is only responsible for
     * orderly shutting down the bundles it is managing.
     */
	@Test
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
        assertOrder(order, c, a, b, d, e);
    }

    /**
     * If the service of a managed bundle are consumed by an unmanaged bundle,
     * that dependency should not affect the shutdown ordering as gemini blueprint is only responsible for
     * orderly shutting down the bundles it is managing.
     */
	@Test
    public void testReferencesToSelfProvidedServicesAreIgnoredForShutdownOrdering() throws Exception {
        DependencyMockBundle a = new DependencyMockBundle("A");
        DependencyMockBundle b = new DependencyMockBundle("B");
        DependencyMockBundle c = new DependencyMockBundle("C");
        DependencyMockBundle d = new DependencyMockBundle("D");
        DependencyMockBundle e = new DependencyMockBundle("E");

        e.setDependentOn(d);
        d.setDependentOn(c);
        c.setDependentOn(b);
        b.setDependentOn(a);

        a.setDependentOn(a);
        b.setDependentOn(b);

        List<Bundle> order = getOrder(a, b, c, d, e);
        assertOrder(order, a, b, c, d, e);
    }


    private void assertOrder(List<Bundle> ordered, Bundle... expected) {
        assertThat(ordered)
                .describedAs("The order %s does not match the expected order %s", ordered, expected)
                .containsExactly(expected);
    }

    private List<Bundle> getOrder(Bundle... bundles) throws BundleException {
        List<Bundle> list = new ArrayList<>(bundles.length);
        list.addAll(asList(bundles));
        List<Bundle> result = new ArrayList<>();

        while (!list.isEmpty()) {
            result.addAll(ShutdownSorter.getBundles(list));
            for (Bundle bundle : result) {
                bundle.stop();
            }
        }
        return result;
    }
}
