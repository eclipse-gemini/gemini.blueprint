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

package org.eclipse.gemini.blueprint.iandt.reference.proxy;

import org.eclipse.gemini.blueprint.iandt.simpleservice.MyService;

/**
 * @author Hal Hildebrand
 *         Date: Nov 25, 2006
 *         Time: 12:50:20 PM
 */
public class ServiceReferer {
    public static MyService serviceReference;

    public void setReference(MyService reference) {
        serviceReference = reference;
    }
}
