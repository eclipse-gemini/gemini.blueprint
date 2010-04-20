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

package org.eclipse.gemini.blueprint.bundle;


/**
 * Enum class for the {@link org.osgi.framework.Bundle} actions supported by {@link BundleFactoryBean}.
 * 
 * @author Costin Leau
 */
public enum BundleActionEnum {

	/**
	 * Installs the bundle. This action is implied by {@link #START} and {@link #UPDATE} in case no bundle is found in
	 * the existing OSGi BundleContext.
	 * 
	 * @see org.osgi.framework.BundleContext#installBundle(String)
	 */
	INSTALL,

	/**
	 * Starts the bundle. If no bundle is found, it will try first to install one based on the existing configuration.
	 * 
	 * @see org.osgi.framework.Bundle#start()
	 */
	START,

	/**
	 * Updates the bundle. If no bundle is found, it will try first to install one based on the existing configuration.
	 * 
	 * @see org.osgi.framework.Bundle#update()
	 */
	UPDATE,

	/**
	 * Stops the bundle. If no bundle is found, this action does nothing (it will trigger loading).
	 * 
	 * @see org.osgi.framework.Bundle#stop()
	 */
	STOP,

	/**
	 * Uninstalls the bundle. If no bundle is found, this action does nothing (it will trigger loading).
	 * 
	 * @see org.osgi.framework.Bundle#uninstall()
	 */
	UNINSTALL;
}
