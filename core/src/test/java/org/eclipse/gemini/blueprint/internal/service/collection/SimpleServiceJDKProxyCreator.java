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

package org.eclipse.gemini.blueprint.internal.service.collection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ProxyPlusCallback;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceProxyCreator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Simple, JDK based proxy creator useful for testing only.
 * 
 * @author Costin Leau
 */
public class SimpleServiceJDKProxyCreator implements ServiceProxyCreator {

	private Class<?>[] classes;

	private ClassLoader loader;

	private BundleContext context;


	private class JDKHandler implements InvocationHandler {

		private final ServiceReference reference;


		public JDKHandler(ServiceReference reference) {
			this.reference = reference;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object service = context.getService(reference);

			if (AopUtils.isEqualsMethod(method)) {
				return (equals(args[0]) ? Boolean.TRUE : Boolean.FALSE);
			}

			return ReflectionUtils.invokeMethod(method, service, args);
		}

		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (other == null) {
				return false;
			}

			if (Proxy.isProxyClass(other.getClass())) {
				InvocationHandler ih = Proxy.getInvocationHandler(other);
				if (ih instanceof JDKHandler) {
					return reference.equals(((JDKHandler) ih).reference);
				}
			}
			return false;
		}
	}


	public SimpleServiceJDKProxyCreator(BundleContext context, Class<?>[] classes, ClassLoader loader) {
		// add Spring-DM proxies
		Object[] obj = ObjectUtils.addObjectToArray(classes, ImportedOsgiServiceProxy.class);
		this.classes = (Class[]) ObjectUtils.addObjectToArray(obj, SpringProxy.class);
		System.out.println("given classes " + ObjectUtils.nullSafeToString(classes) + " | resulting classes "
				+ ObjectUtils.nullSafeToString(this.classes));
		this.loader = loader;
		this.context = context;
	}

	public SimpleServiceJDKProxyCreator(BundleContext context, Class<?>[] classes) {
		this(context, classes, SimpleServiceJDKProxyCreator.class.getClassLoader());
	}

	public ProxyPlusCallback createServiceProxy(final ServiceReference reference) {
		return new ProxyPlusCallback(Proxy.newProxyInstance(loader, classes, new JDKHandler(reference)), null);
	}
}
