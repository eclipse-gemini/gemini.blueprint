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

package org.eclipse.gemini.blueprint.service.importer.support;

import org.springframework.core.enums.StaticLabeledEnum;
import org.springframework.core.enums.StaticLabeledEnumResolver;

/**
 * Enum-like class containing the OSGi service importer thread context class loader (TCCL) management options.
 * 
 * @author Costin Leau
 * @deprecated As of Spring DM 2.0, replaced by {@link ImportContextClassLoaderEnum}
 */
public class ImportContextClassLoader extends StaticLabeledEnum {

	private static final long serialVersionUID = -7054525261814306077L;

	/**
	 * The TCCL will not be managed upon service invocation.
	 */
	public static final ImportContextClassLoader UNMANAGED = new ImportContextClassLoader(0, "UNMANAGED");

	/**
	 * The TCCL will be set to that of the service provider upon service invocation.
	 */
	public static final ImportContextClassLoader SERVICE_PROVIDER = new ImportContextClassLoader(1, "SERVICE_PROVIDER");

	/**
	 * The TCCL will be set to that of the client upon service invocation.
	 */
	public static final ImportContextClassLoader CLIENT = new ImportContextClassLoader(2, "CLIENT");

	/**
	 * Constructs a new <code>ImportContextClassLoader</code> instance.
	 * 
	 * @param code
	 * @param label
	 */
	private ImportContextClassLoader(int code, String label) {
		super(code, label);
	}

	ImportContextClassLoaderEnum getImportContextClassLoaderEnum() {
		return ImportContextClassLoaderEnum.valueOf(getLabel());
	}

	static ImportContextClassLoader getImportContextClassLoader(ImportContextClassLoaderEnum enm) {
		return (ImportContextClassLoader) StaticLabeledEnumResolver.instance().getLabeledEnumByLabel(
				ImportContextClassLoader.class, enm.name());
	}
}
