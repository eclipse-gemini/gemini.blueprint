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

package org.eclipse.gemini.blueprint.iandt.referenceProxy;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.service.ServiceUnavailableException;
import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.eclipse.gemini.blueprint.service.importer.support.ImportContextClassLoaderEnum;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Costin Leau
 * 
 */
public class ReferenceInterruptTest extends BaseIntegrationTest {

	@Test
	public void testProxyInterrupt() throws Exception {
		long initialWait = 20 * 1000;
		final OsgiServiceProxyFactoryBean proxyFactory = new OsgiServiceProxyFactoryBean();
		proxyFactory.setBeanClassLoader(getClass().getClassLoader());
		proxyFactory.setBundleContext(bundleContext);
		proxyFactory.setAvailability(Availability.OPTIONAL);
		proxyFactory.setImportContextClassLoader(ImportContextClassLoaderEnum.UNMANAGED);
		proxyFactory.setInterfaces(new Class<?>[] { Shape.class });
		proxyFactory.setTimeout(initialWait);
		proxyFactory.afterPropertiesSet();

		Runnable resetProxy = new Runnable() {

			public void run() {
				try {
					Thread.sleep(3 * 1000);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				proxyFactory.setTimeout(0);
			}
		};

		Object proxy = proxyFactory.getObject();
		assertNotNull(proxy);
		Thread stopThread = new Thread(resetProxy, "reset-proxy-thread");
		stopThread.start();

		long start = System.currentTimeMillis();
		logger.info("Invoking proxy...");
		try {
			proxy.toString();
			fail("no service should have been found...");
		}
		catch (ServiceUnavailableException sue) {
		}

		long stop = System.currentTimeMillis();
		assertTrue(stop - start < initialWait);
	}
}
