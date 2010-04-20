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

package org.eclipse.gemini.blueprint.blueprint.container;

import java.awt.Point;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Costin Leau
 */
public class GenerifiedBean {

	private GenericHolder holder;

	public GenericHolder getHolder() {
		return holder;
	}

	public void setStringHolder(GenericHolder<String> stringHolder) {
		this.holder = stringHolder;
	}

	public void setBooleanHolder(GenericHolder<Boolean> booleanHolder) {
		this.holder = booleanHolder;
	}

	public void setPointMap(TreeMap<String, Point> pointMap) {
		System.out.println("created " + pointMap);
	}
	
	public void setConcurrentMap(ConcurrentMap map) {
		System.out.println("created " + map.getClass());
	}
	
}
