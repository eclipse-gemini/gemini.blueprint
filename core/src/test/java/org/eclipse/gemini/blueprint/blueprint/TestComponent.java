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

package org.eclipse.gemini.blueprint.blueprint;

import java.util.Collection;
import java.util.List;

import org.osgi.framework.ServiceReference;

/**
 * Just a simple component used by the namespace tests.
 * 
 * @author Costin Leau
 * 
 */
public class TestComponent implements java.io.Serializable {

	private Object propA;
	private Object propB;
	private ServiceReference serviceReference;

	public TestComponent() {
	}

	public TestComponent(Object arg) {
		propA = arg;
	}

	public TestComponent(int arg) {
		propA = arg;
	}
	
	public TestComponent(String str) {
		this.propA = str;
	}
	
	
	public TestComponent(CustomType str) {
		this.propA = str;
	}
	
	public TestComponent(Double dbl) {
		this.propA = dbl;
	}

	public TestComponent(Object arg1, Object arg2) {
		propA = arg1;
		propB = arg2;
	}

	public Object getPropA() {
		return propA;
	}

	public void setPropA(Object property) {
		this.propA = property;
	}

	public Object getPropB() {
		return propB;
	}

	public void setPropB(Object propB) {
		this.propB = propB;
	}

	public void setList(List list) {
		this.propA = list;
	}

	public void setCollection(Collection col) {
		this.propA = col;
	}

	public void setArray(CustomType[] reg) {
		this.propA = reg;
	}
	
	public ServiceReference getServiceReference() {
		return serviceReference;
	}

	public void setServiceReference(ServiceReference serviceReference) {
		this.serviceReference = serviceReference;
	}
	
	public void setBool(boolean bool) {
		this.propA = Boolean.valueOf(bool);
	}

	public void init() {
		System.out.println("Initialized");
	}
}