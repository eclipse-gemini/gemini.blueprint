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

package org.eclipse.gemini.blueprint.mock;

import java.util.Dictionary;
import java.util.Map;

import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

/**
 * Filter mock.
 * 
 * <p/> Just a no-op interface implementation.
 * 
 * @author Costin Leau
 */
public class MockFilter implements Filter {

	private String filter;


	/**
	 * Constructs a new <code>MockFilter</code> instance.
	 * 
	 */
	public MockFilter() {
		this("<no filter>");
	}

	/**
	 * Constructs a new <code>MockFilter</code> instance.
	 * 
	 * @param filter OSGi filter
	 */
	public MockFilter(String filter) {
		this.filter = filter;
	}

	public boolean match(ServiceReference reference) {
		return false;
	}

	public boolean match(Dictionary dictionary) {
		return false;
	}

	public boolean matchCase(Dictionary dictionary) {
		return false;
	}

	public String toString() {
		return filter;
	}

    @Override
    public boolean matches(Map<String, ?> map) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}