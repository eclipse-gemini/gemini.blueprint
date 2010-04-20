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

package org.eclipse.gemini.blueprint.service.exporter.support.internal.controller;

import java.lang.reflect.Field;

/**
 * Utility class that retrieves the controller associated with a given importer.
 * 
 * @author Costin Leau
 * 
 */
public abstract class ExporterControllerUtils {

	private static final String FIELD_NAME = "controller";
	private static final Field field;

	static {
		String className = "org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean";
		try {
			Class<?> cls = ExporterControllerUtils.class.getClassLoader().loadClass(className);
			field = cls.getDeclaredField(FIELD_NAME);
			field.setAccessible(true);
		} catch (Exception ex) {
			throw (RuntimeException) new IllegalStateException("Cannot read field [" + FIELD_NAME + "] on class ["
					+ className + "]").initCause(ex);
		}
	}

	public static ExporterInternalActions getControllerFor(Object exporter) {
		try {
			return (ExporterInternalActions) field.get(exporter);
		} catch (IllegalAccessException iae) {
			throw (RuntimeException) new IllegalArgumentException("Cannot access field [" + FIELD_NAME
					+ "] on object [" + exporter + "]").initCause(iae);
		}
	}
}
