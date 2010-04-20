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

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Costin Leau
 */
public class ConstructorBean {

	private Object value;

	public ConstructorBean(URL url) {
		this.value = url;
	}

	public ConstructorBean(URL[] url) {
		this.value = url;
	}

	public ConstructorBean(List[] lists) {
		this.value = lists;
	}

	public ConstructorBean(Map[] lists) {
		this.value = lists;
	}

	public ConstructorBean(String[] lists) {
		this.value = lists;
	}

	public ConstructorBean(char[] lists) {
		this.value = lists;
	}

	public ConstructorBean(String[][] lists) {
		this.value = lists;
	}

	
	public ConstructorBean() {
	}

	public ConstructorBean(boolean bool) {
		value = bool;
	}

	public ConstructorBean(Boolean bool) {
		value = bool;
	}

	public Object getValue() {
		return value;
	}

	public Object makeInstance(boolean bool) {
		return Boolean.valueOf(bool);
	}

	public Object makeInstance(Boolean bool) {
		return bool;
	}

	public Object makeInstance(URL url) {
		return url;
	}

	public Object makeInstance(String str) {
		return str;
	}

	public Object makeInstance(Class arg2) {
		return arg2;
	}

	public Object makeInstance(File arg2) {
		return arg2;
	}

	public Object makeInstance(Locale arg2) {
		return arg2;
	}

	public Object makeInstance(Date arg2) {
		return arg2;
	}

	public Object makeInstance(Map arg2) {
		return arg2;
	}

	public Object makeInstance(Set arg2) {
		return arg2;
	}

	public Object makeInstance(List arg2) {
		return arg2;
	}

	public Object makeInstance(Date[] arg1) {
		return arg1;
	}

	public Object makeInstance(URL[] arg1) {
		return arg1;
	}

	public Object makeInstance(Class[] arg1) {
		return arg1;
	}

	public Object makeInstance(Locale[] arg1) {
		return arg1;
	}

	public Object makeInstance(List[] arg1) {
		return arg1;
	}

	public Object makeInstance(Set[] arg1) {
		return arg1;
	}

//	public Object makeInstance(Map[] arg1) {
//		return arg1;
//	}
}