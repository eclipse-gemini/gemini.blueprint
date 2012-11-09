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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEventMulticaster;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEventMulticasterAdapter;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.extender.OsgiApplicationContextCreator;
import org.eclipse.gemini.blueprint.extender.OsgiBeanFactoryPostProcessor;
import org.eclipse.gemini.blueprint.extender.OsgiServiceDependencyFactory;
import org.eclipse.gemini.blueprint.extender.internal.dependencies.startup.MandatoryImporterDependencyFactory;
import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.util.BundleDelegatingClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.timer.TimerTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * Configuration class for the extender. Takes care of locating the extender specific configurations and merging the
 * results with the defaults.
 * 
 * @author Costin Leau
 */
public class ExtenderConfiguration implements BundleActivator {

	/** logger */
    protected final Log log = LogFactory.getLog(getClass());

    private static final String TASK_EXECUTOR_NAME = "taskExecutor";

	private static final String SHUTDOWN_TASK_EXECUTOR_NAME = "shutdownTaskExecutor";

	private static final String CONTEXT_CREATOR_NAME = "applicationContextCreator";

	private static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "osgiApplicationEventMulticaster";

	private static final String CONTEXT_LISTENER_NAME = "osgiApplicationContextListener";

	private static final String PROPERTIES_NAME = "extenderProperties";

	private static final String SHUTDOWN_WAIT_KEY = "shutdown.wait.time";

	private static final String PROCESS_ANNOTATIONS_KEY = "process.annotations";

	private static final String WAIT_FOR_DEPS_TIMEOUT_KEY = "dependencies.wait.time";

	private static final String EXTENDER_CFG_LOCATION = "META-INF/spring/extender";

	private static final String XML_PATTERN = "*.xml";

	private static final String ANNOTATION_DEPENDENCY_FACTORY =
			"org.eclipse.gemini.blueprint.extensions.annotation.ServiceReferenceDependencyBeanFactoryPostProcessor";

	/** annotation processing system property (kept for backwards compatibility) */
	private static final String AUTO_ANNOTATION_PROCESSING =
			"org.eclipse.gemini.blueprint.extender.annotation.auto.processing";

	//
	// defaults
	//

	// default dependency wait time (in milliseconds)
	private static final long DEFAULT_DEP_WAIT = ConfigUtils.DIRECTIVE_TIMEOUT_DEFAULT * 1000;
	private static final boolean DEFAULT_NS_BUNDLE_STATE = true;
	private static final long DEFAULT_SHUTDOWN_WAIT = 10 * 1000;
	private static final boolean DEFAULT_PROCESS_ANNOTATION = false;

	private ConfigurableOsgiBundleApplicationContext extenderConfiguration;

	private TaskExecutor taskExecutor, shutdownTaskExecutor;

	private boolean isTaskExecutorManagedInternally;

	private boolean isShutdownTaskExecutorManagedInternally;

	private boolean isMulticasterManagedInternally;

	private long shutdownWaitTime, dependencyWaitTime;

	private boolean processAnnotation, nsBundledResolved;

	private OsgiBundleApplicationContextEventMulticaster eventMulticaster;

	private OsgiBundleApplicationContextListener contextEventListener;

	private boolean forceThreadShutdown;

	private OsgiApplicationContextCreator contextCreator = null;

	/** bundle wrapped class loader */
	private ClassLoader classLoader;
	/** List of context post processors */
	private final List<OsgiBeanFactoryPostProcessor> postProcessors =
			Collections.synchronizedList(new ArrayList<OsgiBeanFactoryPostProcessor>(0));
	/** List of service dependency factories */
	private final List<OsgiServiceDependencyFactory> dependencyFactories =
			Collections.synchronizedList(new ArrayList<OsgiServiceDependencyFactory>(0));

	// fields reading/writing lock
	private final Object lock = new Object();

	/**
	 * Constructs a new <code>ExtenderConfiguration</code> instance. Locates the extender configuration, creates an
	 * application context which will returned the extender items.
	 * 
	 * @param extenderBundleContext extender OSGi bundle context
	 */
	public void start(BundleContext extenderBundleContext) {
		Bundle bundle = extenderBundleContext.getBundle();
		Properties properties = new Properties(createDefaultProperties());

		Enumeration<?> enm = bundle.findEntries(EXTENDER_CFG_LOCATION, XML_PATTERN, false);

		if (enm == null) {
			log.info("No custom extender configuration detected; using defaults...");
			synchronized (lock) {
				taskExecutor = createDefaultTaskExecutor();
				shutdownTaskExecutor = createDefaultShutdownTaskExecutor();
				eventMulticaster = createDefaultEventMulticaster();
				contextEventListener = createDefaultApplicationContextListener();
			}
			classLoader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle);
		} else {
			String[] configs = copyEnumerationToList(enm);

			log.info("Detected extender custom configurations at " + ObjectUtils.nullSafeToString(configs));
			// create OSGi specific XML context
			ConfigurableOsgiBundleApplicationContext extenderAppCtx = new OsgiBundleXmlApplicationContext(configs);
			extenderAppCtx.setBundleContext(extenderBundleContext);
			extenderAppCtx.refresh();

			synchronized (lock) {
				extenderConfiguration = extenderAppCtx;
				// initialize beans
				taskExecutor =
						extenderConfiguration.containsBean(TASK_EXECUTOR_NAME) ? (TaskExecutor) extenderConfiguration
								.getBean(TASK_EXECUTOR_NAME, TaskExecutor.class) : createDefaultTaskExecutor();

				shutdownTaskExecutor =
						extenderConfiguration.containsBean(SHUTDOWN_TASK_EXECUTOR_NAME) ? (TaskExecutor) extenderConfiguration
								.getBean(SHUTDOWN_TASK_EXECUTOR_NAME, TaskExecutor.class)
								: createDefaultShutdownTaskExecutor();

				eventMulticaster =
						extenderConfiguration.containsBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME) ? (OsgiBundleApplicationContextEventMulticaster) extenderConfiguration
								.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
										OsgiBundleApplicationContextEventMulticaster.class)
								: createDefaultEventMulticaster();

				contextCreator =
						extenderConfiguration.containsBean(CONTEXT_CREATOR_NAME) ? (OsgiApplicationContextCreator) extenderConfiguration
								.getBean(CONTEXT_CREATOR_NAME, OsgiApplicationContextCreator.class)
								: null;

				contextEventListener =
						extenderConfiguration.containsBean(CONTEXT_LISTENER_NAME) ? (OsgiBundleApplicationContextListener) extenderConfiguration
								.getBean(CONTEXT_LISTENER_NAME, OsgiBundleApplicationContextListener.class)
								: createDefaultApplicationContextListener();
			}

			// get post processors
			postProcessors.addAll(extenderConfiguration.getBeansOfType(OsgiBeanFactoryPostProcessor.class).values());

			// get dependency factories
			dependencyFactories.addAll(extenderConfiguration.getBeansOfType(OsgiServiceDependencyFactory.class)
					.values());

			classLoader = extenderConfiguration.getClassLoader();
			// extender properties using the defaults as backup
			if (extenderConfiguration.containsBean(PROPERTIES_NAME)) {
				Properties customProperties =
						(Properties) extenderConfiguration.getBean(PROPERTIES_NAME, Properties.class);
				Enumeration<?> propertyKey = customProperties.propertyNames();
				while (propertyKey.hasMoreElements()) {
					String property = (String) propertyKey.nextElement();
					properties.setProperty(property, customProperties.getProperty(property));
				}
			}
		}

		synchronized (lock) {
			shutdownWaitTime = getShutdownWaitTime(properties);
			dependencyWaitTime = getDependencyWaitTime(properties);
			processAnnotation = getProcessAnnotations(properties);
		}

		// load default dependency factories
		addDefaultDependencyFactories();

		// allow post processing
		contextCreator = postProcess(contextCreator);
	}

	/**
	 * Allows post processing of the context creator.
	 * 
	 * @param contextCreator
	 * @return
	 */
	protected OsgiApplicationContextCreator postProcess(OsgiApplicationContextCreator contextCreator) {
		return contextCreator;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Cleanup the configuration items.
	 */
	public void stop(BundleContext extenderBundleContext) {

		synchronized (lock) {
			if (isMulticasterManagedInternally) {
				eventMulticaster.removeAllListeners();
				eventMulticaster = null;
			}

			if (extenderConfiguration != null) {
				extenderConfiguration.close();
				extenderConfiguration = null;
			}

			// postpone the task executor shutdown
			if (forceThreadShutdown) {

				if (isTaskExecutorManagedInternally) {
					log.warn("Forcing the (internally created) taskExecutor to stop...");
					ThreadGroup th = ((SimpleAsyncTaskExecutor) taskExecutor).getThreadGroup();
					if (!th.isDestroyed()) {
						// ask the threads nicely to stop
						th.interrupt();
					}
				}
				taskExecutor = null;
			}

			if (isShutdownTaskExecutorManagedInternally) {
				try {
					((DisposableBean) shutdownTaskExecutor).destroy();
				} catch (Exception ex) {
					log.debug("Received exception while shutting down shutdown task executor", ex);
				}
				shutdownTaskExecutor = null;
			}
		}
	}

	/**
	 * Copies the URLs returned by the given enumeration and returns them as an array of Strings for consumption by the
	 * application context.
	 * 
	 * @param enm
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String[] copyEnumerationToList(Enumeration<?> enm) {
		List<String> urls = new ArrayList<String>(4);
		while (enm != null && enm.hasMoreElements()) {
			URL configURL = (URL) enm.nextElement();
			if (configURL != null) {
				String configURLAsString = configURL.toExternalForm();
				try {
					urls.add(URLDecoder.decode(configURLAsString, "UTF8"));
				} catch (UnsupportedEncodingException uee) {
					log.warn("UTF8 encoding not supported, using the platform default");
					urls.add(URLDecoder.decode(configURLAsString));
				}
			}
		}

		return (String[]) urls.toArray(new String[urls.size()]);
	}

	private Properties createDefaultProperties() {
		Properties properties = new Properties();
		properties.setProperty(SHUTDOWN_WAIT_KEY, "" + DEFAULT_SHUTDOWN_WAIT);
		properties.setProperty(PROCESS_ANNOTATIONS_KEY, "" + DEFAULT_PROCESS_ANNOTATION);
		properties.setProperty(WAIT_FOR_DEPS_TIMEOUT_KEY, "" + DEFAULT_DEP_WAIT);

		return properties;
	}

	protected void addDefaultDependencyFactories() {
		boolean debug = log.isDebugEnabled();

		// default JDK 1.4 processor
		dependencyFactories.add(0, new MandatoryImporterDependencyFactory());

		// load through reflection the dependency and injection processors if running on JDK 1.5 and annotation
		// processing is enabled
		if (processAnnotation) {
			// dependency processor
			Class<?> annotationProcessor = null;
			try {
				annotationProcessor =
						Class.forName(ANNOTATION_DEPENDENCY_FACTORY, false, ExtenderConfiguration.class
								.getClassLoader());
			} catch (ClassNotFoundException cnfe) {
				log.warn("Spring DM annotation package not found, annotation processing disabled.");
				log.debug("Spring DM annotation package not found, annotation processing disabled.", cnfe);
				return;
			}
			Object processor = BeanUtils.instantiateClass(annotationProcessor);
			Assert.isInstanceOf(OsgiServiceDependencyFactory.class, processor);
			dependencyFactories.add(1, (OsgiServiceDependencyFactory) processor);

			if (debug)
				log.debug("Succesfully loaded annotation dependency processor [" + ANNOTATION_DEPENDENCY_FACTORY + "]");

			// add injection processor (first in line)
			postProcessors.add(0, new OsgiAnnotationPostProcessor());
			log.info("Spring-DM annotation processing enabled");
		} else {
			if (debug) {
				log.debug("Spring-DM annotation processing disabled; [" + ANNOTATION_DEPENDENCY_FACTORY
						+ "] not loaded");
			}
		}

	}

	private TaskExecutor createDefaultTaskExecutor() {
		// create thread-pool for starting contexts
		ThreadGroup threadGroup =
				new ThreadGroup("eclipse-gemini-blueprint-extender[" + ObjectUtils.getIdentityHexString(this) + "]-threads");
		threadGroup.setDaemon(false);

		SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
		taskExecutor.setThreadGroup(threadGroup);
		taskExecutor.setThreadNamePrefix("EclipseGeminiBlueprintExtenderThread-");

		isTaskExecutorManagedInternally = true;

		return taskExecutor;
	}

	private TaskExecutor createDefaultShutdownTaskExecutor() {
		TimerTaskExecutor taskExecutor = new TimerTaskExecutor() {
			@Override
			protected Timer createTimer() {
				return new Timer("Gemini Blueprint context shutdown thread", true);
			}
		};

		taskExecutor.afterPropertiesSet();
		isShutdownTaskExecutorManagedInternally = true;
		return taskExecutor;
	}

	private OsgiBundleApplicationContextEventMulticaster createDefaultEventMulticaster() {
		isMulticasterManagedInternally = true;
		return new OsgiBundleApplicationContextEventMulticasterAdapter(new SimpleApplicationEventMulticaster());
	}

	private OsgiBundleApplicationContextListener createDefaultApplicationContextListener() {
		return new DefaultOsgiBundleApplicationContextListener(log);
	}

	private long getShutdownWaitTime(Properties properties) {
		return Long.parseLong(properties.getProperty(SHUTDOWN_WAIT_KEY));
	}

	private long getDependencyWaitTime(Properties properties) {
		return Long.parseLong(properties.getProperty(WAIT_FOR_DEPS_TIMEOUT_KEY));
	}

	private boolean getProcessAnnotations(Properties properties) {
		return Boolean.valueOf(properties.getProperty(PROCESS_ANNOTATIONS_KEY)).booleanValue()
				|| Boolean.getBoolean(AUTO_ANNOTATION_PROCESSING);
	}

	/**
	 * Returns the taskExecutor.
	 * 
	 * @return Returns the taskExecutor
	 */
	public TaskExecutor getTaskExecutor() {
		synchronized (lock) {
			return taskExecutor;
		}
	}

	/**
	 * Returns the shutdown task executor.
	 * 
	 * @return Returns the shutdown task executor
	 */
	public TaskExecutor getShutdownTaskExecutor() {
		synchronized (lock) {
			return shutdownTaskExecutor;
		}
	}

	/**
	 * Returns the contextEventListener.
	 * 
	 * @return Returns the contextEventListener
	 */
	public OsgiBundleApplicationContextListener getContextEventListener() {
		synchronized (lock) {
			return contextEventListener;
		}
	}

	/**
	 * Returns the shutdownWaitTime.
	 * 
	 * @return Returns the shutdownWaitTime
	 */
	public long getShutdownWaitTime() {
		synchronized (lock) {
			return shutdownWaitTime;
		}
	}

	/**
	 * Indicates if the process annotation is enabled or not.
	 * 
	 * @return Returns true if the annotation should be processed or not otherwise.
	 */
	public boolean shouldProcessAnnotation() {
		synchronized (lock) {
			return processAnnotation;
		}
	}

	/**
	 * Returns the dependencyWaitTime.
	 * 
	 * @return Returns the dependencyWaitTime
	 */
	public long getDependencyWaitTime() {
		synchronized (lock) {
			return dependencyWaitTime;
		}
	}

	/**
	 * Returns the eventMulticaster.
	 * 
	 * @return Returns the eventMulticaster
	 */
	public OsgiBundleApplicationContextEventMulticaster getEventMulticaster() {
		synchronized (lock) {
			return eventMulticaster;
		}
	}

	/**
	 * Sets the flag to force the taskExtender to close up in case of runaway threads - this applies *only* if the
	 * taskExecutor has been created internally.
	 * 
	 * <p/> The flag will cause a best attempt to shutdown the threads.
	 * 
	 * @param forceThreadShutdown The forceThreadShutdown to set.
	 */
	public void setForceThreadShutdown(boolean forceThreadShutdown) {
		synchronized (lock) {
			this.forceThreadShutdown = forceThreadShutdown;
		}
	}

	/**
	 * Returns the contextCreator.
	 * 
	 * @return Returns the contextCreator
	 */
	public OsgiApplicationContextCreator getContextCreator() {
		synchronized (lock) {
			return contextCreator;
		}
	}

	/**
	 * Returns the postProcessors.
	 * 
	 * @return Returns the postProcessors
	 */
	public List<OsgiBeanFactoryPostProcessor> getPostProcessors() {
		return postProcessors;
	}

	/**
	 * Returns the class loader wrapped around the extender bundle.
	 * 
	 * @return extender bundle class loader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * Returns the dependencies factories declared by the extender configuration. The list automatically contains the
	 * default listeners (such as the annotation one).
	 * 
	 * @return list of dependency factories
	 */
	public List<OsgiServiceDependencyFactory> getDependencyFactories() {
		return dependencyFactories;
	}
}