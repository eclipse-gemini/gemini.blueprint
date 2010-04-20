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

package org.eclipse.gemini.blueprint.extender.internal.dependencies.shutdown;

import org.osgi.framework.Bundle;

/**
 * SPI for sorting OSGi bundles based on their service dependency. Given a
 * number of bundles, implementors of this interface will return a list
 * referencing the bundles in the order in which they should be shutdown based
 * on their OSGi service dependencies.
 * <p/>
 * It is considered that bundle A depends on bundle B if A uses a service that
 * belongs to a bundle which depends on B or is B itself. Note that bundles can
 * depend on each other : A -> B -> A.
 * <p/>
 * Thus implementations should 'sort' direct, circular graphs without any
 * guarantee on the node used for start.
 *  
 * @author Costin Leau
 * 
 */
public interface ServiceDependencySorter {

	/**
	 * Given a number of bundles, determine the dependency between each other and compute
	 * the dependency tree.
	 * 
	 * @param bundles array of bundles
	 * @return an array of bundles, sorted out by their dependency. 
	 */
	Bundle[] computeServiceDependencyGraph(Bundle[] bundles);
}
