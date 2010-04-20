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

package org.eclipse.gemini.blueprint.context.support;

import org.springframework.beans.factory.BeanClassLoaderAware;

/**
 * Default implementation of {@link ContextClassLoaderProvider} interface.
 * 
 * It returns the given application context class loader if it is set, falling
 * back to the current thread context class loader otherwise (in effect, leaving
 * the TCCL as it is).
 * 
 * @author Costin Leau
 */
public class DefaultContextClassLoaderProvider implements ContextClassLoaderProvider, BeanClassLoaderAware {

	private ClassLoader beanClassLoader;


	public ClassLoader getContextClassLoader() {
		return (beanClassLoader != null ? beanClassLoader : Thread.currentThread().getContextClassLoader());
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}
}
