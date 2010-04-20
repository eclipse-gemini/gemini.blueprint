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
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ResourceLoader;

/**
 * Strategy interface for plugging various thread context class loaders during
 * an OSGi application context critical life cycle events.
 * 
 * @see Thread#getContextClassLoader()
 * @see AbstractDelegatedExecutionApplicationContext
 * @see ResourceLoader#getClassLoader()
 * @see BeanClassLoaderAware
 * @see ApplicationContextAware
 * 
 * @author Costin Leau
 */
public interface ContextClassLoaderProvider {

	/**
	 * Returns the context class loader to be used by the OSGi application
	 * context during its life cycle events.
	 * 
	 * @return class loader used as a thread context class loader
	 */
	ClassLoader getContextClassLoader();
}
