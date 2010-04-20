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

package org.eclipse.gemini.blueprint.extender.support.scanning;

import org.osgi.framework.Bundle;

/**
 * Convenience scanner locating suitable Spring configurations inside an OSGi
 * bundle. This interface can be implemented to customize Spring-DM default
 * definition of a 'Spring-powered' bundle by using different locations or
 * supplying defaults for bundles that do not provide a proper configuration.
 * 
 * <p/> Additionally, non-XML configurations (for example annotation-based) can
 * be plugged in. This would normally imply a custom application context creator
 * as well.
 * 
 * <p/><b>Note:</b>It is strongly recommended that the default locations (<tt>META-INF/spring/*.xml</tt>
 * or <tt>Spring-Context</tt> manifest header) are supported (through chaining
 * or by extending the default implementation) to avoid breaking bundles using
 * them.
 * 
 * <p/>This interface is intended for usage with the default
 * {@link org.eclipse.gemini.blueprint.extender.OsgiApplicationContextCreator}
 * implementation.
 * 
 * @see org.eclipse.gemini.blueprint.extender.support.DefaultOsgiApplicationContextCreator
 * @see org.eclipse.gemini.blueprint.extender.OsgiApplicationContextCreator
 * 
 * @author Costin Leau
 */
public interface ConfigurationScanner {

	/**
	 * Returns an array of existing Spring configuration locations (as Strings)
	 * for the given bundle. If no resource was found, an empty/null array
	 * should be returned.
	 * 
	 * @param bundle non-null bundle intended for scanning
	 * @return Spring configuration locations
	 */
	String[] getConfigurations(Bundle bundle);
}
