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

package org.eclipse.gemini.blueprint.extender.support;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.InitializingBean;
import org.eclipse.gemini.blueprint.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.extender.OsgiApplicationContextCreator;
import org.springframework.util.Assert;

/**
 * Useful {@link OsgiApplicationContextCreator} implementation that dictates
 * whether the default application context used by the Spring-DM extender should
 * be created (or not) based on a <code>boolean</code> value. This allows
 * clients to handle only the bundleContext filtering while being decoupled from
 * the context creation process:
 * 
 * <pre class="code">
 * 
 * ConditionalApplicationContextCreator creator = new ConditionalApplicationContextCreator();
 * 
 * creator.setFilter(new ConditionalApplicationContextCreator.BundleContextFilter() {
 * 	// filter bundles with no copyright
 * 	public boolean matches(BundleContext bundleContext) {
 * 		return bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_COPYRIGHT) != null)
 * 	}
 * }
 * 
 * creator.createApplicationContext(bundleContext); 
 * </pre>
 * 
 * @see OsgiApplicationContextCreator
 * @author Costin Leau
 */
public class ConditionalApplicationContextCreator implements OsgiApplicationContextCreator, InitializingBean {

	/**
	 * Callback used to filter the bundle contexts for which the default
	 * application contexts are created.
	 * 
	 * @author Costin Leau
	 * 
	 */
	public static interface BundleContextFilter {

		/**
		 * Determines if the given bundle context matches the filter criteria.
		 * 
		 * @param bundleContext the OSGi bundle context to check
		 * @return true if the bundle context matches, false otherwise.
		 */
		boolean matches(BundleContext bundleContext);
	}


	private BundleContextFilter filter;

	private OsgiApplicationContextCreator delegatedContextCreator;


	public void afterPropertiesSet() throws Exception {
		Assert.notNull(filter, "filter property is required");
		if (delegatedContextCreator == null)
			delegatedContextCreator = new DefaultOsgiApplicationContextCreator();
	}

	public DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext)
			throws Exception {
		if (filter.matches(bundleContext))
			return delegatedContextCreator.createApplicationContext(bundleContext);
		else
			return null;
	}

	/**
	 * Sets the {@link BundleContextFilter} used by this context creator.
	 * 
	 * @param filter The bundle context filter to set.
	 */
	public void setFilter(BundleContextFilter filter) {
		this.filter = filter;
	}

	/**
	 * Sets the {@link OsgiApplicationContextCreator} used by this context
	 * creator for the actual creation. If none is specified,
	 * {@link DefaultOsgiApplicationContextCreator} is used.
	 * 
	 * @param delegatedContextCreator the instance used for creating the
	 * application context
	 */
	public void setDelegatedApplicationContextCreator(OsgiApplicationContextCreator delegatedContextCreator) {
		this.delegatedContextCreator = delegatedContextCreator;
	}
}
