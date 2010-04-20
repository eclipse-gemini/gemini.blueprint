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
 * Convenience constants class for OSGi platforms supported out of the box.
 * 
 * @author Costin Leau
 */
public abstract class Platforms {

	/**
	 * <a href="http://www.eclipse.org/equinox">Equinox</a> OSGi platform
	 * constant.
	 */
	public static final String EQUINOX = EquinoxPlatform.class.getName();

	/**
	 * <a href="http://www.knopflerfish.org/">Knopflerfish</a> OSGi platform
	 * constant.
	 */
	public static final String KNOPFLERFISH = KnopflerfishPlatform.class.getName();

	/**
	 * <a href="http://felix.apache.org/">Felix</a> OSGi platform constant.
	 */
	public static final String FELIX = FelixPlatform.class.getName();

}
