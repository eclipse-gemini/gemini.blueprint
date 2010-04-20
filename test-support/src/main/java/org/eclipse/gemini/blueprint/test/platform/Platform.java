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

package org.eclipse.gemini.blueprint.test.platform;

import org.osgi.framework.BundleContext;

/**
 * Internal interface used for starting different versions of the same platform (such as Felix 1.0.x vs 1.2.x vs 2.x).
 * 
 * @author Costin Leau
 */
interface Platform {

	BundleContext start() throws Exception;

	void stop() throws Exception;
}
