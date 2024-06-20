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

package org.eclipse.gemini.blueprint.iandt.simpleservice.impl;

import org.eclipse.gemini.blueprint.iandt.simpleservice.MyService;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Hal Hildebrand
 *         Date: Dec 1, 2006
 *         Time: 3:06:01 PM
 */
public class MyServiceImpl implements MyService, InitializingBean {
    public void afterPropertiesSet() throws Exception {
        Integer delay = Integer.getInteger("org.eclipse.gemini.blueprint.iandt.simpleservice.impl.delay", Integer.valueOf(0));
        System.getProperties().remove("org.eclipse.gemini.blueprint.iandt.simpleservice.impl.delay");
        System.out.println("Delaying for:" + delay);
        Thread.sleep(delay.intValue());
    }


    public String stringValue() {
		return "Bond.  James Bond.";
	}

}
