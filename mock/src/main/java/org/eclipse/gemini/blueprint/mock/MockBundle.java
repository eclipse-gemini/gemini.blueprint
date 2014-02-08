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
import java.net.URL;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * Bundle mock. Except resource/class loading operations (which are executed on its internal class loader), the rest of
 * the methods are dummies.
 * 
 * @author Costin Leau
 */
public class MockBundle implements Bundle {

	private String location;

	private final Dictionary headers;

	private static int GENERAL_BUNDLE_ID = 0;

	private long bundleId = (GENERAL_BUNDLE_ID++);

	// required for introspection by util classes (should be removed)
	private BundleContext bundleContext;

	private ClassLoader loader = getClass().getClassLoader();

	private final Dictionary defaultHeaders = new Hashtable(0);

	private final String SYMBOLIC_NAME = "Mock-Bundle_" + System.currentTimeMillis();

	private final String symName;

	private final Version version;

    @Override
    public int compareTo(Bundle o) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static class EmptyEnumeration implements Enumeration {

		public boolean hasMoreElements() {
			return false;
		}

		public Object nextElement() {
			throw new NoSuchElementException();
		}
	}

	/**
	 * Constructs a new <code>MockBundle</code> instance using default values.
	 * 
	 */
	public MockBundle() {
		this(null, null, null);
	}

	/**
	 * Constructs a new <code>MockBundle</code> instance with the given bundle headers.
	 * 
	 * @param headers bundle headers
	 */
	public MockBundle(Dictionary headers) {
		this(null, headers, null);
	}

	/**
	 * Constructs a new <code>MockBundle</code> instance associated with the given bundle context.
	 * 
	 * @param context associated bundle context
	 */
	public MockBundle(BundleContext context) {
		this(null, null, context);
	}

	/**
	 * Constructs a new <code>MockBundle</code> instance with the given symbolic name.
	 * 
	 * @param symName bundle symbolic name
	 */
	public MockBundle(String symName) {
		this(symName, null, null);
	}

	/**
	 * Constructs a new <code>MockBundle</code> instance using the given bundle symbolic name, properties and associated
	 * bundle context.
	 * 
	 * @param symName bundle symbolic name
	 * @param headers bundle headers
	 * @param context associated bundle context
	 */
	public MockBundle(String symName, Dictionary headers, BundleContext context) {
		this.symName = ((symName != null && symName.length() > 0) ? symName : SYMBOLIC_NAME);
		defaultHeaders.put("Bundle-SymbolicName", this.symName);

		this.location = "<default location>";
		this.headers = (headers == null ? defaultHeaders : headers);
		this.bundleContext = (context == null ? new MockBundleContext(this) : context);
		this.version = getVersion(headers);
	}

	/**
	 * Delegates to the classloader. Identical to classLoader.getResources(path + filePattern);
	 * 
	 * @see org.osgi.framework.Bundle#findEntries(java.lang.String, java.lang.String, boolean)
	 */
	public Enumeration findEntries(String path, String filePattern, boolean recurse) {
		Enumeration enm = null;

		try {
			enm = loader.getResources(path + "/" + filePattern);
		} catch (IOException ex) {
			// catch to allow nice behavior
			System.err.println("returning an empty enumeration as cannot load resource; exception " + ex);
		}
		return (enm == null ? new EmptyEnumeration() : enm);
	}

	public long getBundleId() {
		return this.bundleId;
	}

	public void setBundleId(long bundleId) {
		this.bundleId = bundleId;
	}

	public URL getEntry(String name) {
		return loader.getResource(name);
	}

	public Enumeration getEntryPaths(String path) {
		return new EmptyEnumeration();
	}

	public Dictionary getHeaders() {
		return headers;
	}

	public Dictionary getHeaders(String locale) {
		return getHeaders();
	}

	public long getLastModified() {
		return 0;
	}

	public String getLocation() {
		return location;
	}

	public ServiceReference[] getRegisteredServices() {
		return new ServiceReference[] {};
	}

	public URL getResource(String name) {
		return loader.getResource(name);
	}

	public Enumeration getResources(String name) throws IOException {
		return loader.getResources(name);
	}

	public ServiceReference[] getServicesInUse() {
		return new ServiceReference[] {};
	}

	public int getState() {
		return Bundle.ACTIVE;
	}

	public String getSymbolicName() {
		String name = (String) headers.get(Constants.BUNDLE_SYMBOLICNAME);
		return (name == null ? SYMBOLIC_NAME : name);
	}

	public boolean hasPermission(Object permission) {
		return true;
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loader.loadClass(name);
	}

	public void start() throws BundleException {
		start(0);
	}

	public void start(int options) throws BundleException {
	}

	public void stop() throws BundleException {
		stop(0);
	}

	public void stop(int options) throws BundleException {
	}

	public void uninstall() throws BundleException {
	}

	public void update() throws BundleException {
	}

	public void update(InputStream in) throws BundleException {
	}

	public Version getVersion() {
		return version;
	}

    @Override
    public <A> A adapt(Class<A> type) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public File getDataFile(String filename) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static Version getVersion(Dictionary headers) {
		if (headers != null) {
			Object header = headers.get(Constants.BUNDLE_VERSION);
			if (header instanceof String) {
				return Version.parseVersion((String) header);
			}
		}

		return Version.emptyVersion;
	}

	public Map getSignerCertificates(int signerType) {
		return Collections.emptyMap();
	}

	// chiefly here so that compilers/find-bugs don't complain about the
	// "unused" bundleContext field.
	// also enables OsgiResoureUtils.getBundleContext to find the context via
	// reflection
	public BundleContext getContext() {
		return this.bundleContext;
	}

	public BundleContext getBundleContext() {
		return getContext();
	}

	public String toString() {
		return symName;
	}

	/**
	 * Sets the location for this mock bundle.
	 * 
	 * @param location bundle location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Sets the class loader internally used by the bundle to mock the loading operations. By default, the MockBundle
	 * uses its own class loader.
	 * 
	 * @param loader mock bundle class loader
	 */
	public void setClassLoader(ClassLoader loader) {
		if (loader == null) {
			throw new IllegalArgumentException("A non-null class loader expected");
		}

		this.loader = loader;
	}
}