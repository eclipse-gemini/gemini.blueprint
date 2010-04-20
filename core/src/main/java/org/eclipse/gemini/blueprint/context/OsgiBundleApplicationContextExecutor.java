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

package org.eclipse.gemini.blueprint.context;

import org.springframework.beans.BeansException;

/**
 * {@link DelegatedExecutionOsgiBundleApplicationContext} executor. Decides how
 * and when the application context will be refreshed/closed.
 * 
 * @author Costin Leau
 * @see DelegatedExecutionOsgiBundleApplicationContext
 */
public interface OsgiBundleApplicationContextExecutor {

	/**
	 * Execute the delegated
	 * {@link org.springframework.context.ConfigurableApplicationContext#refresh()}.
	 * 
	 * @throws BeansException
	 * @throws IllegalStateException
	 */
	void refresh() throws BeansException, IllegalStateException;

	/**
	 * Execute the delegated
	 * {@link org.springframework.context.ConfigurableApplicationContext#close()}.
	 */
	void close();
}
