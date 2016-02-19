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
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.osgi.framework.BundleContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.List;

/**
 * @author Costin Leau
 */
public class ExtenderConfigurationDefaultSettingsTest extends TestCase {

	private ExtenderConfiguration config;
	private BundleContext bundleContext;

	protected void setUp() throws Exception {
		bundleContext = new MockBundleContext();
		config = new ExtenderConfiguration();
        this.config.start(this.bundleContext);
	}

	protected void tearDown() throws Exception {
		config.start(this.bundleContext);
		config = null;
	}

	public void testTaskExecutor() throws Exception {
		assertTrue(config.getTaskExecutor() instanceof SimpleAsyncTaskExecutor);
	}

	public void testShutdownTaskExecutor() throws Exception {
		TaskExecutor executor = config.getShutdownTaskExecutor();
		assertTrue(executor instanceof SimpleAsyncTaskExecutor);
	}

	public void testEventMulticaster() throws Exception {
		assertTrue(config.getEventMulticaster() instanceof OsgiBundleApplicationContextEventMulticasterAdapter);
	}

	public void testApplicationContextCreator() throws Exception {
        assertNull(config.getContextCreator());
	}

	public void testShutdownWaitTime() throws Exception {
		// 10 seconds in ms
		assertEquals(10 * 1000, config.getShutdownWaitTime());
	}

    public void testShutdownAsynchronously() throws Exception {
        assertTrue(config.shouldShutdownAsynchronously());
    }

    public void testShouldProcessAnnotation() throws Exception {
		assertFalse(config.shouldProcessAnnotation());
	}

	public void testDependencyWaitTime() throws Exception {
		// 5 minutes in ms
		assertEquals(5 * 60 * 1000, config.getDependencyWaitTime());
	}

	public void testPostProcessors() throws Exception {
		List postProcessors = config.getPostProcessors();
		assertTrue(postProcessors.isEmpty());
	}

	public void testDependencyFactories() throws Exception {
		List factories = config.getDependencyFactories();
		assertEquals("wrong number of dependencies factories registered by default", 1, factories.size());
		assertTrue(factories.get(0) instanceof MandatoryImporterDependencyFactory);
	}
}