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

package org.eclipse.gemini.blueprint.test.parsing.packageA;

import java.io.File;

import javax.imageio.ImageIO;

import org.eclipse.gemini.blueprint.test.parsing.CaseWithVisibleMethodsBaseTest;

/**
 * Simple base class from a different class that will be included in the
 * archive.
 * 
 * @author Costin Leau
 * 
 */
public abstract class BaseClassFromAnotherPackageTest extends CaseWithVisibleMethodsBaseTest {

	// strange import that doesn't do anything
	private static File file = ImageIO.getCacheDirectory();

}
