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
import org.eclipse.gemini.blueprint.extender.internal.activator.listeners.BaseListener;
import org.eclipse.gemini.blueprint.extender.internal.activator.listeners.NamespaceBundleLister;
import org.eclipse.gemini.blueprint.extender.internal.support.NamespaceManager;
import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * @author Bill Gallagher
 * @author Andy Piper
 * @author Hal Hildebrand
 * @author Adrian Colyer
 * @author Costin Leau
 * @author Olaf Otto
 */
public class NamespaceHandlerActivator implements BundleActivator {
    /**
     * Monitor used for dealing with the bundle activator and synchronous bundle threads
     */
    private final Object monitor = new Object();
    private boolean stopped = false;
    private final Log log = LogFactory.getLog(getClass());
    private NamespaceManager nsManager;
    private BaseListener nsListener;
    private long bundleId;
    private BundleContext extenderBundleContext;
    private DefaultVersionMatcher versionMatcher;

    public void start(BundleContext extenderBundleContext) {
        this.extenderBundleContext = extenderBundleContext;
        this.nsManager = new NamespaceManager(extenderBundleContext);
        this.bundleId = extenderBundleContext.getBundle().getBundleId();
        Version extenderVersion = OsgiBundleUtils.getBundleVersion(extenderBundleContext.getBundle());
        this.versionMatcher = new DefaultVersionMatcher(getManagedBundleExtenderVersionHeader(), extenderVersion);

        initNamespaceHandlers(extenderBundleContext);
    }

    public void stop(BundleContext context) throws Exception {
        synchronized (monitor) {
            if (stopped) {
                return;
            }
            stopped = true;
        }

        this.nsListener.close();
        this.extenderBundleContext.removeBundleListener(this.nsListener);
        this.nsListener = null;
        this.nsManager.destroy();
    }

    protected String getManagedBundleExtenderVersionHeader() {
        return ConfigUtils.EXTENDER_VERSION;
    }

    protected void initNamespaceHandlers(BundleContext extenderBundleContext) {
        nsManager = new NamespaceManager(extenderBundleContext);

        // register listener first to make sure any bundles in INSTALLED state
        // are not lost

        // if the property is defined and true, consider bundles in STARTED/LAZY-INIT state, otherwise use RESOLVED
        boolean nsResolved = !Boolean.getBoolean("org.eclipse.gemini.blueprint.ns.bundles.started");
        nsListener = new NamespaceBundleLister(nsResolved, this);
        extenderBundleContext.addBundleListener(nsListener);

        Bundle[] previousBundles = extenderBundleContext.getBundles();

        for (Bundle bundle : previousBundles) {
            // special handling for uber bundle being restarted
            if ((nsResolved && OsgiBundleUtils.isBundleResolved(bundle)) || (!nsResolved && OsgiBundleUtils.isBundleActive(bundle)) || bundleId == bundle.getBundleId()) {
                maybeAddNamespaceHandlerFor(bundle, false);
            } else if (OsgiBundleUtils.isBundleLazyActivated(bundle)) {
                maybeAddNamespaceHandlerFor(bundle, true);
            }
        }

        // discovery finished, publish the resolvers/parsers in the OSGi space
        nsManager.afterPropertiesSet();
    }


    public void maybeAddNamespaceHandlerFor(Bundle bundle, boolean isLazy) {
        if (handlerBundleMatchesExtenderVersion(bundle)) {
            nsManager.maybeAddNamespaceHandlerFor(bundle, isLazy);
        }
    }

    public void maybeRemoveNameSpaceHandlerFor(Bundle bundle) {
        if (handlerBundleMatchesExtenderVersion(bundle))
            nsManager.maybeRemoveNameSpaceHandlerFor(bundle);
    }

    /**
     * Utility method that does extender range version check and appropriate logging.
     */
    protected boolean handlerBundleMatchesExtenderVersion(Bundle bundle) {
        if (!versionMatcher.matchVersion(bundle)) {
            if (log.isDebugEnabled())
                log.debug("Ignoring handler bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle)
                        + "] due to mismatch in expected extender version");
            return false;
        }
        return true;
    }
}
