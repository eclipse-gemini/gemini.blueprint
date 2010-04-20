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

package org.eclipse.gemini.blueprint.io;

import org.osgi.framework.Bundle;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * OSGi specific {@link org.springframework.core.io.ResourceLoader}
 * implementation.
 * 
 * This loader resolves paths inside an OSGi bundle using the bundle native
 * methods. Please see {@link OsgiBundleResource} javadoc for information on
 * what prefixes are supported.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 * 
 * @see org.osgi.framework.Bundle
 * @see org.eclipse.gemini.blueprint.io.OsgiBundleResource
 * 
 */
public class OsgiBundleResourceLoader extends DefaultResourceLoader {

	private final Bundle bundle;


	/**
	 * Creates a OSGi aware <code>ResourceLoader</code> using the given
	 * bundle.
	 * 
	 * @param bundle OSGi <code>Bundle</code> to be used by this loader
	 * loader.
	 */
	public OsgiBundleResourceLoader(Bundle bundle) {
		this.bundle = bundle;
	}

	protected Resource getResourceByPath(String path) {
		Assert.notNull(path, "Path is required");
		return new OsgiBundleResource(this.bundle, path);
	}

	public Resource getResource(String location) {
		Assert.notNull(location, "location is required");
		return new OsgiBundleResource(bundle, location);
	}

	/**
	 * Returns the bundle used by this loader.
	 * 
	 * @return OSGi <code>Bundle</code> used by this resource
	 */
	public final Bundle getBundle() {
		return bundle;
	}

}
