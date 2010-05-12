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

package org.eclipse.gemini.blueprint.iandt.jdk5.bridgemethods;

import java.awt.Shape;
import java.util.Map;

/**
 * Generified listener causing bridged methods to be created.
 * 
 * @author Costin Leau
 * 
 */
public class Listener implements GenerifiedListenerInterface<Shape> {

	public static int BIND_CALLS = 0;
	public static int UNBIND_CALLS = 0;

	public void bind(Shape service, Map<String, ?> properties) {
		System.out.println("calling bind");
		BIND_CALLS++;
	}

	public void unbind(Shape service, Map<String, ?> properties) {
		System.out.println("calling unbind");
		UNBIND_CALLS++;
	}
}
