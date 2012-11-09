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

import org.eclipse.gemini.blueprint.extender.internal.activator.NamespaceHandlerActivator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;

/**
 * Bundle listener used for detecting namespace handler/resolvers. Exists as a separate listener so that it can be
 * registered early to avoid race conditions with bundles in INSTALLING state but still to avoid premature context
 * creation before the Spring {@link org.eclipse.gemini.blueprint.extender.internal.activator.ContextLoaderListener} is not fully initialized.
 *
 * @author Costin Leau
 */
public class NamespaceBundleLister extends BaseListener {

    private final boolean resolved;
    private final NamespaceHandlerActivator namespaceHandlerActivator;

    public NamespaceBundleLister(boolean resolvedBundles, NamespaceHandlerActivator namespaceHandlerActivator) {
        this.resolved = resolvedBundles;
        this.namespaceHandlerActivator = namespaceHandlerActivator;
    }

    protected void handleEvent(BundleEvent event) {
        Bundle bundle = event.getBundle();

        switch (event.getType()) {

            case BundleEvent.RESOLVED:
                if (resolved) {
                    this.namespaceHandlerActivator.maybeAddNamespaceHandlerFor(bundle, false);
                }
                break;

            case LAZY_ACTIVATION_EVENT_TYPE: {
                if (!resolved) {
                    push(bundle);
                    this.namespaceHandlerActivator.maybeAddNamespaceHandlerFor(bundle, true);
                }
                break;
            }
            case BundleEvent.STARTED: {
                if (!resolved) {
                    if (!pop(bundle)) {
                        this.namespaceHandlerActivator.maybeAddNamespaceHandlerFor(bundle, false);
                    }
                }
                break;
            }
            case BundleEvent.STOPPED: {
                pop(bundle);
                this.namespaceHandlerActivator.maybeRemoveNameSpaceHandlerFor(bundle);
                break;
            }
            default:
                break;
        }
    }
}
