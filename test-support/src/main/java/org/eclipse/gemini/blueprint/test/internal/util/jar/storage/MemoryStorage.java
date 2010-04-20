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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * Memory based storage. This class writes the information to a byte array.
 * 
 * @author Costin Leau
 * 
 */
public class MemoryStorage implements Storage {

	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	public void dispose() {
		buffer = new ByteArrayOutputStream(0);
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(buffer.toByteArray());
	}

	public OutputStream getOutputStream() {
		return buffer;
	}

	public Resource getResource() {
		return new ByteArrayResource(buffer.toByteArray());
	}
}
