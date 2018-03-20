/*
 Copyright (c) 2006, 2010 VMware Inc.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 and Apache License v2.0 which accompanies this distribution.
 The Eclipse Public License is available at
 http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 is available at http://www.opensource.org/licenses/apache2.0.php.
 You may elect to redistribute this code under either of these licenses.

 Contributors:
 VMware Inc.
 */

package org.eclipse.gemini.blueprint.mock;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.Constants.SERVICE_ID;
import static org.osgi.framework.Constants.SERVICE_RANKING;

/**
 * ServiceReference mock.
 * <p>
 * <p/> This mock tries to adhere to the OSGi spec as much as possible by
 * providing the mandatory serviceId properties such as
 * {@link Constants#SERVICE_ID}, {@link Constants#OBJECTCLASS} and
 * {@link Constants#SERVICE_RANKING}.
 *
 * @author Costin Leau
 */
public class MockServiceReference<T> implements ServiceReference<T> {

    private Bundle bundle;
    private static long GLOBAL_SERVICE_ID = System.currentTimeMillis();

    private long serviceId;

    // private ServiceRegistration registration;
    private Dictionary<String, Object> properties;

    private String[] objectClass = new String[]{Object.class.getName()};


    /**
     * Constructs a new <code>MockServiceReference</code> instance associated
     * with the given bundle.
     *
     * @param bundle associated reference bundle
     */
    public MockServiceReference(Bundle bundle) {
        this(bundle, null, null);
    }

    /**
     * Constructs a new <code>MockServiceReference</code> instance matching
     * the given class namess.
     *
     * @param classes associated class names
     */
    public MockServiceReference(String[] classes) {
        this(null, null, classes);
    }

    /**
     * Constructs a new <code>MockServiceReference</code> instance associated
     * with the given bundle and matching the given class names.
     *
     * @param bundle  associated bundle
     * @param classes matching class names
     */
    public MockServiceReference(Bundle bundle, String[] classes) {
        this(bundle, null, classes);
    }

    /**
     * Constructs a new <code>MockServiceReference</code> instance associated
     * with the given service registration.
     */
    public MockServiceReference() {
        this(null, null, null);
    }

    /**
     * Constructs a new <code>MockServiceReference</code> instance associated
     * with the given bundle, service registration and having the given service
     * properties.
     *
     * @param bundle     associated bundle
     * @param properties reference properties
     */
    public MockServiceReference(Bundle bundle, Dictionary properties) {
        this(bundle, properties, null);
    }

    /**
     * Constructs a new <code>MockServiceReference</code> instance. This
     * constructor gives access to all the parameters of the mock service
     * reference such as associated bundle, reference properties, service
     * registration and reference class names.
     *
     * @param bundle     associated bundle
     * @param properties reference properties
     * @param classes    reference class names
     */
    public MockServiceReference(Bundle bundle, Dictionary properties, String[] classes) {
        this.bundle = (bundle == null ? new MockBundle() : bundle);
        // this.registration = (registration == null ? new
        // MockServiceRegistration() :
        // registration);
        this.properties = (properties == null ? new Hashtable() : properties);
        if (classes != null && classes.length > 0)
            this.objectClass = classes;
        addMandatoryProperties(this.properties);
    }

    private void addMandatoryProperties(Dictionary<String, Object> dict) {
        // add mandatory properties
        Object id = dict.get(SERVICE_ID);
        if (id == null || !(id instanceof Long))
            dict.put(SERVICE_ID, GLOBAL_SERVICE_ID++);

        if (dict.get(OBJECTCLASS) == null)
            dict.put(OBJECTCLASS, objectClass);

        Object ranking = dict.get(SERVICE_RANKING);
        if (ranking == null || !(ranking instanceof Integer))
            dict.put(SERVICE_RANKING, 0);

        serviceId = (Long) dict.get(SERVICE_ID);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public String[] getPropertyKeys() {
        String[] keys = new String[this.properties.size()];
        Enumeration ks = this.properties.keys();

        for (int i = 0; i < keys.length && ks.hasMoreElements(); i++) {
            keys[i] = (String) ks.nextElement();
        }

        return keys;
    }

    public Bundle[] getUsingBundles() {
        return new Bundle[]{};
    }

    public boolean isAssignableTo(Bundle bundle, String className) {
        return false;
    }

    /**
     * Sets the properties associated with this reference.
     */
    public void setProperties(Dictionary<String, ?> properties) {
        if (properties != null) {
            Dictionary<String, Object> copy = new Hashtable<>();
            Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                copy.put(key, properties.get(key));
            }

            // copy mandatory properties
            copy.put(SERVICE_ID, this.properties.get(SERVICE_ID));
            copy.put(OBJECTCLASS, this.properties.get(OBJECTCLASS));
            // optional property
            if (copy.get(SERVICE_RANKING) == null)
                copy.put(SERVICE_RANKING, this.properties.get(SERVICE_RANKING));

            this.properties = copy;
        }
    }

    /**
     * Two mock service references are equal if they contain the same service
     * id.
     */
    public boolean equals(Object obj) {
        return this == obj || obj instanceof MockServiceReference && this.hashCode() == obj.hashCode();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a hash code based on the class and service id.
     */
    public int hashCode() {
        return MockServiceReference.class.hashCode() * 13 + (int) serviceId;
    }

    public String toString() {
        return "mock service reference [owning bundle id=" + bundle.hashCode() + "|props : " + properties + "]";
    }

    public int compareTo(Object reference) {
        ServiceReference other = (ServiceReference) reference;

        // compare based on service ranking

        Object ranking = this.getProperty(SERVICE_RANKING);
        // if the property is not supplied or of incorrect type, use the default
        int rank1 = ((ranking != null && ranking instanceof Integer) ? (Integer) ranking : 0);
        ranking = other.getProperty(SERVICE_RANKING);
        int rank2 = ((ranking != null && ranking instanceof Integer) ? (Integer) ranking : 0);

        int result = rank1 - rank2;

        if (result == 0) {
            long id1 = serviceId;
            long id2 = (Long) other.getProperty(SERVICE_ID);

            // when comparing IDs, make sure to return inverse results (i.e. lower
            // id, means higher service)
            return (int) (id2 - id1);
        }

        return result;
    }
}