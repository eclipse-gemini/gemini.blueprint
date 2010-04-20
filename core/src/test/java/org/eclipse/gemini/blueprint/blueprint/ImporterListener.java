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

import org.osgi.framework.ServiceReference;

/**
 * @author Costin Leau
 */
public class ImporterListener {

	public static final List bind = new ArrayList();
	public static final List unbind = new ArrayList();


	public void bind(ServiceReference ref) {
		bind.add(ref);
	}

	public void unbind(ServiceReference ref) {
		unbind.add(ref);
	}

	public void refBind(ServiceReference ref) {
		bind.add(ref);
	}

	public void refUnbind(ServiceReference ref) {
		unbind.add(ref);
	}

	public void bindM(ServiceReference ref) {
		bind.add(ref);
	}

	public void unbindM(ServiceReference ref) {
		unbind.add(ref);
	}

	public void up(ServiceReference ref) {
		bind.add(ref);
	}

	public void down(ServiceReference ref) {
		unbind.add(ref);
	}
}