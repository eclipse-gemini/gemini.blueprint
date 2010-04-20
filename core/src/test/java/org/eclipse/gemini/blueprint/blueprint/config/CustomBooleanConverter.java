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

package org.eclipse.gemini.blueprint.blueprint.config;

import org.osgi.service.blueprint.container.Converter;
import org.osgi.service.blueprint.container.ReifiedType;

/**
 * @author Costin Leau
 */
public class CustomBooleanConverter implements Converter {

	public Object convert(Object source, ReifiedType toType) throws Exception {
		if (source instanceof String && toType.getRawClass() == Boolean.class) {
			String strValue = (String) source;
			if (strValue.equals("T")) {
				return new Boolean(true);
			} else if (strValue.equals("F")) {
				return new Boolean(false);
			}
			// this make the module context only support converting T/F to Boolean?
		}

		// we're supposed to throw an exception if we can't convert
		throw new Exception("Unconvertable object type");
	}

	public boolean canConvert(Object value, ReifiedType toType) {
		return toType.getRawClass() == Boolean.class && value instanceof String;
	}
}
