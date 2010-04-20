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

package org.eclipse.gemini.blueprint.service.importer;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.service.importer.ServiceProxyDestroyedException;

/**
 * 
 * @author Costin Leau
 * 
 */
public class ServiceProxyDestroyedExceptionTest extends TestCase {

	public void testServiceProxyDestroyedException() {
		ServiceProxyDestroyedException exception = new ServiceProxyDestroyedException();
		assertNull(exception.getCause());
		assertNotNull(exception.getMessage());
	}

	public void testServiceProxyDestroyedExceptionStringThrowable() {
		String msg = "msg";
		Exception ex = new Exception();
		ServiceProxyDestroyedException exception = new ServiceProxyDestroyedException(msg, ex);
		assertEquals(msg, exception.getMessage());
		assertEquals(ex, exception.getCause());
	}

	public void testServiceProxyDestroyedExceptionString() {
		String msg = "msg";
		ServiceProxyDestroyedException exception = new ServiceProxyDestroyedException(msg);
		assertEquals(msg, exception.getMessage());
	}

	public void testServiceProxyDestroyedExceptionThrowable() {
		Exception ex = new Exception();
		ServiceProxyDestroyedException exception = new ServiceProxyDestroyedException(ex);
		assertEquals(ex, exception.getCause());
	}
}
