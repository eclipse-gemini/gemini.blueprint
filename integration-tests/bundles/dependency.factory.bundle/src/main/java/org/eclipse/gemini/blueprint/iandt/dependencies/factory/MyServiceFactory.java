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

package org.eclipse.gemini.blueprint.iandt.dependencies.factory;

import org.springframework.beans.factory.SmartFactoryBean;
import org.eclipse.gemini.blueprint.iandt.simpleservice.MyService;

/**
 * @author Hal Hildebrand
 *         Date: Aug 27, 2007
 *         Time: 8:41:19 AM
 */
public class MyServiceFactory implements SmartFactoryBean {

    protected MyService service = new MyService() {
        public String stringValue() {
            return "Hello World";
        }
    };
    private static final String DELAY_PROP = "org.eclipse.gemini.blueprint.iandt.dependencies.factory.delay"; 

    public Object getObject() throws Exception {
        Integer delay = Integer.getInteger(DELAY_PROP, new Integer(0));
        System.getProperties().remove(DELAY_PROP);
        System.out.println("Delaying for:" + delay);
        Thread.sleep(delay.intValue());
        return service;
    }


    public Class<?> getObjectType() {
        return MyService.class;
    }


    public boolean isSingleton() {
        return true;
    }


    public boolean isPrototype() {
        return false;
    }


    public boolean isEagerInit() {
        return true;
    }
}
