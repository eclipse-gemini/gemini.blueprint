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

import java.util.Date;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceProxyCreator;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceCollection;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.mock.MockServiceReference;
import org.springframework.util.ClassUtils;

/**
 * Base class for Osgi service dynamic collection tests.
 * 
 * @author Costin Leau
 * 
 */
public abstract class AbstractOsgiCollectionTest extends TestCase {

	protected MockBundleContext context;

	protected Map services;

	protected OsgiServiceCollection col;


	public static interface Wrapper {

		Object execute();
	}

	public static class DateWrapper implements Wrapper, Comparable {

		private Date date;


		public DateWrapper(long time) {
			date = new Date(time);
		}

		public Object execute() {
			return new Long(date.getTime());
		}

		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (other instanceof DateWrapper) {
				DateWrapper oth = (DateWrapper) other;
				return (date.equals(oth.date));
			}
			return false;
		}

		public int hashCode() {
			return DateWrapper.class.hashCode() * 13 + date.hashCode();
		}

		public int compareTo(Object o) {
			Wrapper wr = (Wrapper) o;
			Long time = (Long) wr.execute();
			return new Long(date.getTime()).compareTo(time);
		}

	};


	protected void setUp() throws Exception {
		services = new LinkedHashMap();

		context = new MockBundleContext() {

			public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
				return new ServiceReference[0];
			}

			public Object getService(ServiceReference reference) {
				Object service = services.get(reference);
				return (service == null ? new Object() : service);
			}
		};

		col = createCollection();
		col.setRequiredAtStartup(false);
		col.afterPropertiesSet();
	}

	abstract OsgiServiceCollection createCollection();

	protected ServiceProxyCreator createProxyCreator(Class<?>[] classes) {
		return new SimpleServiceJDKProxyCreator(context, classes, getClass().getClassLoader());
	}

	protected void tearDown() throws Exception {
		services = null;
		context = null;
	}

	protected void addService(Object service, Dictionary properties) {

		ServiceReference ref = null;
		ServiceEvent event = null;

		Set intfs = ClassUtils.getAllInterfacesAsSet(service);
		String[] clazzez = new String[intfs.size()];

		int i = 0;
		for (Iterator iter = intfs.iterator(); iter.hasNext();) {
			clazzez[i++] = ((Class) iter.next()).getName();
		}

		ref = new MockServiceReference(null, properties, null, clazzez);

		event = new ServiceEvent(ServiceEvent.REGISTERED, ref);

		services.put(ref, service);

		for (Iterator iter = context.getServiceListeners().iterator(); iter.hasNext();) {
			ServiceListener listener = (ServiceListener) iter.next();
			listener.serviceChanged(event);
		}
	}

	protected void addService(Object service) {
		addService(service, new Properties());
	}

	protected void removeService(Object service) {
		ServiceReference ref = new MockServiceReference();

		for (Iterator iter = services.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			if (entry.getValue().equals(service)) {
				ref = (ServiceReference) entry.getKey();
				continue;
			}
		}

		services.remove(ref);

		ServiceEvent event = new ServiceEvent(ServiceEvent.UNREGISTERING, ref);

		for (Iterator iter = context.getServiceListeners().iterator(); iter.hasNext();) {
			ServiceListener listener = (ServiceListener) iter.next();
			listener.serviceChanged(event);
		}

	}
}
