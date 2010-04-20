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

package org.eclipse.gemini.blueprint.test.internal.util.jar.storage;

import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.Resource;

/**
 * Simple interface for storing information, which allows reading and writing.
 * 
 * @author Costin Leau
 * 
 */
public interface Storage {

	InputStream getInputStream();

	OutputStream getOutputStream();
	
	Resource getResource();
	
	void dispose();
}
