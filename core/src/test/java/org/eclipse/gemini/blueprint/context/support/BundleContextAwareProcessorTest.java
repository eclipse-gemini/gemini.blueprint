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

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.context.support.BundleContextAwareProcessor;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public abstract class BundleContextAwareProcessorTest extends TestCase{

	private MockControl bundleContextControl;
	private MockControl bundleContextAwareControl;
	private BundleContext mockContext;
	private BundleContextAware mockAware;
	
	protected void setUp() throws Exception {
		this.bundleContextControl = MockControl.createControl(BundleContext.class);
		this.mockContext = (BundleContext) this.bundleContextControl.getMock();
		// no tests should ever call the mockContext, we're really
		// using it just as a convenient implementation
		this.bundleContextControl.replay();
		
		this.bundleContextAwareControl = MockControl.createControl(BundleContextAware.class);
		this.mockAware = (BundleContextAware) this.bundleContextAwareControl.getMock();
	}
	
	protected void tearDown() throws Exception {
		this.bundleContextControl.verify();
	}
	
	public void testBeforeInitializationNoBundleContext() {
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(null);
		this.bundleContextAwareControl.replay();
		try {
			//bcaProcessor.postProcessAfterInstantiation(this.mockAware, "aName");
			fail("should throw an IllegalStateException when no BundleContext available");
		} 
		catch(IllegalStateException ex) {
			assertEquals("Cannot satisfy BundleContextAware for bean 'aName' without BundleContext",
					     ex.getMessage());
		}
		this.bundleContextAwareControl.verify();
	}
	
	public void testBeforeInitializationNonImplementer() {
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(this.mockContext);
		Object bean = new Object();
		Object ret = bcaProcessor.postProcessBeforeInitialization(bean, "aName");
		assertSame("should return same bean instance",bean,ret);
	}
	
	public void testBeforeInitializationBundleContextImplementer() {
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(this.mockContext);
		this.mockAware.setBundleContext(this.mockContext);
		this.bundleContextAwareControl.replay();
		//boolean ret = bcaProcessor.postProcessAfterInstantiation(this.mockAware, "aName");
		this.bundleContextAwareControl.verify();
		//assertTrue("should return true",ret);
	}
	
	public void testAfterInitialization() {
		Object bean = new Object();
		BundleContextAwareProcessor bcaProcessor = new BundleContextAwareProcessor(this.mockContext);
		Object ret = bcaProcessor.postProcessAfterInitialization(bean, "aName");
		assertSame("should return the same bean instance",bean,ret);
	}
}
