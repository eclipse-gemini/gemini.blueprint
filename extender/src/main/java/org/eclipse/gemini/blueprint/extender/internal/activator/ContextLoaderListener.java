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

package org.eclipse.gemini.blueprint.extender.internal.activator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.extender.OsgiApplicationContextCreator;
import org.eclipse.gemini.blueprint.extender.internal.activator.listeners.BaseListener;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.eclipse.gemini.blueprint.extender.support.DefaultOsgiApplicationContextCreator;
import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.osgi.framework.*;

/**
 * Osgi Extender that bootstraps 'Spring powered bundles'.
 * 
 * <p/> The class listens to bundle events and manages the creation and destruction of application contexts for bundles
 * that have one or both of: <ul> <li>A manifest header entry Spring-Context <li>XML files in META-INF/spring folder
 * </ul>
 * 
 * <p/> The extender also discovers any Spring namespace/schema handlers in resolved bundles and makes them available
 * through a dedicated OSGi service.
 * 
 * <p/> The extender behaviour can be customized by attaching fragments to the extender bundle. On startup, the extender
 * will look for <code>META-INF/spring/*.xml</code> files and merge them into an application context. From the resulting
 * context, the context will look for beans with predefined names to determine its configuration. The current version
 * recognises the following bean names:
 * 
 * <table border="1"> <tr> <th>Bean Name</th> <th>Bean Type</th> <th>Description</th> </tr> <tr>
 * <td><code>taskExecutor</code></td> <td><code>org.springframework.core.task.TaskExecutor</code></td> <td>Task executor
 * used for creating the discovered application contexts.</td> </tr> <tr> <td><code>shutdownTaskExecutor</code></td>
 * <td><code>org.springframework.core.task.TaskExecutor</code></td> <td>Task executor used for shutting down various
 * application contexts.</td> </tr> <tr> <td><code>extenderProperties</code></td>
 * <td><code>java.util.Properties</code></td> <td>Various properties for configuring the extender behaviour (see
 * below)</td> </tr> </table>
 * 
 * <p/> <code>extenderProperties</code> recognises the following properties:
 * 
 * <table border="1"> <tr> <th>Name</th> <th>Type</th> <th>Description</th> </tr> <tr>
 * <td><code>shutdown.wait.time</code></td> <td>Number</td> <td>The amount of time the extender will wait for each
 * application context to shutdown gracefully. Expressed in milliseconds.</td> </tr> <tr>
 * <td><code>process.annotations</code></td> <td>Boolean</td> <td>Whether or not, the extender will process SpringOSGi
 * annotations.</td> </tr> </table>
 * 
 * <p/> Note: The extender configuration context is created during the bundle activation (a synchronous OSGi lifecycle
 * callback) and should contain only simple bean definitions that will not delay context initialisation. </p>
 * 
 * @author Bill Gallagher
 * @author Andy Piper
 * @author Hal Hildebrand
 * @author Adrian Colyer
 * @author Costin Leau
 */
public class ContextLoaderListener implements BundleActivator {

    /**
	 * Bundle listener used for context creation/destruction.
	 */
	private class ContextBundleListener extends BaseListener {

		protected void handleEvent(BundleEvent event) {

			Bundle bundle = event.getBundle();

			// ignore current bundle for context creation
			if (bundle.getBundleId() == bundleId) {
				return;
			}

			switch (event.getType()) {
			case BundleEvent.STARTED: {
				lifecycleManager.maybeCreateApplicationContextFor(bundle);
				break;
			}
			case BundleEvent.STOPPING: {
				if (OsgiBundleUtils.isSystemBundle(bundle)) {
					if (log.isDebugEnabled()) {
						log.debug("System bundle stopping");
					}
					// System bundle is shutting down; Special handling for
					// framework shutdown
					shutdown();
				} else {
					lifecycleManager.maybeCloseApplicationContextFor(bundle);
				}
				break;
			}
			default:
				break;
			}
		}
	}

	protected final Log log = LogFactory.getLog(getClass());

    private ExtenderConfiguration extenderConfiguration;
    private VersionMatcher versionMatcher;
    private Version extenderVersion;

    /** extender bundle id */
	private long bundleId;

	/** The bundle's context */
	private BundleContext bundleContext;

	/** Bundle listener interested in context creation */
	private BaseListener contextListener;

	/**
	 * Monitor used for dealing with the bundle activator and synchronous bundle threads
	 */
	private final Object monitor = new Object();

	/**
	 * flag indicating whether the context is down or not - useful during shutdown
	 */
	private volatile boolean isClosed = false;

	private volatile LifecycleManager lifecycleManager;
	private volatile OsgiContextProcessor processor;

    public ContextLoaderListener(ExtenderConfiguration extenderConfiguration) {
        this.extenderConfiguration = extenderConfiguration;
    }

    /**
	 * <p/> Called by OSGi when this bundle is started. Finds all previously resolved bundles and adds namespace
	 * handlers for them if necessary. </p> <p/> Creates application contexts for bundles started before the extender
	 * was started. </p> <p/> Registers a namespace/entity resolving service for use by web app contexts. </p>
	 * 
	 * @see org.osgi.framework.BundleActivator#start
	 */
	public void start(BundleContext extenderBundleContext) throws Exception {

		this.bundleContext = extenderBundleContext;
		this.bundleId = extenderBundleContext.getBundle().getBundleId();
        this.extenderVersion = OsgiBundleUtils.getBundleVersion(extenderBundleContext.getBundle());
        this.versionMatcher = new DefaultVersionMatcher(getManagedBundleExtenderVersionHeader(), extenderVersion);
        this.processor = createContextProcessor();

		// initialize the configuration once namespace handlers have been detected
		this.lifecycleManager =
				new LifecycleManager(
                        this.extenderConfiguration,
                        getVersionMatcher(),
                        createContextConfigFactory(),
                        getOsgiApplicationContextCreator(),
						this.processor,
                        getTypeCompatibilityChecker(),
                        bundleContext);

		// Step 3: discover the bundles that are started
		// and require context creation
		initStartedBundles(bundleContext);
	}

	protected OsgiContextProcessor createContextProcessor() {
		return new NoOpOsgiContextProcessor();
	}

	protected TypeCompatibilityChecker getTypeCompatibilityChecker() {
		return null;
	}

	protected void initStartedBundles(BundleContext bundleContext) {
		// register the context creation listener
		contextListener = new ContextBundleListener();
		// listen to any changes in bundles
		bundleContext.addBundleListener(contextListener);
		// get the bundles again to get an updated view
		Bundle[] previousBundles = bundleContext.getBundles();

		// Instantiate all previously resolved bundles which are Spring
		// powered
		for (int i = 0; i < previousBundles.length; i++) {
			if (OsgiBundleUtils.isBundleActive(previousBundles[i])) {
				try {
					lifecycleManager.maybeCreateApplicationContextFor(previousBundles[i]);
				} catch (Throwable e) {
					log.warn("Cannot start bundle " + OsgiStringUtils.nullSafeSymbolicName(previousBundles[i])
							+ " due to", e);
				}
			}
		}
	}

	/**
	 * Called by OSGi when this bundled is stopped. Unregister the namespace/entity resolving service and clear all
	 * state. No further management of application contexts created by this extender prior to stopping the bundle occurs
	 * after this point (even if the extender bundle is subsequently restarted).
	 * 
	 * @see org.osgi.framework.BundleActivator#stop
	 */
	public void stop(BundleContext context) throws Exception {
		shutdown();
	}

	/**
	 * Shutdown the extender and all bundled managed by it. Shutdown of contexts is in the topological order of the
	 * dependency graph formed by the service references.
	 */
	protected void shutdown() {
		synchronized (monitor) {
			// if already closed, bail out
			if (isClosed)
				return;
			else
				isClosed = true;
		}

        this.contextListener.close();

		// remove the bundle listeners (we are closing down)
		if (contextListener != null) {
			bundleContext.removeBundleListener(contextListener);
			contextListener = null;
		}

		// close managed bundles
		lifecycleManager.destroy();
	}

	protected ApplicationContextConfigurationFactory createContextConfigFactory() {
		return new DefaultApplicationContextConfigurationFactory();
	}

    public VersionMatcher getVersionMatcher() {
        return versionMatcher;
    }

    protected String getManagedBundleExtenderVersionHeader() {
        return ConfigUtils.EXTENDER_VERSION;
    }

    protected OsgiApplicationContextCreator getOsgiApplicationContextCreator() {
        OsgiApplicationContextCreator creator = this.extenderConfiguration.getContextCreator();
        if (creator == null) {
            creator = createDefaultOsgiApplicationContextCreator();
        }
        return creator;
    }

    protected OsgiApplicationContextCreator createDefaultOsgiApplicationContextCreator() {
        return new DefaultOsgiApplicationContextCreator();
    }
}