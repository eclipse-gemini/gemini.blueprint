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

package org.eclipse.gemini.blueprint.test.internal.util.jar;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Utility class for creating Manifest objects on various criterions.
 * 
 * @author Costin Leau
 * 
 */
public abstract class ManifestUtils {

	/**
	 * Determine the Import-Package value based on the Export-Package entries in
	 * the jars given as Resources.
	 * @param resources
	 * @return
	 */
	public static String[] determineImportPackages(Resource[] resources) {
		Set collection = new LinkedHashSet();
		// for each resource
		for (int i = 0; i < resources.length; i++) {
			Resource resource = resources[i];
			Manifest man = JarUtils.getManifest(resource);
			if (man != null) {
				// read the manifest
				// get the Export-Package
				Attributes attrs = man.getMainAttributes();
				String exportedPackages = attrs.getValue(Constants.EXPORT_PACKAGE);
				// add it to the StringBuilder
				if (StringUtils.hasText(exportedPackages)) {
					collection.addAll(StringUtils.commaDelimitedListToSet(exportedPackages));
				}
			}
		}
		// return the result as string
		String[] array = (String[]) collection.toArray(new String[collection.size()]);

		// clean whitespace just in case
		for (int i = 0; i < array.length; i++) {
			array[i] = StringUtils.trimWhitespace(array[i]);
		}
		return array;
	}
}
