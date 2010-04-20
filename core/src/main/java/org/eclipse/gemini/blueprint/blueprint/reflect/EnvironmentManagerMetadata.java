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

package org.eclipse.gemini.blueprint.blueprint.reflect;

import java.util.Collections;
import java.util.List;

import org.osgi.service.blueprint.reflect.ComponentMetadata;

/**
 * Dedicated metadata class for environment managers.
 * 
 * @author Costin Leau
 */
class EnvironmentManagerMetadata implements ComponentMetadata {

	private final String id;

	public EnvironmentManagerMetadata(String id) {
		this.id = id;
	}

	public int getActivation() {
		return ComponentMetadata.ACTIVATION_EAGER;
	}

	public List<String> getDependsOn() {
		return Collections.emptyList();
	}

	public String getId() {
		return id;
	}
}
