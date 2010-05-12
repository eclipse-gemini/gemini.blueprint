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

package org.springframework.osgi.iandt.configuration;

import java.util.*;

/**
 * @author Hal Hildebrand
 *         Date: Jun 14, 2007
 *         Time: 5:30:05 PM
 */
public class ManagedServiceListener {
    public final static List updates = new ArrayList();

    public static final String SERVICE_FACTORY_PID = "test.service.pid";


    public void updateService(Dictionary properties) {
        if (properties.isEmpty()) {
            return;
        }
        Dictionary copy = new Hashtable();
        for (Enumeration keys = properties.keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            copy.put(key, properties.get(key));
        }
        updates.add(copy);
    }


    public void updateServiceMap(Map properties) {
        if (properties.isEmpty()) {
            return;
        }
        Dictionary copy = new Hashtable();
        for (Iterator keys = properties.keySet().iterator(); keys.hasNext();) {
            Object key = keys.next();
            copy.put(key, properties.get(key));
        }
        updates.add(copy);
    }
}
