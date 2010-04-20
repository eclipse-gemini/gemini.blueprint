/******************************************************************************
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   Costin Leau - VMware Inc.
 *****************************************************************************/
package org.eclipse.gemini.blueprint.blueprint;

/**
 * @author Costin Leau
 */
public class CustomTypeComponent {

	private final String name;
	private final CustomType customType;

	public CustomTypeComponent(String name, CustomType customType) {
		this.name = name;
		this.customType = customType;
	}

	public String getName() {
		return name;
	}

	public CustomType getCustomType() {
		return customType;
	}
}
