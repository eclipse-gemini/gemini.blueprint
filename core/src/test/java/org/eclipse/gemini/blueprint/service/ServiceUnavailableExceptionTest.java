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

import org.eclipse.gemini.blueprint.service.ServiceUnavailableException;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockFilter;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;

/**
 * 
 * @author Costin Leau
 * 
 */
public class ServiceUnavailableExceptionTest extends TestCase {

	public void testServiceUnavailableExceptionFilter() {
		Filter filter = new MockFilter();
		ServiceUnavailableException exception = new ServiceUnavailableException(filter);
		assertFalse(filter.toString().equals(exception.getMessage()));
	}

	public void testServiceUnavailableExceptionNullFilter() {
		ServiceUnavailableException exception = new ServiceUnavailableException((Filter) null);
		assertNotNull(exception.getMessage());
	}

	public void testServiceUnavailableExceptionString() {
		String msg = "msg";
		ServiceUnavailableException exception = new ServiceUnavailableException(msg);
		assertFalse(msg.equals(exception.getMessage()));
	}

	public void testServiceUnavailableExceptionNullString() {
		ServiceUnavailableException exception = new ServiceUnavailableException((String) null);
		assertNotNull(exception.getMessage());
	}

	public void testServiceUnavailableExceptionServiceReference() {
		ServiceReference sr = new MockServiceReference();
		ServiceUnavailableException exception = new ServiceUnavailableException(sr);
		assertNotNull(exception.getMessage());
	}

	public void testServiceUnavailableExceptionNullServiceReference() {
		ServiceUnavailableException exception = new ServiceUnavailableException((ServiceReference) null);
		assertNotNull(exception.getMessage());
	}

}
