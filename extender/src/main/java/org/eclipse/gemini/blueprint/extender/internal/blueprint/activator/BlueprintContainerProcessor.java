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

package org.eclipse.gemini.blueprint.extender.internal.blueprint.activator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextFailedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextRefreshedEvent;
import org.eclipse.gemini.blueprint.extender.event.BootstrappingDependenciesEvent;
import org.eclipse.gemini.blueprint.extender.event.BootstrappingDependenciesFailedEvent;
import org.eclipse.gemini.blueprint.extender.internal.activator.OsgiContextProcessor;
import org.eclipse.gemini.blueprint.extender.internal.blueprint.event.EventAdminDispatcher;
import org.eclipse.gemini.blueprint.service.importer.event.OsgiServiceDependencyWaitStartingEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ClassUtils;

import java.util.Collection;

/**
 * Blueprint specific context processor.
 * 
 * @author Costin Leau
 */
public class BlueprintContainerProcessor implements
		OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent>, OsgiContextProcessor {

	/** logger */
	private static final Log log = LogFactory.getLog(BlueprintContainerProcessor.class);

	private final EventAdminDispatcher dispatcher;
	private final BlueprintListenerManager listenerManager;
	private final Bundle extenderBundle;
	private final BeanFactoryPostProcessor cycleBreaker;

	class BlueprintWaitingEventDispatcher implements ApplicationListener<ApplicationEvent> {
		private final BundleContext bundleContext;
		private volatile boolean enabled = true;
		private volatile boolean initialized = false;

		BlueprintWaitingEventDispatcher(BundleContext context) {
			this.bundleContext = context;
		}

		// WAITING event
		public void onApplicationEvent(ApplicationEvent event) {
			if (event instanceof ContextClosedEvent) {
				enabled = false;
				return;
			}

			if (event instanceof ContextRefreshedEvent) {
				initialized = true;
				return;
			}

			if (event instanceof OsgiServiceDependencyWaitStartingEvent) {
				if (enabled) {
					OsgiServiceDependencyWaitStartingEvent evt = (OsgiServiceDependencyWaitStartingEvent) event;
					String[] filter = new String[] { evt.getServiceDependency().getServiceFilter().toString() };
					BlueprintEvent waitingEvent =
							new BlueprintEvent(BlueprintEvent.WAITING, bundleContext.getBundle(), extenderBundle,
									filter);

					listenerManager.blueprintEvent(waitingEvent);
					dispatcher.waiting(waitingEvent);
				}
				return;
			}
		}
	};

	public BlueprintContainerProcessor(EventAdminDispatcher dispatcher, BlueprintListenerManager listenerManager,
			Bundle extenderBundle) {
		this.dispatcher = dispatcher;
		this.listenerManager = listenerManager;
		this.extenderBundle = extenderBundle;

		Class<?> processorClass =
				ClassUtils.resolveClassName(
						"org.eclipse.gemini.blueprint.blueprint.container.support.internal.config.CycleOrderingProcessor",
						BundleContextAware.class.getClassLoader());

		cycleBreaker = (BeanFactoryPostProcessor) BeanUtils.instantiate(processorClass);
	}

	public void postProcessClose(ConfigurableOsgiBundleApplicationContext context) {
		BlueprintEvent destroyedEvent =
				new BlueprintEvent(BlueprintEvent.DESTROYED, context.getBundle(), extenderBundle);

		listenerManager.blueprintEvent(destroyedEvent);
		dispatcher.afterClose(destroyedEvent);
	}

	public void postProcessRefresh(ConfigurableOsgiBundleApplicationContext context) {
		BlueprintEvent createdEvent = new BlueprintEvent(BlueprintEvent.CREATED, context.getBundle(), extenderBundle);

		listenerManager.blueprintEvent(createdEvent);
		dispatcher.afterRefresh(createdEvent);
	}

	public void postProcessRefreshFailure(ConfigurableOsgiBundleApplicationContext context, Throwable th) {
		BlueprintEvent failureEvent =
				new BlueprintEvent(BlueprintEvent.FAILURE, context.getBundle(), extenderBundle, th);

		listenerManager.blueprintEvent(failureEvent);
		dispatcher.refreshFailure(failureEvent);
	}

	public void preProcessClose(ConfigurableOsgiBundleApplicationContext context) {
		BlueprintEvent destroyingEvent =
				new BlueprintEvent(BlueprintEvent.DESTROYING, context.getBundle(), extenderBundle);

		listenerManager.blueprintEvent(destroyingEvent);
		dispatcher.beforeClose(destroyingEvent);
	}

	/**
	 * Adds infrastructure to Blueprint bundles that has not already been registered in
	 * {@link org.eclipse.gemini.blueprint.context.support.AbstractOsgiBundleApplicationContext#postProcessBeanFactory(ConfigurableListableBeanFactory)}
	 */
	public void preProcessRefresh(final ConfigurableOsgiBundleApplicationContext context) {

		// 1. add event listeners
		// add waiting event broadcaster
		context.addApplicationListener(new BlueprintWaitingEventDispatcher(context.getBundleContext()));

		// 2. add cycle breaker
		context.addBeanFactoryPostProcessor(cycleBreaker);

		BlueprintEvent creatingEvent = new BlueprintEvent(BlueprintEvent.CREATING, context.getBundle(), extenderBundle);
		listenerManager.blueprintEvent(creatingEvent);
		dispatcher.beforeRefresh(creatingEvent);
	}

	public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent evt) {

		// grace event
		if (evt instanceof BootstrappingDependenciesEvent) {
			BootstrappingDependenciesEvent event = (BootstrappingDependenciesEvent) evt;
			Collection<String> flts = event.getDependencyFilters();
			if (flts.isEmpty()) {
				if (log.isDebugEnabled()) {
					log.debug("All dependencies satisfied, not sending Blueprint GRACE event "
							+ "with emtpy dependencies from " + event);
				}
			} else {
				String[] filters = flts.toArray(new String[flts.size()]);
				BlueprintEvent graceEvent =
						new BlueprintEvent(BlueprintEvent.GRACE_PERIOD, evt.getBundle(), extenderBundle, filters);
				listenerManager.blueprintEvent(graceEvent);
				dispatcher.grace(graceEvent);
			}

			return;
		}

		// bootstrapping failure
		if (evt instanceof BootstrappingDependenciesFailedEvent) {
			BootstrappingDependenciesFailedEvent event = (BootstrappingDependenciesFailedEvent) evt;
			Collection<String> flts = event.getDependencyFilters();
			String[] filters = flts.toArray(new String[flts.size()]);
			BlueprintEvent failureEvent =
					new BlueprintEvent(BlueprintEvent.FAILURE, evt.getBundle(), extenderBundle, filters, event
							.getFailureCause());
			listenerManager.blueprintEvent(failureEvent);
			dispatcher.refreshFailure(failureEvent);
			return;
		}

		// created
		if (evt instanceof OsgiBundleContextRefreshedEvent) {
			postProcessRefresh((ConfigurableOsgiBundleApplicationContext) evt.getApplicationContext());
			return;
		}

		// failure
		if (evt instanceof OsgiBundleContextFailedEvent) {
			OsgiBundleContextFailedEvent failureEvent = (OsgiBundleContextFailedEvent) evt;
			postProcessRefreshFailure(
					((ConfigurableOsgiBundleApplicationContext) failureEvent.getApplicationContext()), failureEvent
							.getFailureCause());
			return;
		}
	}
}