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

import org.springframework.core.enums.StaticLabeledEnum;
import org.springframework.core.enums.StaticLabeledEnumResolver;

/**
 * Enum-like class for the exporter thread context-classLoader (TCCL) management options.
 * 
 * <p/> Used by {@link OsgiServiceFactoryBean} for exported services that depend on certain TCCL to be set.
 * 
 * @author Costin Leau
 * @deprecated As of Spring DM 2.0, replaced by {@link ExportContextClassLoaderEnum}
 */
public class ExportContextClassLoader extends StaticLabeledEnum {

	private static final long serialVersionUID = 4550689727536101071L;

	/**
	 * The TCCL will not be managed upon service invocation.
	 */
	public static final ExportContextClassLoader UNMANAGED = new ExportContextClassLoader(0, "UNMANAGED");

	/**
	 * The TCCL will be set to the service provider upon service invocation.
	 */
	public static final ExportContextClassLoader SERVICE_PROVIDER = new ExportContextClassLoader(1, "SERVICE_PROVIDER");

	/**
	 * Constructs a new <code>ExportContextClassLoader</code> instance.
	 * 
	 * @param code
	 * @param label
	 */
	private ExportContextClassLoader(int code, String label) {
		super(code, label);
	}

	ExportContextClassLoaderEnum getExportContextClassLoaderEnum() {
		return ExportContextClassLoaderEnum.valueOf(getLabel());
	}

	static ExportContextClassLoader getExportContextClassLoader(ExportContextClassLoaderEnum enm) {
		return (ExportContextClassLoader) StaticLabeledEnumResolver.instance().getLabeledEnumByLabel(
				ExportContextClassLoader.class, enm.name());
	}
}
