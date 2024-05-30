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

package org.eclipse.gemini.blueprint.io.internal;

import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.gemini.blueprint.io.OsgiBundleResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Utility class used for IO resources.
 * 
 * @author Costin Leau
 * 
 */
public abstract class OsgiResourceUtils {

	public static final String EMPTY_PREFIX = "";

	public static final String PREFIX_DELIMITER = ":";

	public static final String FOLDER_DELIMITER = "/";

	// PREFIXES TYPES

	// non-osgi prefixes (file, http)
	public static final int PREFIX_TYPE_UNKNOWN = -1;

	// no prefix
	public static final int PREFIX_TYPE_NOT_SPECIFIED = 0x00000000;

	// osgibundlejar:
	public static final int PREFIX_TYPE_BUNDLE_JAR = 0x00000001;

	// osgibundle:
	public static final int PREFIX_TYPE_BUNDLE_SPACE = 0x00000010;

	// classpath:
	public static final int PREFIX_TYPE_CLASS_SPACE = 0x00000100;

	// classpath*:
	public static final int PREFIX_TYPE_CLASS_ALL_SPACE = 0x00000200;


	/**
	 * Return the path prefix if there is any or {@link #EMPTY_PREFIX}
	 * otherwise.
	 * 
	 * @param path
	 * @return
	 */
	public static String getPrefix(String path) {
		if (path == null)
			return EMPTY_PREFIX;
		int index = path.indexOf(PREFIX_DELIMITER);
		return ((index > 0) ? path.substring(0, index + 1) : EMPTY_PREFIX);
	}

	/**
	 * Return the search type to be used for the give string based on the
	 * prefix.
	 * 
	 * @param path
	 * @return
	 */
	public static int getSearchType(String path) {
		Assert.notNull(path, "path is required");
		int type = PREFIX_TYPE_NOT_SPECIFIED;
		String prefix = getPrefix(path);

		// no prefix is treated just like osgibundle:
		if (!StringUtils.hasText(prefix))
			type = PREFIX_TYPE_NOT_SPECIFIED;
		else if (prefix.startsWith(OsgiBundleResource.BUNDLE_URL_PREFIX))
			type = PREFIX_TYPE_BUNDLE_SPACE;
		else if (prefix.startsWith(OsgiBundleResource.BUNDLE_JAR_URL_PREFIX))
			type = PREFIX_TYPE_BUNDLE_JAR;
		else if (prefix.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX))
			type = PREFIX_TYPE_CLASS_SPACE;
		else if (prefix.startsWith(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX))
			type = PREFIX_TYPE_CLASS_ALL_SPACE;

		else
			type = PREFIX_TYPE_UNKNOWN;

		return type;
	}

	public static boolean isClassPathType(int type) {
		return (type == PREFIX_TYPE_CLASS_SPACE || type == PREFIX_TYPE_CLASS_ALL_SPACE);
	}

	public static String stripPrefix(String path) {
		// strip prefix
		int index = path.indexOf(PREFIX_DELIMITER);
		return (index > -1 ? path.substring(index + 1) : path);

	}

	public static Resource[] convertURLArraytoResourceArray(URL[] urls) {
		if (urls == null) {
			return new Resource[0];
		}

		// convert this into a resource array
		Resource[] res = new Resource[urls.length];
		for (int i = 0; i < urls.length; i++) {
			res[i] = new UrlResource(urls[i]);
		}
		return res;
	}

	public static Resource[] convertURLEnumerationToResourceArray(Enumeration<URL> enm) {
		Set<UrlResource> resources = new LinkedHashSet<UrlResource>(4);
		while (enm != null && enm.hasMoreElements()) {
			resources.add(new UrlResource(enm.nextElement()));
		}
		return (Resource[]) resources.toArray(new Resource[resources.size()]);
	}

	/**
	 * Similar to /path/path1/ -> /path/, /path/file -> /path/
	 * 
	 * @return
	 */
	public static String findUpperFolder(String path) {
		if (path.length() < 2)
			return path;

		String newPath = path;
		// if it's a folder
		if (path.endsWith(FOLDER_DELIMITER)) {
			newPath = path.substring(0, path.length() - 1);
		}

		int index = newPath.lastIndexOf(FOLDER_DELIMITER);
		if (index > 0)
			return newPath.substring(0, index + 1);

		else
			// fallback to defaults
			return path;
	}
}
