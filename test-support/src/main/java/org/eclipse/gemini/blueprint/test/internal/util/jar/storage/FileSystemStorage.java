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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * File-system based storage. Uses a temporary file for storing information.
 * 
 * @author Costin Leau
 * 
 */
public class FileSystemStorage implements Storage {

	private File storage;

	private static final String TEMP_FILE_PREFIX = "spring.osgi";

	public FileSystemStorage() {
		try {
			storage = File.createTempFile(TEMP_FILE_PREFIX, null);
		}
		catch (IOException ex) {
			throw new RuntimeException("cannot create temporary file", ex);
		}
		storage.deleteOnExit();
	}

	public InputStream getInputStream() {
		try {
			return new BufferedInputStream(new FileInputStream(storage));
		}
		catch (IOException ex) {
			throw new RuntimeException("cannot return file stream", ex);
		}
	}

	public OutputStream getOutputStream() {
		try {
			return new BufferedOutputStream(new FileOutputStream(storage));
		}
		catch (IOException ex) {
			throw new RuntimeException("cannot return file stream", ex);
		}
	}

	public void dispose() {
		storage.delete();
	}

	public Resource getResource() {
		return new FileSystemResource(storage);
	}
}
