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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEventMulticaster;
import org.eclipse.gemini.blueprint.extender.OsgiApplicationContextCreator;
import org.eclipse.gemini.blueprint.extender.OsgiBeanFactoryPostProcessor;
import org.eclipse.gemini.blueprint.extender.internal.dependencies.shutdown.ShutdownSorter;
import org.eclipse.gemini.blueprint.extender.internal.dependencies.startup.DependencyWaiterApplicationContextExecutor;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.eclipse.gemini.blueprint.extender.internal.support.OsgiBeanFactoryPostProcessorAdapter;
import org.eclipse.gemini.blueprint.extender.internal.util.concurrent.Counter;
import org.eclipse.gemini.blueprint.extender.internal.util.concurrent.RunnableTimedExecution;
import org.eclipse.gemini.blueprint.extender.support.ApplicationContextConfiguration;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager handling the startup/shutdown threading issues regarding OSGi contexts. Used by {@link ContextLoaderListener}
 * .
 * 
 * @author Costin Leau
 */
class LifecycleManager implements DisposableBean {

	/** logger */
	private static final Log log = LogFactory.getLog(LifecycleManager.class);

	/**
	 * The contexts we are currently managing. Keys are bundle ids, values are ServiceDependentOsgiApplicationContexts
	 * for the application context
	 */
	private final Map<Long, ConfigurableOsgiBundleApplicationContext> managedContexts =
			new ConcurrentHashMap<Long, ConfigurableOsgiBundleApplicationContext>(16);

	/** listener counter - used to properly synchronize shutdown */
	private Counter contextsStarted = new Counter("contextsStarted");

	// "Spring Application Context Creation Timer"
	private final Timer timer = new Timer("Spring DM Context Creation Timer", true);

	/** Task executor used for bootstraping the Spring contexts in async mode */
	private final TaskExecutor taskExecutor;

	/** ApplicationContext Creator */
	private final OsgiApplicationContextCreator contextCreator;

	/** BFPP list */
	private final List<OsgiBeanFactoryPostProcessor> postProcessors;

	/** shutdown task executor */
	private final TaskExecutor shutdownTaskExecutor;

	/**
	 * Task executor which uses the same thread for running tasks. Used when doing a synchronous wait-for-dependencies.
	 */
	private final TaskExecutor sameThreadTaskExecutor = new SyncTaskExecutor();

	private final OsgiBundleApplicationContextEventMulticaster multicaster;

	private final ExtenderConfiguration extenderConfiguration;

	private final BundleContext bundleContext;

	private final OsgiContextProcessor processor;

	private final ApplicationContextConfigurationFactory contextConfigurationFactory;

	private final VersionMatcher versionMatcher;
	private final TypeCompatibilityChecker typeChecker;

	LifecycleManager(ExtenderConfiguration extenderConfiguration, VersionMatcher versionMatcher,
                     ApplicationContextConfigurationFactory appCtxCfgFactory, OsgiApplicationContextCreator osgiApplicationContextCreator, OsgiContextProcessor processor,
                     TypeCompatibilityChecker checker, BundleContext context) {

		this.versionMatcher = versionMatcher;
		this.extenderConfiguration = extenderConfiguration;
		this.contextConfigurationFactory = appCtxCfgFactory;
        this.contextCreator = osgiApplicationContextCreator;
		this.processor = processor;

		this.taskExecutor = extenderConfiguration.getTaskExecutor();
		this.shutdownTaskExecutor = extenderConfiguration.getShutdownTaskExecutor();

		this.multicaster = extenderConfiguration.getEventMulticaster();

		this.postProcessors = extenderConfiguration.getPostProcessors();
		this.typeChecker = checker;

		this.bundleContext = context;
	}

	/**
	 * Context creation is a potentially long-running activity (certainly more than we want to do on the synchronous
	 * event callback).
	 * 
	 * <p/> Based on our configuration, the context can be started on the same thread or on a different one.
	 * 
	 * <p/> Kick off a background activity to create an application context for the given bundle if needed.
	 * 
	 * <b>Note:</b> Make sure to do the fastest filtering first to avoid slow-downs on platforms with a big number of
	 * plugins and wiring (i.e. Eclipse platform).
	 * 
	 * @param bundle
	 */
	protected void maybeCreateApplicationContextFor(Bundle bundle) {

		boolean debug = log.isDebugEnabled();
		String bundleString = "[" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "]";

		final Long bundleId = new Long(bundle.getBundleId());

		if (managedContexts.containsKey(bundleId)) {
			if (debug) {
				log.debug("Bundle " + bundleString + " is already managed; ignoring...");
			}
			return;
		}

		if (!versionMatcher.matchVersion(bundle)) {
			return;
		}

		BundleContext localBundleContext = OsgiBundleUtils.getBundleContext(bundle);

		// initialize context
		final DelegatedExecutionOsgiBundleApplicationContext localApplicationContext;

		if (debug)
			log.debug("Inspecting bundle " + bundleString);

		try {
			localApplicationContext = contextCreator.createApplicationContext(localBundleContext);
		} catch (Exception ex) {
			log.error("Cannot create application context for bundle " + bundleString, ex);
			return;
		}

		if (localApplicationContext == null) {
			log.debug("No application context created for bundle " + bundleString);
			return;
		}

		if (typeChecker != null) {
			if (!typeChecker.isTypeCompatible(localBundleContext)) {
				log.info("Bundle " + OsgiStringUtils.nullSafeName(bundle) + " is not type compatible with extender "
						+ OsgiStringUtils.nullSafeName(bundleContext.getBundle()) + "; ignoring bundle...");
				return;
			}
		}

		log.debug("Bundle " + OsgiStringUtils.nullSafeName(bundle) + " is type compatible with extender "
				+ OsgiStringUtils.nullSafeName(bundleContext.getBundle()) + "; processing bundle...");

		// create a dedicated hook for this application context
		BeanFactoryPostProcessor processingHook =
				new OsgiBeanFactoryPostProcessorAdapter(localBundleContext, postProcessors);

		// add in the post processors
		localApplicationContext.addBeanFactoryPostProcessor(processingHook);

		// add the context to the tracker
		managedContexts.put(bundleId, localApplicationContext);

		localApplicationContext.setDelegatedEventMulticaster(multicaster);

		ApplicationContextConfiguration config = contextConfigurationFactory.createConfiguration(bundle);

		final boolean asynch = config.isCreateAsynchronously();

		// create refresh runnable
		Runnable contextRefresh = new Runnable() {

			public void run() {
				// post refresh events are caught through events
				if (log.isTraceEnabled()) {
					log.trace("Calling pre-refresh on processor " + processor);
				}
				processor.preProcessRefresh(localApplicationContext);
				localApplicationContext.refresh();
			}
		};

		// executor used for creating the appCtx
		// chosen based on the sync/async configuration
		TaskExecutor executor = null;

		String creationType;

		// synch/asynch context creation
		if (asynch) {
			// for the async stuff use the executor
			executor = taskExecutor;
			creationType = "Asynchronous";
		} else {
			// for the sync stuff, use this thread
			executor = sameThreadTaskExecutor;
			creationType = "Synchronous";
		}

		if (debug) {
			log.debug(creationType + " context creation for bundle " + bundleString);
		}

		// wait/no wait for dependencies behaviour
		if (config.isWaitForDependencies()) {
			DependencyWaiterApplicationContextExecutor appCtxExecutor =
					new DependencyWaiterApplicationContextExecutor(localApplicationContext, !asynch,
							extenderConfiguration.getDependencyFactories());

			long timeout;
			// check whether a timeout has been defined

			if (config.isTimeoutDeclared()) {
				timeout = config.getTimeout();
				if (debug)
					log.debug("Setting bundle-defined, wait-for-dependencies/graceperiod timeout value=" + timeout
							+ " ms, for bundle " + bundleString);

			} else {
				timeout = extenderConfiguration.getDependencyWaitTime();
				if (debug)
					log.debug("Setting globally defined wait-for-dependencies/graceperiod timeout value=" + timeout
							+ " ms, for bundle " + bundleString);
			}

			appCtxExecutor.setTimeout(timeout);
			appCtxExecutor.setWatchdog(timer);
			appCtxExecutor.setTaskExecutor(executor);
			appCtxExecutor.setMonitoringCounter(contextsStarted);
			// set events publisher
			appCtxExecutor.setDelegatedMulticaster(this.multicaster);

			contextsStarted.increment();
		} else {
			// do nothing; by default contexts do not wait for services.
		}

		executor.execute(contextRefresh);
	}

	/**
	 * Closing an application context is a potentially long-running activity, however, we *have* to do it synchronously
	 * during the event process as the BundleContext object is not valid once we return from this method.
	 * 
	 * @param bundle
	 */
	protected void maybeCloseApplicationContextFor(Bundle bundle) {
		final ConfigurableOsgiBundleApplicationContext context =
				(ConfigurableOsgiBundleApplicationContext) managedContexts.remove(Long.valueOf(bundle.getBundleId()));
		if (context == null) {
			return;
		}

		RunnableTimedExecution.execute(new Runnable() {

			private final String toString = "Closing runnable for context " + context.getDisplayName();

			public void run() {
				closeApplicationContext(context);
			}

			public String toString() {
				return toString;
			}

		}, extenderConfiguration.getShutdownWaitTime(), shutdownTaskExecutor);
	}

	/**
	 * Closes an application context. This is a convenience methods that invokes the event notification as well.
	 * 
	 * @param ctx
	 */
	private void closeApplicationContext(ConfigurableOsgiBundleApplicationContext ctx) {
		if (log.isDebugEnabled()) {
			log.debug("Closing application context " + ctx.getDisplayName());
		}

		if (log.isTraceEnabled()) {
			log.trace("Calling pre-close on processor " + processor);
		}
		processor.preProcessClose(ctx);
		try {
			ctx.close();
		} finally {
			if (log.isTraceEnabled()) {
				log.trace("Calling post close on processor " + processor);
			}
			processor.postProcessClose(ctx);
		}
	}

	public void destroy() {
		// first stop the watchdog
		stopTimer();

		// get hold of the needed bundles
		List<Bundle> bundles = new ArrayList<Bundle>(managedContexts.size());

		for (ConfigurableOsgiBundleApplicationContext context : managedContexts.values()) {
			bundles.add(context.getBundle());
		}

		boolean debug = log.isDebugEnabled();

		if (debug) {
			log.debug("Starting shutdown procedure for bundles " + bundles);
		}
		while (!bundles.isEmpty()) {
			Collection<Bundle> candidates = ShutdownSorter.getBundles(bundles);
			if (debug)
				log.debug("Staging shutdown for bundles " + candidates);

			final List<Runnable> taskList = new ArrayList<Runnable>(candidates.size());
			final List<ConfigurableOsgiBundleApplicationContext> closedContexts =
					Collections.synchronizedList(new ArrayList<ConfigurableOsgiBundleApplicationContext>());
			final Object[] contextClosingDown = new Object[1];

			for (Bundle shutdownBundle : candidates) {
				final ConfigurableOsgiBundleApplicationContext context = getManagedContext(shutdownBundle);
				if (context != null) {
					closedContexts.add(context);
					// add a new runnable
					taskList.add(new Runnable() {

						private final String toString = "Closing runnable for context " + context.getDisplayName();

						public void run() {
							contextClosingDown[0] = context;
							// eliminate context
							closedContexts.remove(context);
							closeApplicationContext(context);
						}

						public String toString() {
							return toString;
						}
					});
				}
			}

			// tasks
			final Runnable[] tasks = (Runnable[]) taskList.toArray(new Runnable[taskList.size()]);

			// start the ripper >:)
			for (int j = 0; j < tasks.length; j++) {
				if (RunnableTimedExecution.execute(tasks[j], extenderConfiguration.getShutdownWaitTime(),
						shutdownTaskExecutor)) {
					if (debug) {
						log.debug(contextClosingDown[0] + " context did not close successfully; forcing shutdown...");
					}
				}
			}
		}

		this.managedContexts.clear();

		// before bailing out; wait for the threads that might be left by
		// the task executor
		stopTaskExecutor();
	}

    public ConfigurableOsgiBundleApplicationContext getManagedContext(Bundle bundle) {
        ConfigurableOsgiBundleApplicationContext context = null;
        try {
            Long id = new Long(bundle.getBundleId());
            context = (ConfigurableOsgiBundleApplicationContext) managedContexts.get(id);
        } catch (IllegalStateException _) {
            // ignore
        }
        return context;
    }

	/**
	 * Do some additional waiting so the service dependency listeners detect the shutdown.
	 */
	private void stopTaskExecutor() {
		boolean debug = log.isDebugEnabled();

		if (debug)
			log.debug("Waiting for " + contextsStarted + " service dependency listener(s) to stop...");

		contextsStarted.waitForZero(extenderConfiguration.getShutdownWaitTime());

		if (!contextsStarted.isZero()) {
			if (debug)
				log.debug(contextsStarted.getValue()
						+ " service dependency listener(s) did not responded in time; forcing them to shutdown...");
			extenderConfiguration.setForceThreadShutdown(true);
		}

		else
			log.debug("All listeners closed");
	}

	/**
	 * Cancel any tasks scheduled for the timer.
	 */
	private void stopTimer() {
		if (log.isDebugEnabled())
			log.debug("Canceling timer tasks");
		timer.cancel();
	}
}