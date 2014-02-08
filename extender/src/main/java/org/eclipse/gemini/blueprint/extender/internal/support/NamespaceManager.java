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

package org.eclipse.gemini.blueprint.extender.internal.support;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.eclipse.gemini.blueprint.extender.internal.util.BundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.springframework.util.Assert;
import org.xml.sax.EntityResolver;

/**
 * Support class that deals with namespace parsers discovered inside Spring bundles.
 * 
 * @author Costin Leau
 * 
 */
public class NamespaceManager implements InitializingBean, DisposableBean {

	private static final Log log = LogFactory.getLog(NamespaceManager.class);

	/** The set of all namespace plugins known to the extender */
	private NamespacePlugins namespacePlugins;

	/**
	 * ServiceRegistration object returned by OSGi when registering the NamespacePlugins instance as a service
	 */
	private ServiceRegistration nsResolverRegistration, enResolverRegistration = null;

	/** OSGi Environment */
	private final BundleContext context;

	private final String extenderInfo;

	private static final String META_INF = "META-INF/";

	private static final String SPRING_HANDLERS = "spring.handlers";

	private static final String SPRING_SCHEMAS = "spring.schemas";

	/**
	 * Constructs a new <code>NamespaceManager</code> instance.
	 * 
	 * @param context containing bundle context
	 */
	public NamespaceManager(BundleContext context) {
		this.context = context;

		extenderInfo =
				context.getBundle().getSymbolicName() + "|" + OsgiBundleUtils.getBundleVersion(context.getBundle());

		// detect package admin
		this.namespacePlugins = new NamespacePlugins();
	}

	/**
	 * Registers the namespace plugin handler if this bundle defines handler mapping or schema mapping resources.
	 * 
	 * <p/> This method considers only the bundle space and not the class space.
	 * 
	 * @param bundle target bundle
	 * @param isLazyBundle indicator if the bundle analyzed is lazily activated
	 */
	public void maybeAddNamespaceHandlerFor(Bundle bundle, boolean isLazyBundle) {
		// Ignore system bundle
		if (OsgiBundleUtils.isSystemBundle(bundle)) {
			return;
		}

		// Ignore non-wired Spring DM bundles
		if ("org.eclipse.gemini.blueprint.core".equals(bundle.getSymbolicName())
				&& !bundle.equals(BundleUtils.getDMCoreBundle(context))) {
			return;
		}

		boolean debug = log.isDebugEnabled();
		boolean trace = log.isTraceEnabled();
		// FIXME: Blueprint uber bundle temporary hack
		// since embedded libraries are not discovered by findEntries and inlining them doesn't work
		// (due to resource classes such as namespace handler definitions)
		// we use getResource

		boolean hasHandlers = false, hasSchemas = false;

		if (trace) {
			log.trace("Inspecting bundle " + bundle + " for Spring namespaces");
		}
		// extender/RFC 124 bundle
		if (context.getBundle().equals(bundle)) {

			try {
				Enumeration<?> handlers = bundle.getResources(META_INF + SPRING_HANDLERS);
				Enumeration<?> schemas = bundle.getResources(META_INF + SPRING_SCHEMAS);

				hasHandlers = handlers != null;
				hasSchemas = schemas != null;

				if (hasHandlers && debug) {
					log.debug("Found namespace handlers: " + Collections.list(schemas));
				}
			} catch (IOException ioe) {
				log.warn("Cannot discover own namespaces", ioe);
			}
		} else {
			hasHandlers = bundle.findEntries(META_INF, SPRING_HANDLERS, false) != null;
			hasSchemas = bundle.findEntries(META_INF, SPRING_SCHEMAS, false) != null;
		}

		// if the bundle defines handlers
		if (hasHandlers) {

			if (trace)
				log.trace("Bundle " + bundle + " provides Spring namespace handlers...");

			if (isLazyBundle) {
				this.namespacePlugins.addPlugin(bundle, isLazyBundle, true);
			} else {
				// check type compatibility between the bundle's and spring-extender's spring version
				if (hasCompatibleNamespaceType(bundle)) {
					this.namespacePlugins.addPlugin(bundle, isLazyBundle, false);
				} else {
					if (debug)
						log.debug("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle)
								+ "] declares namespace handlers but is not compatible with extender [" + extenderInfo
								+ "]; ignoring...");
				}
			}
		} else {
			// bundle declares only schemas, add it though the handlers might not be compatible...
			if (hasSchemas) {
				this.namespacePlugins.addPlugin(bundle, isLazyBundle, false);
				if (trace)
					log.trace("Bundle " + bundle + " provides Spring schemas...");
			}
		}
	}

	private boolean hasCompatibleNamespaceType(Bundle bundle) {
		return namespacePlugins.isTypeCompatible(bundle);
	}

	/**
	 * Removes the target bundle from the set of those known to provide handler or schema mappings.
	 * 
	 * @param bundle handler bundle
	 */
	public void maybeRemoveNameSpaceHandlerFor(Bundle bundle) {
		Assert.notNull(bundle);
		boolean removed = this.namespacePlugins.removePlugin(bundle);
		if (removed && log.isDebugEnabled()) {
			log.debug("Removed namespace handler resolver for " + OsgiStringUtils.nullSafeNameAndSymName(bundle));
		}
	}

	/**
	 * Registers the NamespacePlugins instance as an Osgi Resolver service
	 */
	private void registerResolverServices() {
		if (log.isDebugEnabled()) {
			log.debug("Registering Spring NamespaceHandlerResolver and EntityResolver...");
		}

		Bundle bnd = BundleUtils.getDMCoreBundle(context);
		Dictionary<String, Object> props = null;
		if (bnd != null) {
			props = new Hashtable<String, Object>();
			props.put(BundleUtils.DM_CORE_ID, bnd.getBundleId());
			props.put(BundleUtils.DM_CORE_TS, bnd.getLastModified());
		}
		nsResolverRegistration =
				context.registerService(new String[] { NamespaceHandlerResolver.class.getName() },
						this.namespacePlugins, props);

		enResolverRegistration =
				context.registerService(new String[] { EntityResolver.class.getName() }, this.namespacePlugins, props);

	}

	/**
	 * Unregisters the NamespaceHandler and EntityResolver service
	 */
	private void unregisterResolverService() {

		boolean result = OsgiServiceUtils.unregisterService(nsResolverRegistration);
		result = result || OsgiServiceUtils.unregisterService(enResolverRegistration);

		if (result) {
			if (log.isDebugEnabled())
				log.debug("Unregistering Spring NamespaceHandler and EntityResolver service");
		}

		this.nsResolverRegistration = null;
		this.enResolverRegistration = null;
	}

	public NamespacePlugins getNamespacePlugins() {
		return namespacePlugins;
	}

	//
	// Lifecycle methods
	//

	public void afterPropertiesSet() {
		registerResolverServices();
	}

	public void destroy() {
		unregisterResolverService();
		this.namespacePlugins.destroy();
		this.namespacePlugins = null;
	}
}