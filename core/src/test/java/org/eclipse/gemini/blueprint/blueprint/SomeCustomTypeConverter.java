/******************************************************************************
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   Costin Leau - VMware Inc.
 *****************************************************************************/
package org.eclipse.gemini.blueprint.blueprint;

import org.osgi.service.blueprint.container.Converter;
import org.osgi.service.blueprint.container.ReifiedType;

/**
 * @author Costin Leau
 */
public class SomeCustomTypeConverter implements Converter {
	public Object convert(Object source, ReifiedType toType) throws Exception {
		Class toClass = (Class) toType.getRawClass();
		if (source instanceof String
				&& ((CustomType.class.isAssignableFrom(toClass) || SomeCustomType.class.isAssignableFrom(toClass)))) {
			return new SomeCustomType((String) source);
		}
		// we're supposed to throw an exception if we can't convert
		throw new Exception("Unconvertable object type");
	}

	public boolean canConvert(Object value, ReifiedType toType) {
		Class toClass = (Class) toType.getRawClass();
		return (value instanceof String && ((CustomType.class.isAssignableFrom(toClass) || SomeCustomType.class
				.isAssignableFrom(toClass))));
	}
}
