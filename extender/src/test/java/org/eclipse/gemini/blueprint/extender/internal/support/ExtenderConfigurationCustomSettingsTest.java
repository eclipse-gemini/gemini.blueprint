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

package org.eclipse.gemini.blueprint.extender.internal.support;

import junit.framework.TestCase;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEventMulticasterAdapter;
import org.eclipse.gemini.blueprint.extender.internal.dependencies.startup.MandatoryImporterDependencyFactory;
import org.eclipse.gemini.blueprint.mock.ArrayEnumerator;
import org.eclipse.gemini.blueprint.mock.MockBundle;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Costin Leau
 */
public class ExtenderConfigurationCustomSettingsTest extends TestCase {

	private ExtenderConfiguration config;
	private BundleContext bundleContext;
	private Bundle bundle;

	protected void setUp() throws Exception {
		bundle = new MockBundle() {

			public Enumeration findEntries(String path, String filePattern, boolean recurse) {
				return new ArrayEnumerator(new URL[] { getClass().getResource(
						"/org/eclipse/gemini/blueprint/extender/internal/support/extender-custom-config.xml") });
			}
		};

		bundleContext = new MockBundleContext(bundle);
		config = new ExtenderConfiguration();
        config.start(this.bundleContext);
	}

	protected void tearDown() throws Exception {
		config.stop(this.bundleContext);
		config = null;
	}

	public void testTaskExecutor() throws Exception {
		assertTrue(config.getTaskExecutor() instanceof SimpleAsyncTaskExecutor);
		assertEquals("conf-extender-thread", ((SimpleAsyncTaskExecutor) config.getTaskExecutor()).getThreadNamePrefix());
	}

	public void testShutdownTaskExecutor() throws Exception {
		TaskExecutor executor = config.getShutdownTaskExecutor();
		assertTrue(executor instanceof SimpleAsyncTaskExecutor);
	}

	public void testEventMulticaster() throws Exception {
		assertTrue(config.getEventMulticaster() instanceof OsgiBundleApplicationContextEventMulticasterAdapter);
	}

	public void testApplicationContextCreator() throws Exception {
		assertTrue(config.getContextCreator() instanceof DummyContextCreator);
	}

	public void testShutdownWaitTime() throws Exception {
		// 300 ms
		assertEquals(300, config.getShutdownWaitTime());
	}

	public void testShouldProcessAnnotation() throws Exception {
		assertTrue(config.shouldProcessAnnotation());
	}

	public void testDependencyWaitTime() throws Exception {
		// 200 ms
		assertEquals(200, config.getDependencyWaitTime());
	}

	public void testPostProcessors() throws Exception {
		List postProcessors = config.getPostProcessors();
		assertEquals(1, postProcessors.size());
		assertTrue(postProcessors.get(0) instanceof DummyProcessor);
	}

	public void testDependencyFactories() throws Exception {
		List factories = config.getDependencyFactories();
		assertEquals("wrong number of dependencies factories registered by default", 1, factories.size());
		assertTrue(factories.get(0) instanceof MandatoryImporterDependencyFactory);
	}
}