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

package org.eclipse.gemini.blueprint.iandt.clogging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.BundleContext;
import org.springframework.util.CollectionUtils;

/**
 * Integration test for commons logging 1.0.4 and its broken logging discovery.
 * 
 * @author Costin Leau
 * 
 */
public abstract class CommonsLogging104Test extends BaseIntegrationTest {

	/** logger */
	private static final Log log = LogFactory.getLog(CommonsLogging104Test.class);


	protected String[] getTestFrameworkBundlesNames() {
		String[] bundles = super.getTestFrameworkBundlesNames();

		// remove slf4j
		Collection bnds = new ArrayList(bundles.length);
		CollectionUtils.mergeArrayIntoCollection(bundles, bnds);

		for (Iterator iterator = bnds.iterator(); iterator.hasNext();) {
			String object = (String) iterator.next();
			// remove slf4j
			if (object.startsWith("org.slf4j"))
				iterator.remove();
		}
		// add commons logging
		bnds.add("org.eclipse.bundles,commons-logging,20070611");

		return (String[]) bnds.toArray(new String[bnds.size()]);
	}

	public void testSimpleLoggingStatement() throws Exception {
		log.info("logging statement");
	}

	protected void preProcessBundleContext(BundleContext platformBundleContext) throws Exception {

		// all below fail
		LogFactory.releaseAll();
		//System.setProperty("org.apache.commons.logging.LogFactory", "org.apache.commons.logging.impl.NoOpLog");
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		//		System.out.println("TCCL is " + cl);
		Thread.currentThread().setContextClassLoader(null);
		super.preProcessBundleContext(platformBundleContext);
	}

}
