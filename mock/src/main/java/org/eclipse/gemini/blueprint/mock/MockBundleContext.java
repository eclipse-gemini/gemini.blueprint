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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * BundleContext mock.
 * 
 * <p/>
 * Can be configured to use a predefined Bundle or/and configuration. By
 * default, will create an internal MockBundle. Most of the operations are no-op
 * (as anonymous classes with specific functionality can be created per use
 * basis).
 * 
 * @author Costin Leau
 * 
 */
public class MockBundleContext implements BundleContext {

	public static final Properties DEFAULT_PROPERTIES = new DefaultBundleContextProperties();

	private Bundle bundle;

	private Properties properties;

    protected Set<ServiceListener> serviceListeners;
    protected Set<BundleListener> bundleListeners;


	/**
	 * Constructs a new <code>MockBundleContext</code> instance. The associated
	 * bundle will be created automatically.
	 */
	public MockBundleContext() {
		this(null, null);
	}

	/**
	 * Constructs a new <code>MockBundleContext</code> instance.
	 * 
	 * @param bundle associated bundle
	 */
	public MockBundleContext(Bundle bundle) {
		this(bundle, null);
	}

	/**
	 * Constructs a new <code>MockBundleContext</code> instance allowing both
	 * the bundle and the context properties to be specified.
	 * 
	 * @param bundle associated bundle
	 * @param props context properties
	 */
	public MockBundleContext(Bundle bundle, Properties props) {
		this.bundle = (bundle == null ? new MockBundle(this) : bundle);
		properties = new Properties(DEFAULT_PROPERTIES);
		if (props != null)
			properties.putAll(props);

        // make sure the order is preserved
        this.serviceListeners = new LinkedHashSet<ServiceListener>(2);
        this.bundleListeners = new LinkedHashSet<BundleListener>(2);
	}

	public void addBundleListener(BundleListener listener) {
		bundleListeners.add(listener);
	}

	public void addFrameworkListener(FrameworkListener listener) {
	}

    public void addServiceListener(ServiceListener listener) {
        try {
            addServiceListener(listener, null);
        } catch (InvalidSyntaxException ex) {
            throw new IllegalStateException("exception should not occur");
        }
    }

	public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
		if (listener == null)
			throw new IllegalArgumentException("non-null listener required");
		this.serviceListeners.add(listener);
	}

	public Filter createFilter(String filter) throws InvalidSyntaxException {
		return new MockFilter(filter);
	}

    @Override
    public Bundle getBundle(String location) {
        // always return null as we do not keep track of location.
        return null;
    }

    public ServiceReference[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
		return new ServiceReference[] {};
	}

	public Bundle getBundle() {
		return bundle;
	}

	public Bundle getBundle(long id) {
		return bundle;
	}

	public Bundle[] getBundles() {
		return new Bundle[] { bundle };
	}

	public File getDataFile(String filename) {
		return null;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public <S> S getService(ServiceReference<S> reference) {
        Class type = getClass(reference.getClass().getGenericSuperclass());
        try {
            @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"}) S result = (S)type.newInstance();
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Class<?> getClass(Type type) {
        if (type instanceof Class) {
          return (Class) type;
        }
        else if (type instanceof ParameterizedType) {
          return getClass(((ParameterizedType) type).getRawType());
        }
        else if (type instanceof GenericArrayType) {
          Type componentType = ((GenericArrayType) type).getGenericComponentType();
          Class<?> componentClass = getClass(componentType);
          if (componentClass != null ) {
            return Array.newInstance(componentClass, 0).getClass();
          }
          else {
            return null;
          }
        }
        else {
          return null;
        }
      }

	public ServiceReference getServiceReference(String clazz) {
		return new MockServiceReference(getBundle(), new String[] { clazz });
	}

    @Override
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        @SuppressWarnings({"UnnecessaryLocalVariable", "unchecked"})
        ServiceReference<S> result = (ServiceReference<S>) getServiceReference(clazz.getName());
        return result;
    }

    @Override
    public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) throws InvalidSyntaxException {
        @SuppressWarnings("unchecked")
        ServiceReference<S>[] refs = (ServiceReference<S>[]) getServiceReferences(clazz.getName(), filter);
        if (refs == null) {
            return Collections.emptyList();
        }
        List<ServiceReference<S>> result = new ArrayList<ServiceReference<S>>(refs.length);
        for (ServiceReference<S> r : refs) {
            result.add(r);
        }
        return result;
    }

    public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
		// Some jiggery-pokery to get round the fact that we don't ever use the clazz
		if (clazz == null) {
			if (filter != null) {
				// flatten filter since the constants might be case insensitive
				String flattenFilter = filter.toLowerCase();
				int i = flattenFilter.indexOf(Constants.OBJECTCLASS.toLowerCase() + "=");
				if (i > 0) {
					clazz = filter.substring(i + Constants.OBJECTCLASS.length() + 1);
					clazz = clazz.substring(0, clazz.indexOf(")"));
				}
			} else {
				clazz = Object.class.getName();
            }
        }
		return new ServiceReference[] { new MockServiceReference(getBundle(), new String[] { clazz }) };
	}

	public Bundle installBundle(String location) throws BundleException {
		MockBundle bundle = new MockBundle();
		bundle.setLocation(location);
		return bundle;
	}

	public Bundle installBundle(String location, InputStream input) throws BundleException {
		try {
			input.close();
        } catch (IOException ex) {
			throw new BundleException("cannot close stream", ex);
		}
		return installBundle(location);
	}

	public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
		MockServiceRegistration reg = new MockServiceRegistration(properties);

		// disabled for now
		// MockServiceReference ref = new MockServiceReference(this.bundle,
		// properties, reg, clazzes);
		// ServiceEvent event = new ServiceEvent(ServiceEvent.REGISTERED, ref);
		//
		// for (Iterator iter = serviceListeners.iterator(); iter.hasNext();) {
		// ServiceListener listener = (ServiceListener) iter.next();
		// listener.serviceChanged(event);
		// }

		return reg;
	}

	public ServiceRegistration registerService(String clazz, Object service, Dictionary properties) {
		return registerService(new String[] { clazz }, service, properties);
	}

	public void removeBundleListener(BundleListener listener) {
		bundleListeners.remove(listener);
	}

	public void removeFrameworkListener(FrameworkListener listener) {
	}

    @Override
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        ServiceRegistration<S> registration = (ServiceRegistration<S>) registerService(clazz.getName(), service, properties);
        return registration;
    }

    public void removeServiceListener(ServiceListener listener) {
		serviceListeners.remove(listener);
	}

	public boolean ungetService(ServiceReference reference) {
		return false;
	}

	/**
	 * Sets the bundle associated with this context.
	 * 
	 * @param bundle associated bundle
	 */
	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}

	// hooks
	/**
	 * Returns a set of registered service listeners. Handy method when mocking
	 * with listeners is required.
	 * 
	 * @return set of registered service listeners
	 */
    public Set<ServiceListener> getServiceListeners() {
		return serviceListeners;
	}

	/**
	 * Returns a set of registered bundle listeners.
	 * 
	 * @return set of registered bundle listeners
	 */
    public Set<BundleListener> getBundleListeners() {
		return bundleListeners;
	}
}