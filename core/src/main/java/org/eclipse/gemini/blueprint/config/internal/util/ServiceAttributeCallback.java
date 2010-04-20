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

package org.eclipse.gemini.blueprint.config.internal.util;

import java.util.Locale;

import org.eclipse.gemini.blueprint.service.exporter.support.DefaultInterfaceDetector;
import org.eclipse.gemini.blueprint.service.exporter.support.ExportContextClassLoaderEnum;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * &lt;service&gt; attribute callback.
 * 
 * @author Costin Leau
 */
public class ServiceAttributeCallback implements AttributeCallback {

	private static final char UNDERSCORE_CHAR = '_';
	private static final char DASH_CHAR = '-';
	private static final String AUTOEXPORT = "auto-export";
	private static final String AUTOEXPORT_PROP = "interfaceDetector";
	private static final String INTERFACE = "interface";
	private static final String INTERFACES_PROP = "interfaces";
	private static final String CCL_PROP = "exportContextClassLoader";
	private static final String CONTEXT_CLASSLOADER = "context-class-loader";
	private static final String REF = "ref";

	public boolean process(Element parent, Attr attribute, BeanDefinitionBuilder bldr) {
		String name = attribute.getLocalName();

		if (INTERFACE.equals(name)) {
			bldr.addPropertyValue(INTERFACES_PROP, attribute.getValue());
			return false;
		} else if (REF.equals(name)) {
			return false;
		}

		else if (AUTOEXPORT.equals(name)) {
			// convert constant to upper case to let Spring do the
			// conversion
			String label = attribute.getValue().toUpperCase(Locale.ENGLISH).replace(DASH_CHAR, UNDERSCORE_CHAR);
			bldr.addPropertyValue(AUTOEXPORT_PROP, Enum.valueOf(DefaultInterfaceDetector.class, label));
			return false;
		}

		else if (CONTEXT_CLASSLOADER.equals(name)) {
			// convert constant to upper case to let Spring do the
			// conversion

			String value = attribute.getValue().toUpperCase(Locale.ENGLISH).replace(DASH_CHAR, UNDERSCORE_CHAR);
			bldr.addPropertyValue(CCL_PROP, ExportContextClassLoaderEnum.valueOf(value));
			return false;
		}

		return true;
	}
}