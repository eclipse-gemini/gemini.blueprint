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

package org.eclipse.gemini.blueprint.extender;

import org.eclipse.gemini.blueprint.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.osgi.framework.BundleContext;

/**
 * Extender hook for customizing the OSGi application context creation. Each
 * bundle started by the OSGi platform can create (or customize) an application
 * context that becomes managed by the Spring-DM extender.
 * 
 * For example, to create an application context based on the presence of a
 * manifest header, one could use the following code:
 * 
 * <pre class="code"> class HeaderBasedAppCtxCreator implements
 * OsgiApplicationContextCreator {
 * 
 * /** location header &#42;/ private static final String HEADER =
 * &quot;Context-Locations&quot;;
 * 
 * 
 * public DelegatedExecutionOsgiBundleApplicationContext
 * createApplicationContext(BundleContext bundleContext) { Bundle owningBundle =
 * bundleContext.getBundle();
 * 
 * Object value = owningBundle.getHeaders().get(HEADER); String[] locations =
 * null; if (value != null &amp;&amp; value instanceof String) { locations =
 * StringUtils.commaDelimitedListToStringArray((String) value); } else {
 * locations = &lt;default values&gt; }
 * 
 * // create application context from 'locations'
 * 
 * return applicationContext; } } </pre>
 * 
 * <p/>
 * <b>Note:</b> The application contexts should be only created and initialized
 * but not started (i.e. <code>refresh()</code> method should not be called).
 * 
 * <p/>
 * The recommended way of configuring the extender is to attach any relevant
 * <code>OsgiApplicationContextCreator</code> implementation as fragments to
 * extender bundle. Please see the OSGi specification and Spring-DM reference
 * documentation for more information on how to do that.
 * 
 * <p/>
 * Note the extender also supports <code>OsgiBeanFactoryPostProcessor</code> for
 * application context customization.
 * 
 * <p/>
 * The creation of an application context doesn't guarantee that a bundle
 * becomes Spring-DM managed. The Spring-DM extender can do additional post
 * filtering that can discard the bundle (and associated context).
 * 
 * @author Costin Leau
 * 
 */
public interface OsgiApplicationContextCreator {

	/**
	 * Creates an application context for the given bundle context. If no
	 * application context needs to be created, then <code>null</code> should be
	 * returned. Exceptions will be caught and logged but will not prevent the
	 * creation of other application contexts.
	 * 
	 * @param bundleContext OSGi bundle context determining the context creation
	 * @return <code>null</code> if no context should be created, non-
	 *         <code>null</code> otherwise
	 * @throws Exception if something goes wrong
	 */
	DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext)
			throws Exception;
}