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

package org.eclipse.gemini.blueprint.iandt.cardinality0to1.test;

import org.eclipse.gemini.blueprint.iandt.simpleservice2.MyService2;

/**
 * @author Hal Hildebrand
 *         Date: Apr 16, 2007
 *         Time: 3:23:40 PM
 */
public class ReferenceContainer {
    public static MyService2 service;


    public void setSimpleService(MyService2 simpleService) {
        service = simpleService;
    }
}
