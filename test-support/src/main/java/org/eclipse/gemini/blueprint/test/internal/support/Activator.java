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

package org.eclipse.gemini.blueprint.test.internal.support;

import org.eclipse.gemini.blueprint.test.internal.holder.HolderLoader;
import org.junit.runner.Runner;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Default activator for Spring/OSGi test support. This class can be seen as the
 * 'server-side' of the framework, which register the OsgiJUnitTest executor.
 * 
 * @author Costin Leau
 * 
 */
public class Activator implements BundleActivator {

	private ServiceRegistration<Runner> registration;


	public void start(BundleContext context) throws Exception {
		registration = context.registerService(Runner.class, new OsgiJUnitService(context.getBundle().getClass()), null);

		// add also the bundle id so that AbstractOsgiTest can determine its BundleContext when used in an environment
		// where the system bundle is treated as a special case.
		HolderLoader.INSTANCE.getHolder().setTestBundleId(context.getBundle().getBundleId());
	}

	public void stop(BundleContext context) throws Exception {
		// unregister the service even though the framework should do this automatically
		if (registration != null) {
			registration.unregister();
        }
	}

}
