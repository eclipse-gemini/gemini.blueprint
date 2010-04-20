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

package org.eclipse.gemini.blueprint.service.exporter.support;

import org.eclipse.gemini.blueprint.util.internal.ClassUtils;
import org.springframework.core.enums.StaticLabeledEnum;

/**
 * Enum-like class indicatin class exporters available to {@link OsgiServiceFactoryBean} for registering object as OSGi
 * services.
 * 
 * @author Costin Leau
 * @deprecated as of 2.0, replaced by {@link InterfaceDetector}
 */
public abstract class AutoExport extends StaticLabeledEnum implements InterfaceDetector {

	/** Do not export anything */
	public static final AutoExport DISABLED = new AutoExport(0, "DISABLED") {

		private static final long serialVersionUID = -8297270116184239840L;

		private final Class<?>[] clazz = new Class[0];

		Class<?>[] getExportedClasses(Class<?> targetClass) {
			return clazz;
		}
	};

	/**
	 * Export all interfaces (and their hierarchy) implemented by the given class
	 */
	public static final AutoExport INTERFACES = new AutoExport(1, "INTERFACES") {

		private static final long serialVersionUID = -8336152449611885031L;

		public Class<?>[] getExportedClasses(Class<?> targetClass) {
			return ClassUtils.getClassHierarchy(targetClass, ClassUtils.ClassSet.INTERFACES);
		}
	};

	/**
	 * Export the class hierarchy (all classes inherited by the given target excluding Object.class)
	 */
	public static final AutoExport CLASS_HIERARCHY = new AutoExport(2, "CLASS_HIERARCHY") {

		private static final long serialVersionUID = 6464782616822538297L;

		public Class<?>[] getExportedClasses(Class<?> targetClass) {
			return ClassUtils.getClassHierarchy(targetClass, ClassUtils.ClassSet.CLASS_HIERARCHY);

		}
	};

	/**
	 * Export every class, inherited or implemented by the given target. Similar to {@link #CLASS_HIERARCHY} +
	 * {@link #INTERFACES}
	 */
	public static final AutoExport ALL_CLASSES = new AutoExport(3, "ALL_CLASSES") {

		private static final long serialVersionUID = -6628398711158262852L;

		public Class<?>[] getExportedClasses(Class<?> targetClass) {
			return ClassUtils.getClassHierarchy(targetClass, ClassUtils.ClassSet.ALL_CLASSES);
		}
	};

	/**
	 * Determines the exported classes given a certain target class.
	 * 
	 * @param targetClass class to be exported into OSGi
	 * @return array of classes that will be published for the OSGi service.
	 */
	abstract Class<?>[] getExportedClasses(Class<?> targetClass);

	public final Class<?>[] detect(Class<?> targetClass) {
		return getExportedClasses(targetClass);
	}

	/**
	 * Constructs a new <code>AutoExport</code> instance.
	 * 
	 * @param code
	 * @param label
	 */
	private AutoExport(int code, String label) {
		super(code, label);
	}
}