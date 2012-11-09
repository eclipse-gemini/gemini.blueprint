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

package org.eclipse.gemini.blueprint.extender.internal.activator.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Common base class for {@link org.eclipse.gemini.blueprint.extender.internal.activator.ContextLoaderListener} listeners.
 *
 * @author Costin Leau
 */
public abstract class BaseListener implements SynchronousBundleListener {
    public static final int LAZY_ACTIVATION_EVENT_TYPE = 0x00000200;

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * flag indicating whether the context is down or not - useful during shutdown
     */
    private volatile boolean isClosed = false;

    /**
     * common cache used for tracking down bundles started lazily so they don't get processed twice (once when
     * started lazy, once when started fully)
     */
    protected final Map<Bundle, Object> lazyBundleCache = new WeakHashMap<Bundle, Object>();
    /**
     * dummy value for the bundle cache
     */
    private final Object VALUE = new Object();

    // caches the bundle
    protected void push(Bundle bundle) {
        synchronized (lazyBundleCache) {
            lazyBundleCache.put(bundle, VALUE);
        }
    }

    // checks the presence of the bundle as well as removing it
    protected boolean pop(Bundle bundle) {
        synchronized (lazyBundleCache) {
            return (lazyBundleCache.remove(bundle) != null);
        }
    }

    /**
     * A bundle has been started, stopped, resolved, or unresolved. This method is a synchronous callback, do not do
     * any long-running work in this thread.
     *
     * @see org.osgi.framework.SynchronousBundleListener#bundleChanged
     */
    public void bundleChanged(BundleEvent event) {

        boolean trace = log.isTraceEnabled();

        // check if the listener is still alive
        if (isClosed) {
            if (trace)
                log.trace("Listener is closed; events are being ignored");
            return;
        }
        if (trace) {
            log.trace("Processing bundle event [" + OsgiStringUtils.nullSafeToString(event) + "] for bundle ["
                    + OsgiStringUtils.nullSafeSymbolicName(event.getBundle()) + "]");
        }
        try {
            handleEvent(event);
        } catch (Exception ex) {
            /* log exceptions before swallowing */
            log.warn("Got exception while handling event " + event, ex);
        }
    }

    protected abstract void handleEvent(BundleEvent event);

    public void close() {
        this.isClosed = true;
    }
}
