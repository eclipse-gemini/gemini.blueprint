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

package org.eclipse.gemini.blueprint.test.platform;

import org.apache.commons.logging.Log;
import org.eclipse.gemini.blueprint.util.OsgiPlatformDetector;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.springframework.util.Assert;

/**
 * Utility class useful for starting and stopping an OSGi 4.2 framework.
 * 
 * @author Costin Leau
 */
class DefaultFrameworkTemplate implements FrameworkTemplate {

	private final Framework fwk;
	/** logger */
	private final Log log;

	public DefaultFrameworkTemplate(Object target, Log log) {
		if (OsgiPlatformDetector.isR42()) {
			Assert.isInstanceOf(Framework.class, target);
			fwk = (Framework) target;
		} else {
			throw new IllegalStateException("Cannot use OSGi 4.2 Framework API in an OSGi 4.1 environment");
		}
		this.log = log;
	}

	public void init() {
		try {
			fwk.init();
		} catch (BundleException ex) {
			throw new IllegalStateException("Cannot initialize framework", ex);
		}
	}

	public void start() {
		try {
			fwk.start();
		} catch (BundleException ex) {
			throw new IllegalStateException("Cannot start framework", ex);
		}
	}

	public void stopAndWait(long delay) {
		try {
			fwk.stop();
		} catch (BundleException ex) {
			log.error("Cannot stop framework", ex);
		}

		try {
			fwk.waitForStop(delay);
		} catch (InterruptedException ex) {
			log.error("Waiting for framework to stop interrupted", ex);
		}
	}
}