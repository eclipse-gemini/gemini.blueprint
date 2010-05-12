/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.iandt.lifecycle;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.eclipse.gemini.blueprint.context.BundleContextAware;

/**
 * @author Hal Hildebrand
 *         Date: Oct 15, 2006
 *         Time: 5:23:16 PM
 */
public class GuineaPig implements InitializingBean, DisposableBean, BundleContextAware {
    BundleContext bundleContext;
    Listener listener;


    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }


    public void afterPropertiesSet() throws Exception {
        System.setProperty("org.eclipse.gemini.blueprint.iandt.lifecycle.GuineaPig.startUp", "true");
        listener = new Listener();
        bundleContext.addFrameworkListener(listener);
    }


    public void destroy() throws Exception {
        bundleContext.removeFrameworkListener(listener);
        System.setProperty("org.eclipse.gemini.blueprint.iandt.lifecycle.GuineaPig.close", "true");
    }


    static class Listener implements FrameworkListener {
        public void frameworkEvent(FrameworkEvent frameworkEvent) {
            System.out.println("Eavesdropping on " + frameworkEvent);
        }
    }
}
