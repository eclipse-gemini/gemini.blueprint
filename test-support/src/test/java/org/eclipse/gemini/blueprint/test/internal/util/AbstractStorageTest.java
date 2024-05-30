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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.gemini.blueprint.test.internal.util.jar.storage.Storage;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

/**
 * Set of storage tests which can be applied on various Storage
 * implementations.This class should be subclassed to run the test against
 * concrete Storage implementations.
 * 
 * @author Costin Leau
 * 
 */
public abstract class AbstractStorageTest {

	protected Storage storage;

	/**
	 * Create the actual storage.
	 * 
	 * @return
	 */
	protected abstract Storage createStorage();

	@Before
	public void setup() throws Exception {
		storage = createStorage();
	}

	@After
	public void tearDown() throws Exception {
		storage.dispose();
	}

	@Test
	public void testInitialInputStream() throws Exception {
		InputStream in = storage.getInputStream();
		try {
			assertEquals(-1, in.read());
		}
		finally {
			IOUtils.closeStream(in);
		}
	}

	@Test
	public void testIdenticalInputStreams() throws Exception {
		assertTrue("streams not identical", compareStreams(storage.getInputStream(), storage.getInputStream()));
	}

	@Test
	public void testIdenticalInputStreamsFromResource() throws Exception {
		assertTrue("streams not identical", compareStreams(storage.getResource().getInputStream(), storage
				.getResource().getInputStream()));
	}

	@Test
	public void testReadWrite() throws Exception {
		int wrote = FileCopyUtils.copy(getSampleContentAsInputStream(), storage.getOutputStream());
		System.out.println("wrote " + wrote + " bytes");
		assertTrue("streams content is different", compareStreams(getSampleContentAsInputStream(), storage
				.getInputStream()));
	}

	@Test
	public void testResource() throws Exception {
		Resource res = storage.getResource();
		assertNotNull(res);
		assertFalse("underlying storage is not reusable", res.isOpen());
	}

	@Test
	public void testCompareInputStreamAndResourceInputStream() throws Exception {
		InputStream in1 = storage.getInputStream();
		InputStream in2 = storage.getResource().getInputStream();
		assertTrue("streams content is different", compareStreams(in1, in2));
	}

	private boolean compareStreams(InputStream in1, InputStream in2) throws Exception {
		int count = 0;
		try {
			int b;
			while ((b = in1.read()) != -1) {
				count++;
				int a = in2.read();
				boolean same = (a == b);
				if (!same) {
					System.out.println("expected " + b + " but was " + a + ";problem occured after reading " + count + " bytes");
					return false;
				}
			}
			// check we have reached the end on both streams
			return (in1.read() == in2.read());
		}
		finally {
			IOUtils.closeStream(in1);
			IOUtils.closeStream(in2);
		}
	}

	private InputStream getSampleContentAsInputStream() throws Exception {
		return getClass().getClassLoader().getResourceAsStream(getClass().getName().replace('.', '/') + ".class");
	}
}
