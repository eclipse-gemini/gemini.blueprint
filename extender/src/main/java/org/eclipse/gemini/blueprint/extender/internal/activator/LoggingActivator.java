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

package org.eclipse.gemini.blueprint.extender.internal.activator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * Simply writes status convenient status information to the log.
 *
 * @author Olaf Otto
 */
public class LoggingActivator implements BundleActivator {
    private final Log log = LogFactory.getLog(getClass());

    public void start(BundleContext extenderBundleContext) throws Exception {
        Version extenderVersion = OsgiBundleUtils.getBundleVersion(extenderBundleContext.getBundle());
        log.info("Starting [" + extenderBundleContext.getBundle().getSymbolicName() + "] bundle v.[" + extenderVersion + "]");
    }

    public void stop(BundleContext extenderBundleContext) throws Exception {
        Version extenderVersion = OsgiBundleUtils.getBundleVersion(extenderBundleContext.getBundle());
        log.info("Stopping [" + extenderBundleContext.getBundle().getSymbolicName() + "] bundle v.[" + extenderVersion + "]");
    }
}
