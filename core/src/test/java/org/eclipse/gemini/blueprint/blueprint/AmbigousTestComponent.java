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

package org.eclipse.gemini.blueprint.blueprint;

/**
 * @author Costin Leau
 */
public class AmbigousTestComponent {

	private String str1, str2;
	private Object obj;


	public AmbigousTestComponent(String arg1, String arg2, Object arg3) {
		this.str1 = arg1;
		this.str2 = arg2;
		this.obj = arg3;
	}

	public AmbigousTestComponent() {

	}

	public String getStr1() {
		return str1;
	}

	public String getStr2() {
		return str2;
	}

	public Object getObj() {
		return obj;
	}

	public void setAmbigousProp(int prop) {
		System.out.println("int setter called");
	}
	
	public void setAmbigousProp(Object prop) {
		System.out.println("Object setter called");
	}
}
