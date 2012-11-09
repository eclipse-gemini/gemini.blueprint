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

import org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.CachedIntrospectionResults;

/**
 * Used to prevent ad-hoc Java Bean discovery on lazy bundles.
 *
 * @author Bill Gallagher
 * @author Andy Piper
 * @author Hal Hildebrand
 * @author Adrian Colyer
 * @author Costin Leau
 * @author Olaf Otto
 */
public class JavaBeansCacheActivator implements BundleActivator {
    /**
     * Monitor used for dealing with the bundle activator and synchronous bundle threads
     */
    private final Object monitor = new Object();
    private boolean stopped = false;

    public void start(BundleContext extenderBundleContext) {
        initJavaBeansCache();
    }

    public void stop(BundleContext extenderBundleContext) {
        synchronized (monitor) {
            if (stopped) {
                return;
            }
            stopped = true;
        }
        destroyJavaBeansCache();
    }

    private void initJavaBeansCache() {
        Class<?>[] classes =
                new Class<?>[] { OsgiServiceFactoryBean.class, OsgiServiceProxyFactoryBean.class,
                        OsgiServiceCollectionProxyFactoryBean.class };

        CachedIntrospectionResults.acceptClassLoader(OsgiStringUtils.class.getClassLoader());

        for (Class<?> clazz : classes) {
            BeanUtils.getPropertyDescriptors(clazz);
        }
    }

    private void destroyJavaBeansCache() {
        CachedIntrospectionResults.clearClassLoader(OsgiStringUtils.class.getClassLoader());
    }
}


