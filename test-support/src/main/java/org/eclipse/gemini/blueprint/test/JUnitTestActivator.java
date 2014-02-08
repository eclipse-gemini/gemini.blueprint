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

package org.eclipse.gemini.blueprint.test;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.test.internal.OsgiJUnitTest;
import org.eclipse.gemini.blueprint.test.internal.TestRunnerService;
import org.eclipse.gemini.blueprint.test.internal.holder.HolderLoader;
import org.eclipse.gemini.blueprint.test.internal.holder.OsgiTestInfoHolder;
import org.eclipse.gemini.blueprint.test.internal.support.OsgiJUnitTestAdapter;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Test bundle activator - looks for a predefined JUnit test runner and triggers
 * the test execution. This class is used by the testing framework to run
 * integration tests inside the OSGi framework.
 * 
 * <strong>Note:</strong> Programmatic usage of this class is strongly
 * discouraged as its semantics might change in the future - in fact, the only
 * reason this class is public is because the OSGi specification requires this.
 * 
 * @author Costin Leau
 */
public class JUnitTestActivator implements BundleActivator {

	private static final Log log = LogFactory.getLog(JUnitTestActivator.class);

	private BundleContext context;
	private ServiceReference<TestRunnerService> reference;
	private ServiceRegistration<JUnitTestActivator> registration;
	private TestRunnerService service;


	public void start(BundleContext bc) throws Exception {
		this.context = bc;

		reference = context.getServiceReference(TestRunnerService.class);
		if (reference == null) {
			throw new IllegalArgumentException("cannot find service at " + TestRunnerService.class.getName());
        }
		service = context.getService(reference);
		registration = context.registerService(JUnitTestActivator.class, this, new Hashtable<String, Object>());
	}

	/**
	 * Starts executing an instance of OSGiJUnitTest on the TestRunnerService.
	 */
	void executeTest() {
		service.runTest(loadTest());
	}

	/**
	 * Loads the test instance inside OSGi and prepares it for execution.
	 * 
	 * @return
	 */
	private OsgiJUnitTest loadTest() {
		OsgiTestInfoHolder holder = HolderLoader.INSTANCE.getHolder();
		String testClass = holder.getTestClassName();
		if (testClass == null)
			throw new IllegalArgumentException("no test class specified");

		try {
			// use bundle to load the classes
			Class<?> clazz = context.getBundle().loadClass(testClass);
			TestCase test = (TestCase) clazz.newInstance();
			// wrap the test with the OsgiJUnitTestAdapter
			OsgiJUnitTest osgiTest = new OsgiJUnitTestAdapter(test);
			osgiTest.injectBundleContext(context);
			return osgiTest;

		}
		catch (Exception ex) {
			log.error("failed to invoke test execution", ex);
			throw new RuntimeException(ex);
		}
	}

	public void stop(BundleContext bc) throws Exception {
		OsgiServiceUtils.unregisterService(registration);
        reference = null;
	}

}
