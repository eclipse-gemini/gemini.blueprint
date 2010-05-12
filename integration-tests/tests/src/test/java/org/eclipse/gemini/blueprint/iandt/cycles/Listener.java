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

package org.eclipse.gemini.blueprint.iandt.cycles;

import java.util.Map;

/**
 * Simple custom listener used for testing cyclic injection.
 * 
 * @author Costin Leau
 */
public abstract class Listener {

	private Object target;

	public void bind(Object service, Map properties) {
	}

	/**
	 * Returns the target.
	 * 
	 * @return Returns the target
	 */
	public Object getTarget() {
		return target;
	}

	/**
	 * @param target The target to set.
	 */
	public void setTarget(Object target) {
		this.target = target;
	}
}
