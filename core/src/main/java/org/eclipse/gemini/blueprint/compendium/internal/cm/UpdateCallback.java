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

package org.eclipse.gemini.blueprint.compendium.internal.cm;

import java.util.Map;

/**
 * Update callback that encapsulates the update-method/strategy. In general, it
 * is expected that the callbacks are thread-safe (and thus
 * stateless/immutable).
 * 
 * @author Costin Leau
 */
interface UpdateCallback {

	/**
	 * Performs an update using the given properties. It's up to each
	 * implementation to decide what other parameters are needed.
	 * 
	 * @param instance the updated instance
	 * @param properties update properties
	 */
	void update(Object instance, Map properties);
}
