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

package org.eclipse.gemini.blueprint.test.parsing;

import java.io.File;
import java.util.Properties;
import java.util.jar.Manifest;

import org.eclipse.gemini.blueprint.test.AbstractConfigurableBundleCreatorTests;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * @author Costin Leau
 * 
 */
public abstract class CaseWithVisibleMethodsBaseTest extends AbstractConfigurableBundleCreatorTests {

	public String getRootPath() {
		ResourceLoader fileLoader = new DefaultResourceLoader();
		try {
			String classFile = CaseWithVisibleMethodsBaseTest.class.getName().replace('.', '/').concat(".class");
			Resource res = fileLoader.getResource(classFile);
			String fileLocation = "file:/" + res.getFile().getAbsolutePath();
			String classFileToPlatform = CaseWithVisibleMethodsBaseTest.class.getName().replace('.', File.separatorChar).concat(
				".class");
			return fileLocation.substring(0, fileLocation.indexOf(classFileToPlatform));
		}
		catch (Exception ex) {
		}

		return null;
	}

	public Manifest getManifest() {
		return super.getManifest();
	}

	public Properties getSettings() throws Exception {
		return super.getSettings();
	}

	public String[] getBundleContentPattern() {
		return super.getBundleContentPattern();
	}

}
