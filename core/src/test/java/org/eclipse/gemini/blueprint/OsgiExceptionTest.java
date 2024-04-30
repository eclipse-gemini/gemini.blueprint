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

package org.eclipse.gemini.blueprint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * 
 * @author Costin Leau
 * 
 */
public class OsgiExceptionTest {
	@Test
	public void testOsgiException() {
		OsgiException exception = new OsgiException();
		assertNull(exception.getCause());
		assertNull(exception.getMessage());
	}

	@Test
	public void testOsgiExceptionStringThrowable() {
		String msg = "msg";
		Exception ex = new Exception();
		OsgiException exception = new OsgiException(msg, ex);
		assertEquals(msg, exception.getMessage());
		assertEquals(ex, exception.getCause());
	}

	@Test
	public void testOsgiExceptionString() {
		String msg = "msg";
		OsgiException exception = new OsgiException(msg);
		assertEquals(msg, exception.getMessage());
	}

	@Test
	public void testOsgiExceptionThrowable() {
		Exception ex = new Exception();
		OsgiException exception = new OsgiException(ex);
		assertSame(ex, exception.getCause());
	}
}
