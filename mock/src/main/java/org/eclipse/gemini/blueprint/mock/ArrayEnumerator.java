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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Simple enumeration mock backed by an array of objects.
 * 
 */
public class ArrayEnumerator<E> implements Enumeration<E> {

	private final E[] source;

	private int index = 0;


	public ArrayEnumerator(E[] source) {
		this.source = source;
	}

	public boolean hasMoreElements() {
		return source.length > index;
	}

	public E nextElement() {
		if (hasMoreElements())
			return (source[index++]);
		else
			throw new NoSuchElementException();
	}
}
