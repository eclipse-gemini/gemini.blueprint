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

package org.eclipse.gemini.blueprint.blueprint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osgi.framework.ServiceRegistration;

/**
 * @author Costin Leau
 */
public class ExporterListener {

	public static final List bind = new ArrayList();
	public static final List unbind = new ArrayList();


	public void up(ServiceRegistration reg, Map serviceProperties) {
		bind.add(reg);
	}

	public void down(ServiceRegistration reg, Map serviceProperties) {
		unbind.add(reg);
	}
}