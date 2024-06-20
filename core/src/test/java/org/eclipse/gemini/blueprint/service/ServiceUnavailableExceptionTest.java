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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.eclipse.gemini.blueprint.mock.MockFilter;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.junit.Test;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

/**
 * 
 * @author Costin Leau
 * 
 */
public class ServiceUnavailableExceptionTest {
	@Test
	public void testServiceUnavailableExceptionFilter() {
		Filter filter = new MockFilter();
		ServiceUnavailableException exception = new ServiceUnavailableException(filter);
		assertFalse(filter.toString().equals(exception.getMessage()));
	}

	@Test
	public void testServiceUnavailableExceptionNullFilter() {
		ServiceUnavailableException exception = new ServiceUnavailableException((Filter) null);
		assertNotNull(exception.getMessage());
	}

	@Test
	public void testServiceUnavailableExceptionString() {
		String msg = "msg";
		ServiceUnavailableException exception = new ServiceUnavailableException(msg);
		assertFalse(msg.equals(exception.getMessage()));
	}

	@Test
	public void testServiceUnavailableExceptionNullString() {
		ServiceUnavailableException exception = new ServiceUnavailableException((String) null);
		assertNotNull(exception.getMessage());
	}

	@Test
	public void testServiceUnavailableExceptionServiceReference() {
		ServiceReference sr = new MockServiceReference();
		ServiceUnavailableException exception = new ServiceUnavailableException(sr);
		assertNotNull(exception.getMessage());
	}

	@Test
	public void testServiceUnavailableExceptionNullServiceReference() {
		ServiceUnavailableException exception = new ServiceUnavailableException((ServiceReference) null);
		assertNotNull(exception.getMessage());
	}

}
