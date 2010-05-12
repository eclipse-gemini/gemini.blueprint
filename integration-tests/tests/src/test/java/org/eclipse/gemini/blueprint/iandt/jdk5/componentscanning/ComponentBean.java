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

package org.eclipse.gemini.blueprint.iandt.jdk5.componentscanning;

import java.awt.Shape;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Just a Spring component that relies on Spring annotations for injection.
 * 
 * @author Costin Leau
 */
@Component
public class ComponentBean {

	private Shape constructorInjection;
	@Autowired
	private Shape fieldInjection;

	private Shape setterInjection;

	@Autowired
	public ComponentBean(Shape Shape) {
		this.constructorInjection = Shape;
	}

	public ComponentBean() {
		// this.constructorShape = Shape;
	}

	/**
	 * Returns the constructorInjection.
	 * 
	 * @return Returns the constructorInjection
	 */
	public Shape getConstructorInjection() {
		return constructorInjection;
	}

	/**
	 * Returns the fieldInjection.
	 * 
	 * @return Returns the fieldInjection
	 */
	public Shape getFieldInjection() {
		return fieldInjection;
	}

	/**
	 * Returns the setterInjection.
	 * 
	 * @return Returns the setterInjection
	 */
	public Shape getSetterInjection() {
		return setterInjection;
	}

	/**
	 * @param setterInjection The setterInjection to set.
	 */
	@Autowired
	public void setSetterInjection(Shape injection) {
		this.setterInjection = injection;
	}
}