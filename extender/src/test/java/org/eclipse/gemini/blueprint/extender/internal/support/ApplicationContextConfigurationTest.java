/*
 Copyright (c) 2006, 2010 VMware Inc.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 and Apache License v2.0 which accompanies this distribution.
 The Eclipse Public License is available at
 http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 is available at http://www.opensource.org/licenses/apache2.0.php.
 You may elect to redistribute this code under either of these licenses.

 Contributors:
 VMware Inc.
 */

package org.eclipse.gemini.blueprint.extender.internal.support;

import junit.framework.TestCase;
import org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.support.BlueprintContainerConfig;
import org.eclipse.gemini.blueprint.extender.support.ApplicationContextConfiguration;
import org.eclipse.gemini.blueprint.mock.ArrayEnumerator;
import org.osgi.framework.Bundle;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils.LEGACY_SPRING_DM_CONTEXT_LOCATION;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test that given a bundle, we can correctly determine the spring configuration
 * required for it.
 *
 * @author Adrian Colyer
 */
public class ApplicationContextConfigurationTest extends TestCase {
    private final URL metaInfSpringFolder;

    {
        try {
            metaInfSpringFolder = new URL("file:///META-INF/spring/");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private final URL context_xml = getClass().getClassLoader().getResource("org/eclipse/gemini/blueprint/extender/internal/support/META-INF/spring/context.xml");
    private final URL context_two_xml = getClass().getClassLoader().getResource("org/eclipse/gemini/blueprint/extender/internal/support/META-INF/spring/context-two.xml");

    public void testBundleWithNoHeaderAndNoMetaInfSpringResourcesIsNotSpringPowered() {
        Bundle bundle = mock(Bundle.class);
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertFalse("bundle is not spring powered", config.isBlueprintConfigurationPresent());
    }

    public void testBundleWithSpringResourcesAndNoHeaderIsSpringPowered() {
        Bundle bundle = mock(Bundle.class);
        addSpringContextConfigurationPresent(bundle);
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertTrue("bundle is spring powered", config.isBlueprintConfigurationPresent());
    }

    public void testBundleWithHeaderAndNoMetaInfResourcesIsSpringPowered() throws Exception {
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "META-INF/spring/context.xml");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        doReturn(new URL("file://META-INF/spring/context.xml")).when(bundle).getEntry(anyString());
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertTrue("bundle is spring powered", config.isBlueprintConfigurationPresent());
    }

    public void testBundleWithNoHeaderShouldWaitFiveMinutes() {
        Bundle bundle = mock(Bundle.class);
        addSpringContextConfigurationPresent(bundle);
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertEquals("bundle should timeout in five minutes", new Long(5 * 60 * 1000), new Long(config.getTimeout()));
    }

    public void testBundleWithWaitFiveSecondWaitForTimeout() {
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "*;timeout:=5");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertTrue("bundle should be Spring powered", config.isBlueprintConfigurationPresent());
        assertEquals("bundle should timeout in 5 s", new Long(5 * 1000), new Long(config.getTimeout()));
    }

    public void testBundleWithWaitForEver() {
        // *;flavour
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "*;timeout:=none");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertTrue("bundle should be Spring powered", config.isBlueprintConfigurationPresent());
        assertEquals("bundle should timeout -2 (indicates forever)", new Long(-2), new Long(config.getTimeout()));
    }

    public void tstConfigLocationsInMetaInfNoHeader() {
        Bundle bundle = mock(Bundle.class);
        addSpringContextConfigurationPresent(bundle);
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        String[] configFiles = config.getConfigurationLocations();
        assertEquals("0 config files", 0, configFiles.length);
        // assertEquals("bundle-url:file://META-INF/spring/context.xml",
        // configFiles[0]);
        // assertEquals("bundle-url:file://META-INF/spring/context-two.xml",
        // configFiles[1]);
    }

    public void tstConfigLocationsInMetaInfWithHeader() throws Exception {
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "META-INF/spring/context.xml");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        doReturn(new URL("file://META-INF/spring/context.xml")).when(bundle).getEntry(anyString());
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        String[] configFiles = config.getConfigurationLocations();
        assertTrue("bundle should be Spring powered", config.isBlueprintConfigurationPresent());
        assertEquals("osgibundle:META-INF/spring/context.xml", configFiles[0]);
    }

    public void tstConfigLocationsInMetaInfWithWildcardHeader() throws Exception {
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "*;wait-for-dependencies:=false");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        doReturn(context_xml).when(bundle).getEntry(anyString());
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        String[] configFiles = config.getConfigurationLocations();
        assertTrue("bundle should be Spring powered", config.isBlueprintConfigurationPresent());
        assertEquals("1 config files", 1, configFiles.length);
        assertEquals(LEGACY_SPRING_DM_CONTEXT_LOCATION, configFiles[0]);
    }

    public void tstEmptyConfigLocationsInMetaInf() throws Exception {
        System.out.println("tsst");
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", ";wait-for-dependencies:=false");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        doReturn(context_xml).when(bundle).getEntry(anyString());
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        String[] configFiles = config.getConfigurationLocations();
        assertTrue("bundle should be Spring powered", config.isBlueprintConfigurationPresent());
        assertEquals("1 config files", 1, configFiles.length);
        assertEquals(LEGACY_SPRING_DM_CONTEXT_LOCATION, configFiles[0]);
    }

    public void tstConfigLocationsInMetaInfWithHeaderAndDependencies() throws Exception {
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "META-INF/spring/context.xml;wait-for-dependencies:=false");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        doReturn(context_xml).when(bundle).getEntry(anyString());
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        String[] configFiles = config.getConfigurationLocations();
        assertTrue("bundle should be Spring powered", config.isBlueprintConfigurationPresent());
        assertEquals("2 config files", 1, configFiles.length);
        assertEquals("osgibundle:META-INF/spring/context.xml", configFiles[0]);
    }

    public void tstBundleWithHeaderWithBadEntriesAndNoMetaInfResourcesIsNotSpringPowered() {
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "META-INF/splurge/context.xml");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertFalse("bundle is not spring powered", config.isBlueprintConfigurationPresent());
    }

    public void tstHeaderWithWildcardEntryAndNoMetaInfResources() {
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "*;wait-for-dependencies:=false");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertFalse("not spring powered", config.isBlueprintConfigurationPresent());
    }

    public void tstHeaderWithBadEntry() throws Exception {
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "META-INF/spring/context-two.xml,META-INF/splurge/context.xml,");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        doReturn(new URL("file://META-INF/spring/context-two.xml")).when(bundle).getEntry(anyString());
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertFalse("bundle is not spring powered", config.isBlueprintConfigurationPresent());
        String[] configFiles = config.getConfigurationLocations();
        assertEquals("0 config file", 0, configFiles.length);
    }

    public void testCreateAsynchronouslyDefaultTrue() throws Exception {
        // *;flavour
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "*;timeout:=none");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        doReturn(context_xml).when(bundle).getEntry(anyString());
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertTrue("bundle should be Spring powered", config.isBlueprintConfigurationPresent());
        assertTrue("bundle should have create-asynchronously = true", config.isCreateAsynchronously());
    }

    public void testSetCreateAsynchronouslyTrue() {
        // *;flavour
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "*;create-asynchronously:=true");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertTrue("bundle should be Spring powered", config.isBlueprintConfigurationPresent());
        assertTrue("bundle should have create-asynchronously = true", config.isCreateAsynchronously());
    }

    public void testSetCreateAsynchronouslyFalse() throws Exception {
        // *;flavour
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "META-INF/spring/context.xml;create-asynchronously:=false");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        doReturn(context_xml).when(bundle).getEntry(anyString());
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertTrue("bundle should be Spring powered", config.isBlueprintConfigurationPresent());
        assertFalse("bundle should have create-asynchronously = false", config.isCreateAsynchronously());
    }

    public void testCreateAsynchronouslyDefaultTrueIfAbsent() {
        // *;flavour
        Dictionary<String, Object> headers = new Hashtable<>();
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertTrue("bundle should have create-asynchronously = true", config.isCreateAsynchronously());
    }

    public void testCreateAsynchronouslyDefaultTrueIfGarbage() {
        // *;flavour
        Dictionary<String, Object> headers = new Hashtable<>();
        headers.put("Spring-Context", "*;favour:=false");
        Bundle bundle = mock(Bundle.class);
        doReturn(headers).when(bundle).getHeaders();
        addSpringContextConfigurationPresent(bundle);
        ApplicationContextConfiguration config = new BlueprintContainerConfig(bundle);
        assertTrue("bundle should have create-asynchronously = true", config.isCreateAsynchronously());
    }

    private void addSpringContextConfigurationPresent(Bundle bundle) {

        doAnswer(r -> new ArrayEnumerator<>(metaInfSpringFolder))
                .when(bundle)
                .findEntries(
                        "META-INF",
                        "spring",
                        false);

        doAnswer(r -> new ArrayEnumerator<>(context_xml, context_two_xml))
                .when(bundle)
                .findEntries(
                        "/META-INF/spring/",
                        null,
                        false);
    }
}
