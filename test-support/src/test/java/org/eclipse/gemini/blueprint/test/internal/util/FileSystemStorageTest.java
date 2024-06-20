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

package org.eclipse.gemini.blueprint.test.internal.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.gemini.blueprint.test.internal.util.jar.storage.FileSystemStorage;
import org.eclipse.gemini.blueprint.test.internal.util.jar.storage.Storage;
import org.junit.Test;
import org.springframework.core.io.Resource;

/**
 * @author Costin Leau
 * 
 */
public class FileSystemStorageTest extends AbstractStorageTest {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gemini.blueprint.test.util.StorageGenericTest#createStorage()
	 */
	protected Storage createStorage() {
		return new FileSystemStorage();
	}

	@Test
	public void testResourceForTempFile() throws Exception {
		Resource res = storage.getResource();
		assertTrue(res.exists());
		File tempFile = res.getFile();
		assertTrue(tempFile.exists());
		assertTrue(tempFile.canRead());
		assertTrue(tempFile.canWrite());
	}

	@Test
	public void testDispose() throws Exception {
		Resource res = storage.getResource();
		File file = res.getFile();
		assertTrue(res.exists());
		assertTrue(file.exists());
		storage.dispose();
		assertFalse(res.exists());
		assertFalse(file.exists());
	}

}
