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

import org.eclipse.gemini.blueprint.test.internal.support.OsgiJUnitService;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
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
	private BundleContext context;
	private ServiceReference<Runner> reference;
	private ServiceRegistration<JUnitTestActivator> registration;
	private OsgiJUnitService service;

	public void start(BundleContext bc) throws Exception {
		this.context = bc;

		reference = context.getServiceReference(Runner.class);
		if (reference == null) {
			throw new IllegalArgumentException("cannot find service at " + OsgiJUnitService.class.getName());
		}
		service = (OsgiJUnitService)context.getService(reference);
		service.setBundleContext(bc);
		registration = context.registerService(JUnitTestActivator.class, this, new Hashtable<String, Object>());
	}

	/**
	 * Starts executing an instance of OSGiJUnitTest on the Runner.
	 */
	void executeTest() {
		service.run(new RunNotifier());
	}

	public void stop(BundleContext bc) throws Exception {
		OsgiServiceUtils.unregisterService(registration);
		reference = null;
	}

}
