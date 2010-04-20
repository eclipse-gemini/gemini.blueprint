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

package org.eclipse.gemini.blueprint.io.internal.resolver;

import org.osgi.framework.Bundle;

/**
 * Importing bundle information.
 * 
 * @author Costin Leau
 * 
 */
public class ImportedBundle {

	private final Bundle importingBundle;

	private final String[] importedPackages;


	public ImportedBundle(Bundle importingBundle, String[] importedPackages) {
		super();
		this.importingBundle = importingBundle;
		this.importedPackages = importedPackages;
	}

	/**
	 * Returns the imported bundle.
	 * 
	 * @return importing bundle
	 */
	public Bundle getBundle() {
		return importingBundle;
	}

	/**
	 * 
	 * Returns an array of imported packages.
	 * 
	 * @return a non-null array of String representing the imported packages
	 */
	public String[] getImportedPackages() {
		return importedPackages;
	}

	public String toString() {
		return importingBundle.toString();
	}
}