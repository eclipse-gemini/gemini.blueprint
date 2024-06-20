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

package org.eclipse.gemini.blueprint.context.support;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public abstract class BundleContextAwareProcessorTest {

    // TODO: is this test still applicable?  Does not look like it is testing anything.

	private BundleContext mockContext;
	private BundleContextAware mockAware;
	
	@Before
	public void setup() throws Exception {
		this.mockContext = createMock(BundleContext.class);
		// no tests should ever call the mockContext, we're really
		// using it just as a convenient implementation
		replay(mockContext);

		this.mockAware = createMock(BundleContextAware.class);
	}
	
	@After
	public void tearDown() throws Exception {
		verify(mockContext);
	}
	
	@Test
	public void testBeforeInitializationNoBundleContext() {
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(null);
		replay(mockAware);
		try {
//			bcaProcessor.postProcessAfterInstantiation(this.mockAware, "aName");
			fail("should throw an IllegalStateException when no BundleContext available");
		} 
		catch(IllegalStateException ex) {
			assertEquals("Cannot satisfy BundleContextAware for bean 'aName' without BundleContext",
					     ex.getMessage());
		}
		verify(mockAware);
	}
	
	@Test
	public void testBeforeInitializationNonImplementer() {
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(this.mockContext);
		Object bean = new Object();
		Object ret = bcaProcessor.postProcessBeforeInitialization(bean, "aName");
		assertSame("should return same bean instance",bean,ret);
	}
	
	@Test
	public void testBeforeInitializationBundleContextImplementer() {
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(this.mockContext);
		this.mockAware.setBundleContext(this.mockContext);
		replay(mockAware);
		//boolean ret = bcaProcessor.postProcessAfterInstantiation(this.mockAware, "aName");
		verify(mockAware);
		//assertTrue("should return true",ret);
	}
	
	@Test
	public void testAfterInitialization() {
		Object bean = new Object();
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(this.mockContext);
		Object ret = bcaProcessor.postProcessAfterInitialization(bean, "aName");
		assertSame("should return the same bean instance",bean,ret);
	}
}
