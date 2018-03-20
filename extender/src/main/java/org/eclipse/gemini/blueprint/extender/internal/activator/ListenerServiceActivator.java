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
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEventMulticaster;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Initializes the event multicaster infrastructure.
 *
 * @author Bill Gallagher
 * @author Andy Piper
 * @author Hal Hildebrand
 * @author Adrian Colyer
 * @author Costin Leau
 * @author Olaf Otto
 */
public class ListenerServiceActivator implements BundleActivator {
    /**
     * Monitor used for dealing with the bundle activator and synchronous bundle threads
     */
    private final Object monitor = new Object();
    private boolean stopped = false;
    private OsgiBundleApplicationContextEventMulticaster multicaster;
    private volatile ListListenerAdapter osgiListeners;
    private final Log log = LogFactory.getLog(getClass());
    private final ExtenderConfiguration extenderConfiguration;
    private BundleContext extenderBundleContext;

    public ListenerServiceActivator(ExtenderConfiguration extenderConfiguration) {
        this.extenderConfiguration = extenderConfiguration;
    }

    public void start(BundleContext extenderBundleContext) {
        this.extenderBundleContext = extenderBundleContext;
        initListenerService();
    }

    public void stop(BundleContext extenderBundleContext) {
        synchronized (monitor) {
            if (stopped) {
                return;
            }
            stopped = true;
        }

        // release multicaster
        if (multicaster != null) {
            multicaster.removeAllListeners();
            multicaster = null;
        }
        // release listeners
        osgiListeners.destroy();
        osgiListeners = null;
    }

    protected void initListenerService() {
        this.multicaster = extenderConfiguration.getEventMulticaster();

        addApplicationListener(multicaster);
        multicaster.addApplicationListener(extenderConfiguration.getContextEventListener());

        if (log.isDebugEnabled())
            log.debug("Initialization of OSGi listeners service completed...");
    }

    protected void addApplicationListener(OsgiBundleApplicationContextEventMulticaster multicaster) {
        osgiListeners = new ListListenerAdapter(this.extenderBundleContext);
        osgiListeners.afterPropertiesSet();
        // register the listener that does the dispatching
        multicaster.addApplicationListener(osgiListeners);
    }

    public OsgiBundleApplicationContextEventMulticaster getMulticaster() {
        return multicaster;
    }
}
