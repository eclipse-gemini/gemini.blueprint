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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.gemini.blueprint.io.internal.OsgiResourceUtils;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.UrlResource;

/**
 * Extension to {@link UrlResource} that adds support for
 * {@link ContextResource}. This resource is used by the
 * {@link OsgiBundleResourcePatternResolver} with the URLs returned by the OSGi
 * API.
 * 
 * @author Costin Leau
 */
class UrlContextResource extends UrlResource implements ContextResource {

	private final String pathWithinContext;


	/**
	 * Constructs a new <code>UrlContextResource</code> instance.
	 * 
	 * @param path
	 * @throws MalformedURLException
	 */
	public UrlContextResource(String path) throws MalformedURLException {
		super(path);
		pathWithinContext = checkPath(path);
	}

	private String checkPath(String path) {
		return (path.startsWith(OsgiResourceUtils.FOLDER_DELIMITER) ? path : OsgiResourceUtils.FOLDER_DELIMITER + path);
	}

	/**
	 * Constructs a new <code>UrlContextResource</code> instance.
	 * 
	 * @param url
	 */
	public UrlContextResource(URL url, String path) {
		super(url);
		this.pathWithinContext = checkPath(path);
	}

	public String getPathWithinContext() {
		return pathWithinContext;
	}
}
