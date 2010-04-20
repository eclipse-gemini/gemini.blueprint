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

package org.eclipse.gemini.blueprint.extender.internal.activator;

import org.osgi.framework.BundleContext;

/**
 * Class used for checking type compatibility between the extender and the bundles it scans. Usually the checker is
 * invoked after a bundle has been scanned and found suitable for parsing.
 * 
 * @author Costin Leau
 */
public interface TypeCompatibilityChecker {

	boolean isTypeCompatible(BundleContext targetContext);
}
