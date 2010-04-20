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

import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;

import org.osgi.framework.BundleContext;

/**
 * Dedicated Mock Bundle that provides control over the returned entries for
 * lookup calls.
 * 
 * @author Adrian Colyer
 */
public class EntryLookupControllingMockBundle extends MockBundle {

	protected Enumeration nextFindResult = null;

	protected URL nextEntryResult = null;


	/**
	 * Constructs a new <code>EntryLookupControllingMockBundle</code>
	 * instance.
	 * 
	 * @param headers
	 */
	public EntryLookupControllingMockBundle(Dictionary headers) {
		super(headers);
	}

	public void setResultsToReturnOnNextCallToFindEntries(String[] findResult) {
		if (findResult == null) {
			findResult = new String[0];
		}
		this.nextFindResult = createEnumerationOver(findResult);
	}

	public Enumeration findEntries(String path, String filePattern, boolean recurse) {
		if (this.nextFindResult == null) {
			return super.findEntries(path, filePattern, recurse);
		}
		else {
			Enumeration ret = this.nextFindResult;
			this.nextFindResult = null;
			return ret;
		}
	}

	public void setEntryReturnOnNextCallToGetEntry(URL entry) {
		this.nextEntryResult = entry;
	}

	public URL getEntry(String name) {
		if (this.nextEntryResult != null) {
			URL result = this.nextEntryResult;
			this.nextEntryResult = null;
			return result;
		}
		else {
			return super.getEntry(name);
		}
	}

	public URL getResource(String name) {
		return getEntry(name);
	}

	// for OsgiResourceUtils
	public BundleContext getContext() {
		return super.getContext();
	}

	protected Enumeration createEnumerationOver(String[] entries) {
		return new ArrayEnumerator(entries);
	}
}