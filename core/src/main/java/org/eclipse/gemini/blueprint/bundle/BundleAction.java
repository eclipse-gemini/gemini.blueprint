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

import org.springframework.core.enums.StaticLabeledEnum;
import org.springframework.core.enums.StaticLabeledEnumResolver;

/**
 * Enum-like class for the {@link org.osgi.framework.Bundle} actions supported by {@link BundleFactoryBean}.
 * 
 * @author Costin Leau
 * @depreated As of Spring DM 2.0, replaced by {@link BundleActionEnum}
 */
public class BundleAction extends StaticLabeledEnum {

	private static final long serialVersionUID = 3723986124669884703L;

	/**
	 * Install bundle. This action is implied by {@link #START} and {@link #UPDATE} in case no bundle is found in the
	 * existing OSGi BundleContext.
	 * 
	 * @see org.osgi.framework.BundleContext#installBundle(String)
	 */
	public static final BundleAction INSTALL = new BundleAction(1, "install");

	/**
	 * Start bundle. If no bundle is found, it will try first to install one based on the existing configuration.
	 * 
	 * @see org.osgi.framework.Bundle#start()
	 */
	public static final BundleAction START = new BundleAction(2, "start");

	/**
	 * Update bundle. If no bundle is found, it will try first to install one based on the existing configuration.
	 * 
	 * @see org.osgi.framework.Bundle#update()
	 */
	public static final BundleAction UPDATE = new BundleAction(3, "update");

	/**
	 * Stop bundle. If no bundle is found, this action does nothing (it will trigger loading).
	 * 
	 * @see org.osgi.framework.Bundle#stop()
	 */
	public static final BundleAction STOP = new BundleAction(4, "stop");

	/**
	 * Uninstall bundle. If no bundle is found, this action does nothing (it will trigger loading).
	 * 
	 * @see org.osgi.framework.Bundle#uninstall()
	 */
	public static final BundleAction UNINSTALL = new BundleAction(5, "uninstall");

	/**
	 * Constructs a new <code>BundleAction</code> instance.
	 * 
	 * @param code
	 * @param label
	 */
	private BundleAction(int code, String label) {
		super(code, label);
	}

	BundleActionEnum getBundleActionEnum() {
		return BundleActionEnum.valueOf(this.getLabel().toUpperCase());
	}

	static BundleAction getBundleAction(BundleActionEnum enm) {
		return (BundleAction) StaticLabeledEnumResolver.instance().getLabeledEnumByLabel(BundleAction.class,
				enm.name().toLowerCase());
	}
}