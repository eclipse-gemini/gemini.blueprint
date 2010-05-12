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

package org.eclipse.gemini.blueprint.iandt.importer;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author Costin Leau
 */
public class Listener {
	final Map<Object, Map> bind = new LinkedHashMap<Object, Map>();
	final Map<Object, Map> unbind = new LinkedHashMap<Object, Map>();

	public void bind(Object service, Map properties) {
		System.out.println("Binding service hash " + System.identityHashCode(service) + " w/ props " + properties);
		bind.put(service, properties);
	}
	
	public void bind(Date service, Map properties) {
		System.out.println("Binding service hash " + System.identityHashCode(service) + " w/ props " + properties);
		bind.put(service, properties);
	}

	public void unbind(Object service, Map properties) {
		System.out.println("Unbinding service hash " + System.identityHashCode(service) + " w/ props " + properties);
		unbind.put(service, properties);
	}
}
