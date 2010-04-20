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

package org.eclipse.gemini.blueprint.test.parsing.packageX;

import java.io.File;
import java.net.URI;

import javax.imageio.ImageIO;
import javax.print.URIException;

import org.eclipse.gemini.blueprint.test.parsing.CaseWithVisibleMethodsBaseTest;

/**
 * @author Costin Leau
 * 
 */
public abstract class BaseClassInACertainPackageTest extends CaseWithVisibleMethodsBaseTest implements URIException {

	// strange import that doesn't do anything
	private static File file = ImageIO.getCacheDirectory();


	public int getReason() {
		throw new UnsupportedOperationException();
	}

	public URI getUnsupportedURI() {
		throw new UnsupportedOperationException();
	}
}
