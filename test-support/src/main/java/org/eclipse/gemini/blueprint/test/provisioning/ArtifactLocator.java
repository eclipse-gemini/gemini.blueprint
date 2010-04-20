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

package org.eclipse.gemini.blueprint.test.provisioning;

import org.springframework.core.io.Resource;

/**
 * Interface describing the contract for finding dependencies artifacts.
 * Implementations can rely on various lookup strategies for finding the actual
 * artifacts (i.e. Maven, Ant, Ivy, etc...)
 * 
 * @author Costin Leau
 */
public interface ArtifactLocator {

	/** default artifact type */
	String DEFAULT_ARTIFACT_TYPE = "jar";


	/**
	 * Locates the artifact under the given group, with the given id, version
	 * and type. Implementations are free to provide defaults, in case
	 * <code>null</code> values are passed in. The only required field is #id.
	 * 
	 * @param group artifact group (can be <code>null</code>)
	 * @param id artifact id or name (required)
	 * @param version artifact version (can be <code>null</code>)
	 * @param type artifact type (can be <code>null</code>)
	 * 
	 * @return Spring resource to the located artifact
	 */
	Resource locateArtifact(String group, String id, String version, String type);

	/**
	 * Locates the artifact under the given group, with the given id, version
	 * and type. This is a shortcut version which uses the implementation
	 * default artifact type {@link #DEFAULT_ARTIFACT_TYPE}.
	 * 
	 * @param group artifact group (can be <code>null</code>)
	 * @param id artifact id or name (required)
	 * @param version artifact version (can be <code>null</code>)
	 * @return Spring resource to the located artifact
	 */
	Resource locateArtifact(String group, String id, String version);
}
