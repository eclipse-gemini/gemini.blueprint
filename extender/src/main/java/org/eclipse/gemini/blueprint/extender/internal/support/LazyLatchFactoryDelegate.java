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

package org.eclipse.gemini.blueprint.extender.internal.support;

import java.util.concurrent.CountDownLatch;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.Mergeable;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.util.ClassUtils;

/**
 * Delegate class that allows access to LazyLatchFactory class (inside Spring DM
 * core) from Spring DM extender.
 * 
 * @author Costin Leau
 */
public abstract class LazyLatchFactoryDelegate {

	private static final Mergeable a;
	private static final SourceExtractor b;

	static {
		ClassLoader extenderClassLoader = LazyLatchFactoryDelegate.class.getClassLoader();
		Class<?> coreClass = ClassUtils.resolveClassName("org.eclipse.gemini.blueprint.util.OsgiBundleUtils",
			extenderClassLoader);
		Class<?> clzz = ClassUtils.resolveClassName(
			"org.eclipse.gemini.blueprint.service.exporter.support.internal.support.LazyLatchFactory",
			coreClass.getClassLoader());
		Object factory = BeanUtils.instantiateClass(clzz);
		a = (Mergeable) factory;
		b = (SourceExtractor) factory;
	}


	public static CountDownLatch addLatch(Integer key) {
		return (CountDownLatch) b.extractSource(key, null);
	}

	public static CountDownLatch removeLatch(Integer key) {
		return (CountDownLatch) a.merge(key);
	}

	public static void clear() {
		a.isMergeEnabled();
	}
}
