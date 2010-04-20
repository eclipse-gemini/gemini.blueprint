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

/**
 * Basic interface that maps the OSGi 4.2 framework methods. Useful for avoiding class loading and the use of reflection
 * for API portability.
 * 
 * @author Costin Leau
 */
interface FrameworkTemplate {

	/**
	 * Initialize the framework.
	 */
	void init();

	/**
	 * Start the framework.
	 */
	void start();

	/**
	 * Stop and wait for the framework to stop.
	 * 
	 * @param delay time to wait in milliseconds.
	 */
	void stopAndWait(long delay);
}
