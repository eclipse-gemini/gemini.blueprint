/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.extender.internal.support;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Spring schema handler/resolver for OSGi environments.
 * 
 * Besides delegation this class also does type filtering to avoid wiring the wrong bundle if multiple versions of the
 * same library (which support the same schema) are available.
 * 
 * Additionally, lazy handlers are supported so that they are checked (and thus loaded) only if no previous handler has
 * been able to satisfy the request.
 * 
 * @author Hal Hildebrand
 * @author Costin Leau
 * 
 */
public class NamespacePlugins implements NamespaceHandlerResolver, EntityResolver, DisposableBean {

	/**
	 * Wrapper class which implements both {@link EntityResolver} and {@link NamespaceHandlerResolver} interfaces.
	 * 
	 * Simply delegates to the actual implementation discovered in a specific bundle.
	 */
	private static class Plugin implements NamespaceHandlerResolver, EntityResolver {

		private final NamespaceHandlerResolver namespace;

		private final EntityResolver entity;

		private final Bundle bundle;

		private Plugin(Bundle bundle) {
			this.bundle = bundle;

			ClassLoader loader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle);

			entity = new DelegatingEntityResolver(loader);
			namespace = new DefaultNamespaceHandlerResolver(loader);
		}

		public NamespaceHandler resolve(String namespaceUri) {
			return namespace.resolve(namespaceUri);
		}

		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			return entity.resolveEntity(publicId, systemId);
		}

		public Bundle getBundle() {
			return bundle;
		}
	}

	private static final Log log = LogFactory.getLog(NamespacePlugins.class);

	final LazyBundleRegistry.Condition condition = new LazyBundleRegistry.Condition() {

		private final String NS_HANDLER_RESOLVER_CLASS_NAME = NamespaceHandlerResolver.class.getName();

		public boolean pass(Bundle bundle) {
			try {
				Class<?> type = bundle.loadClass(NS_HANDLER_RESOLVER_CLASS_NAME);
				return NamespaceHandlerResolver.class.equals(type);
			} catch (Throwable th) {
				// if the interface is not wired, ignore the bundle
				log.warn("Bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle) + " cannot see class ["
						+ NS_HANDLER_RESOLVER_CLASS_NAME + "]; ignoring it as a namespace resolver");

				return false;
			}
		}
	};

	private final LazyBundleRegistry.Activator<Plugin> activation = new LazyBundleRegistry.Activator<Plugin>() {

		public Plugin activate(Bundle bundle) {
			return new Plugin(bundle);
		}
	};

	private final LazyBundleRegistry<Plugin> pluginRegistry =
			new LazyBundleRegistry<Plugin>(condition, activation, log);

	/**
	 * Adds a bundle as a handler to plugin registry.
	 * 
	 * @param bundle
	 * @param lazyBundle
	 */
	void addPlugin(Bundle bundle, boolean lazyBundle, boolean applyCondition) {
		boolean debug = log.isDebugEnabled();

		if (debug)
			log.debug("Adding as " + (lazyBundle ? "lazy " : "") + "namespace handler bundle "
					+ OsgiStringUtils.nullSafeNameAndSymName(bundle));

		pluginRegistry.add(bundle, lazyBundle, applyCondition);
	}

	/**
	 * Checks the type compatibility check between the namespace parser wired to Spring DM and the discovered bundle
	 * class space.
	 * 
	 * @param bundle handler bundle
	 * @return true if there is type compatibility, false otherwise
	 */
	boolean isTypeCompatible(Bundle bundle) {
		return condition.pass(bundle);
	}

	/**
	 * Returns true if a handler mapping was removed for the given bundle.
	 * 
	 * @param bundle bundle to look at
	 * @return true if the bundle was used in the plugin map
	 */
	boolean removePlugin(Bundle bundle) {
		if (log.isDebugEnabled())
			log.debug("Removing handler " + OsgiStringUtils.nullSafeNameAndSymName(bundle));

		return pluginRegistry.remove(bundle);
	}

	public NamespaceHandler resolve(final String namespaceUri) {
		return doResolve(namespaceUri);
	}

	public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
		try {
			return doResolveEntity(publicId, systemId);
		} catch (Exception ex) {
			handleInputSourceException(ex);
		}

		return null;
	}

	private NamespaceHandler doResolve(final String namespaceUri) {
		final boolean debug = log.isDebugEnabled();
		final boolean trace = log.isTraceEnabled();

		if (debug)
			log.debug("Trying to resolving namespace handler for " + namespaceUri);

		try {
			return pluginRegistry.apply(new LazyBundleRegistry.Operation<Plugin, NamespaceHandler>() {

				public NamespaceHandler operate(Plugin plugin) {
					try {
						NamespaceHandler handler = plugin.resolve(namespaceUri);
						if (handler != null) {
							if (debug)
								log.debug("Namespace handler for " + namespaceUri + " found inside bundle "
										+ OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()));

							return handler;
						} else if (trace)
							log.trace("Namespace handler for " + namespaceUri + " not found inside bundle "
									+ OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()));

					} catch (IllegalArgumentException ex) {
						if (trace)
							log.trace("Namespace handler for " + namespaceUri + " not found inside bundle "
									+ OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()));
					}
					return null;
				}
			});
		} catch (Exception ex) {
			// the inner method doesn't declare any exceptions so the cast should be safe
			throw (RuntimeException) ex;
		}
	}

	private InputSource doResolveEntity(final String publicId, final String systemId) throws Exception {
		final boolean debug = log.isDebugEnabled();
		final boolean trace = log.isTraceEnabled();

		if (debug)
			log.debug("Trying to resolving entity for " + publicId + "|" + systemId);

		if (systemId != null) {

			return pluginRegistry.apply(new LazyBundleRegistry.Operation<Plugin, InputSource>() {

				public InputSource operate(Plugin plugin) throws SAXException, IOException {
					try {
						InputSource inputSource = plugin.resolveEntity(publicId, systemId);

						if (inputSource != null) {
							if (debug)
								log.debug("XML schema for " + publicId + "|" + systemId + " found inside bundle "
										+ OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()));
							return inputSource;
						}

					} catch (FileNotFoundException ex) {
						if (trace)
							log.trace("XML schema for " + publicId + "|" + systemId + " not found inside bundle "
									+ OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()), ex);
					}
					return null;
				}
			});
		}
		return null;
	}

	private void handleInputSourceException(Exception exception) throws SAXException, IOException {
		if (exception instanceof RuntimeException) {
			throw (RuntimeException) exception;
		}
		if (exception instanceof IOException) {
			throw (IOException) exception;
		}
		throw (SAXException) exception;
	}

	public void destroy() {
		pluginRegistry.clear();
	}
}