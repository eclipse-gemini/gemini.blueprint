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

/**
 * @author Andy Piper
 */
public class MyServiceImpl implements MyService {
    // The counter can be used to check that the class has been freshly loaded, rather than
    // reused by the system
    private static int counter = 1;
    public String stringValue() {
		return "Dalton.  Timothy Dalton #" + counter++;
	}

}
