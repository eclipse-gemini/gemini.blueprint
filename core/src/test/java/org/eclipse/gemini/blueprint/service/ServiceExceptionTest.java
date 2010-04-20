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

package org.eclipse.gemini.blueprint.service;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.service.ServiceException;

/**
 * 
 * @author Costin Leau
 * 
 */
public class ServiceExceptionTest extends TestCase {

	public void testServiceException() {
		ServiceException exception = new ServiceException();
		assertNull(exception.getCause());
		assertNull(exception.getMessage());
	}

	public void testServiceExceptionStringThrowable() {
		String msg = "msg";
		Exception ex = new Exception();
		ServiceException exception = new ServiceException(msg, ex);
		assertEquals(msg, exception.getMessage());
		assertEquals(ex, exception.getCause());
	}

	public void testServiceExceptionString() {
		String msg = "msg";
		ServiceException exception = new ServiceException(msg);
		assertEquals(msg, exception.getMessage());
	}

	public void testServiceExceptionThrowable() {
		Exception ex = new Exception();
		ServiceException exception = new ServiceException(ex);
		assertSame(ex, exception.getCause());
	}

}
