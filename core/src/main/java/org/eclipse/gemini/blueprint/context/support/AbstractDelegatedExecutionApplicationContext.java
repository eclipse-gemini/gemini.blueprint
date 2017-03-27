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

package org.eclipse.gemini.blueprint.context.support;

import org.eclipse.gemini.blueprint.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.DependencyAwareBeanFactoryPostProcessor;
import org.eclipse.gemini.blueprint.context.DependencyInitializationAwareBeanPostProcessor;
import org.eclipse.gemini.blueprint.context.OsgiBundleApplicationContextExecutor;
import org.eclipse.gemini.blueprint.context.event.*;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.eclipse.gemini.blueprint.util.internal.PrivilegedUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.*;

/**
 * OSGi-specific application context that delegates the execution of its life cycle methods to a different class. The
 * main reason behind this is to <em>break</em> the startup of the application context in steps that can be executed
 * asynchronously. <p/> <p/> <p/> The {@link #refresh()} and {@link #close()} methods delegate their execution to an
 * {@link OsgiBundleApplicationContextExecutor} class that chooses how to call the lifecycle methods. <p/> <p/> <p/> One
 * can still call the 'traditional' lifecycle methods through {@link #normalRefresh()} and {@link #normalClose()}.
 * 
 * @author Costin Leau
 * @author Olaf Otto
 *
 * @see DelegatedExecutionOsgiBundleApplicationContext
 */
public abstract class AbstractDelegatedExecutionApplicationContext extends AbstractOsgiBundleApplicationContext
		implements DelegatedExecutionOsgiBundleApplicationContext {

	/**
	 * Executor that offers the traditional way of <code>refreshing</code>/ <code>closing</code> of an
	 * ApplicationContext (no conditions have to be met and the refresh happens in only one step).
	 * 
	 * @author Costin Leau
	 */
	private static class NoDependenciesWaitRefreshExecutor implements OsgiBundleApplicationContextExecutor {

		private final DelegatedExecutionOsgiBundleApplicationContext context;

		private NoDependenciesWaitRefreshExecutor(DelegatedExecutionOsgiBundleApplicationContext ctx) {
			context = ctx;
		}

		public void refresh() throws BeansException, IllegalStateException {
			context.normalRefresh();
		}

		public void close() {
			context.normalClose();
		}
	}

	/**
	 * BeanPostProcessor that logs an info message when a bean is created during BeanPostProcessor instantiation, i.e.
	 * when a bean is not eligible for getting processed by all BeanPostProcessors.
	 */
	private class BeanPostProcessorChecker implements BeanPostProcessor {

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor)
					&& this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' is not eligible for getting processed by all "
							+ "BeanPostProcessors (for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}
	}

	/**
	 * Default executor
	 */
	private OsgiBundleApplicationContextExecutor executor = new NoDependenciesWaitRefreshExecutor(this);

	/**
	 * monitor used during refresh/close
	 */
	private final Object startupShutdownMonitor = new Object();

	/**
	 * Delegated multicaster
	 */
	private OsgiBundleApplicationContextEventMulticaster delegatedMulticaster;

	private ContextClassLoaderProvider cclProvider;

	/**
	 * Constructs a new <code>AbstractDelegatedExecutionApplicationContext</code> instance.
	 */
	public AbstractDelegatedExecutionApplicationContext() {
		super();
	}

	/**
	 * Constructs a new <code>AbstractDelegatedExecutionApplicationContext</code> instance.
	 * 
	 * @param parent parent application context
	 */
	public AbstractDelegatedExecutionApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	/**
	 * Delegate execution of refresh method to a third party. This allows breaking the refresh process into several
	 * small pieces providing continuation-like behaviour or completion of the refresh method on several threads, in a
	 * asynch manner. <p/> By default, the refresh method in executed in <em>one go</em> (normal behaviour). <p/>
	 * {@inheritDoc}
	 */
	public void refresh() throws BeansException, IllegalStateException {
		executor.refresh();
	}

	public void normalRefresh() {
		Assert.notNull(getBundleContext(), "bundle context should be set before refreshing the application context");

		try {
			PrivilegedUtils.executeWithCustomTCCL(contextClassLoaderProvider().getContextClassLoader(),
					new PrivilegedUtils.UnprivilegedExecution() {

						public Object run() {
							AbstractDelegatedExecutionApplicationContext.super.refresh();
							sendRefreshedEvent();
							return null;
						}
					});
		} catch (Throwable th) {
			if (logger.isDebugEnabled()) {
				logger.debug("Refresh error", th);
			}
			sendFailedEvent(th);
			// propagate exception to the caller
			// rethrow the problem w/o rewrapping
			if (th instanceof RuntimeException) {
				throw (RuntimeException) th;
			} else {
				throw (Error) th;
			}
		}
	}

	public void normalClose() {
		try {
			PrivilegedUtils.executeWithCustomTCCL(contextClassLoaderProvider().getContextClassLoader(),
					new PrivilegedUtils.UnprivilegedExecution() {

						public Object run() {
							AbstractDelegatedExecutionApplicationContext.super.doClose();
							sendClosedEvent();
							return null;
						}
					});
		} catch (Throwable th) {
			// send failure event
			sendClosedEvent(th);
			// rethrow the problem w/o rewrapping
			if (th instanceof RuntimeException) {
				throw (RuntimeException) th;
			} else {
				throw (Error) th;
			}
		}

	}

	// Adds behaviour for isAvailable flag.
	protected void doClose() {
		executor.close();
	}

	public void startRefresh() {

		try {
			PrivilegedUtils.executeWithCustomTCCL(contextClassLoaderProvider().getContextClassLoader(),
					new PrivilegedUtils.UnprivilegedExecution<Object>() {

						public Object run() {
							synchronized (startupShutdownMonitor) {

								if (ObjectUtils.isEmpty(getConfigLocations())) {
									setConfigLocations(getDefaultConfigLocations());
								}
								if (!OsgiBundleUtils.isBundleActive(getBundle())
										&& !OsgiBundleUtils.isBundleLazyActivated(getBundle())) {
									throw new ApplicationContextException(
											"Unable to refresh application context: bundle is neither active nor lazy-activated but "
													+ OsgiStringUtils.bundleStateAsString(getBundle()));
								}

								ConfigurableListableBeanFactory beanFactory = null;
								// Prepare this context for refreshing.
								prepareRefresh();

								// Tell the subclass to refresh the internal bean
								// factory.
								beanFactory = obtainFreshBeanFactory();

								// Prepare the bean factory for use in this context.
								prepareBeanFactory(beanFactory);

								try {
									// Allows post-processing of the bean factory in
									// context subclasses.
									postProcessBeanFactory(beanFactory);

									// Invoke factory processors registered as beans
									// in the context.
									invokeBeanFactoryPostProcessors(beanFactory);

									// Register bean processors that intercept bean
									// creation.
									registerBeanPostProcessors(beanFactory,
											DependencyInitializationAwareBeanPostProcessor.class, null, false);

									return null;
								} catch (BeansException ex) {
									// Destroy already created singletons to avoid
									// dangling resources.
									beanFactory.destroySingletons();
									cancelRefresh(ex);
									// propagate exception to the caller
									throw ex;
								}
							}
						}
					});
		} catch (Throwable th) {
			if (logger.isDebugEnabled()) {
				logger.debug("Pre refresh error", th);
			}
			// send failure event
			sendFailedEvent(th);
			// rethrow the problem w/o rewrapping
			if (th instanceof RuntimeException) {
				throw (RuntimeException) th;
			} else {
				throw (Error) th;
			}
		}
	}

	public void completeRefresh() {
		try {
			PrivilegedUtils.executeWithCustomTCCL(contextClassLoaderProvider().getContextClassLoader(),
					new PrivilegedUtils.UnprivilegedExecution<Object>() {

						public Object run() {

							synchronized (startupShutdownMonitor) {
								try {
									ConfigurableListableBeanFactory beanFactory = getBeanFactory();

									// Invoke factory processors registered as beans
									// in the context.
									invokeBeanFactoryPostProcessors(beanFactory,
											DependencyAwareBeanFactoryPostProcessor.class, null);

									// Register bean processors that intercept bean
									// creation.
									registerBeanPostProcessors(beanFactory);

									// Initialize message source for this context.
									initMessageSource();

									// Initialize event multicaster for this
									// context.
									initApplicationEventMulticaster();

									// Initialize other special beans in specific
									// context
									// subclasses.
									onRefresh();

									// Check for listener beans and register them.
									registerListeners();

									// Instantiate all remaining (non-lazy-init)
									// singletons.
									finishBeanFactoryInitialization(beanFactory);

									// Last step: publish corresponding event.
									finishRefresh();

									// everything went okay, post notification
									sendRefreshedEvent();
									return null;
								} catch (BeansException ex) {
									// Destroy already created singletons to avoid
									// dangling
									// resources.
									getBeanFactory().destroySingletons();
									cancelRefresh(ex);
									// propagate exception to the caller
									throw ex;
								}
							}
						}
					});
		} catch (Throwable th) {
			if (logger.isDebugEnabled()) {
				logger.debug("Post refresh error", th);
			}
			// post notification
			sendFailedEvent(th);
			// rethrow the problem w/o rewrapping
			if (th instanceof RuntimeException) {
				throw (RuntimeException) th;
			} else {
				throw (Error) th;
			}
		}
	}

	// customized to handle DependencyAwareBeanFactoryPostProcessor classes
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		invokeBeanFactoryPostProcessors(beanFactory, BeanFactoryPostProcessor.class,
				DependencyAwareBeanFactoryPostProcessor.class);
	}

	/**
	 * This version of the original
	 * {@link org.springframework.context.support.PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory, List) post processor invocation}
	 * implementation adds including and excluding processor types to allow for a multi-staged
	 * context initialization, see for instance {{@link #completeRefresh()}}.
	 *
	 * @param beanFactory must not be <code>null</code>
	 * @param include only invoke post processors that are assignment-compatible with this type. Must not be <code>null</code>
	 * @param exclude exclude all post processors that are assignment-compatible with this type. Can be <code>null</code>
	 */
	private void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory,
												 Class<? extends BeanFactoryPostProcessor> include,
												 Class<? extends BeanFactoryPostProcessor> exclude) {
		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<String>();

		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			List<BeanFactoryPostProcessor> regularPostProcessors = new LinkedList<BeanFactoryPostProcessor>();
			List<BeanDefinitionRegistryPostProcessor> registryPostProcessors =
					new LinkedList<BeanDefinitionRegistryPostProcessor>();

			for (BeanFactoryPostProcessor postProcessor : getBeanFactoryPostProcessors()) {
				if (isExcluded(include, exclude, postProcessor)) {
					continue;
				}

				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryPostProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryPostProcessor.postProcessBeanDefinitionRegistry(registry);
					registryPostProcessors.add(registryPostProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}

			if (include.isAssignableFrom(BeanDefinitionRegistryPostProcessor.class)) {
				// Do not initialize FactoryBeans here: We need to leave all regular beans
				// uninitialized to let the bean factory post-processors apply to them!
				// Separate between BeanDefinitionRegistryPostProcessors that implement
				// PriorityOrdered, Ordered, and the rest.
				String[] postProcessorNames =
						beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);

				// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
				List<BeanDefinitionRegistryPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanDefinitionRegistryPostProcessor>();
				for (String ppName : postProcessorNames) {
					if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
						priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
					}
				}
				sortPostProcessors(beanFactory, priorityOrderedPostProcessors);
				registryPostProcessors.addAll(priorityOrderedPostProcessors);
				invokeBeanDefinitionRegistryPostProcessors(priorityOrderedPostProcessors, registry);

				// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				List<BeanDefinitionRegistryPostProcessor> orderedPostProcessors = new ArrayList<BeanDefinitionRegistryPostProcessor>();
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
						orderedPostProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
					}
				}
				sortPostProcessors(beanFactory, orderedPostProcessors);
				registryPostProcessors.addAll(orderedPostProcessors);
				invokeBeanDefinitionRegistryPostProcessors(orderedPostProcessors, registry);

				// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
				boolean reiterate = true;
				while (reiterate) {
					reiterate = false;
					postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
					for (String ppName : postProcessorNames) {
						if (!processedBeans.contains(ppName)) {
							BeanDefinitionRegistryPostProcessor pp = beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class);
							registryPostProcessors.add(pp);
							processedBeans.add(ppName);
							pp.postProcessBeanDefinitionRegistry(registry);
							reiterate = true;
						}
					}
				}

				// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
				invokeBeanFactoryPostProcessors(registryPostProcessors, beanFactory);
			}

			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(getBeanFactoryPostProcessors(), beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(include, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		List<String> orderedPostProcessorNames = new ArrayList<String>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<String>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, include));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(beanFactory, priorityOrderedPostProcessors);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, include));
		}
		sortPostProcessors(beanFactory, orderedPostProcessors);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, include));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	private boolean isExcluded(Class<? extends BeanFactoryPostProcessor> include, Class<? extends BeanFactoryPostProcessor> exclude, BeanFactoryPostProcessor postProcessor) {
		return !include.isInstance(postProcessor) || exclude != null && !exclude.isInstance(postProcessor);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	private static void sortPostProcessors(ConfigurableListableBeanFactory beanFactory, List<?> postProcessors) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		Collections.sort(postProcessors, comparatorToUse);
	}

	// customized to handle DependencyInitializationAwareBeanPostProcessor
	// classes
	protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		registerBeanPostProcessors(beanFactory, BeanPostProcessor.class,
				DependencyInitializationAwareBeanPostProcessor.class, true);
	}

	/**
	 * Instantiate and invoke all registered BeanPostProcessor beans, respecting explicit order if given. <p/> Must be
	 * called before any instantiation of application beans. Very similar to
	 * {@link AbstractApplicationContext#invokeBeanFactoryPostProcessors} but allowing exclusion of a certain type.
	 */
	protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory, Class<?> type,
			Class<?> exclude, boolean check) {
		String[] postProcessorNames = beanFactory.getBeanNamesForType(type, true, false);

		if (check) {
			// Register BeanPostProcessorChecker that logs an info message when
			// a bean is created during BeanPostProcessor instantiation, i.e.
			// when
			// a bean is not eligible for getting processed by all
			// BeanPostProcessors.
			int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
			beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));
		}

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
		List<String> orderedPostProcessorNames = new ArrayList<String>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<String>();
		for (int i = 0; i < postProcessorNames.length; i++) {
			// check exclude type first
			if (exclude == null || !isTypeMatch(postProcessorNames[i], exclude)) {
				if (isTypeMatch(postProcessorNames[i], PriorityOrdered.class)) {
					priorityOrderedPostProcessors.add(beanFactory.getBean(postProcessorNames[i],
							BeanPostProcessor.class));
				} else if (isTypeMatch(postProcessorNames[i], Ordered.class)) {
					orderedPostProcessorNames.add(postProcessorNames[i]);
				} else {
					nonOrderedPostProcessorNames.add(postProcessorNames[i]);
				}
			}
		}

		// First, register the BeanPostProcessors that implement
		// PriorityOrdered.
		Collections.sort(priorityOrderedPostProcessors, new OrderComparator());
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<BeanPostProcessor>();

		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(getBean(postProcessorName, BeanPostProcessor.class));
		}

		Collections.sort(orderedPostProcessors, new OrderComparator());
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Finally, register all other BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(getBean(postProcessorName, BeanPostProcessor.class));
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory,
			List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}

	public void setExecutor(OsgiBundleApplicationContextExecutor executor) {
		this.executor = executor;
	}

	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
	}

	public void setDelegatedEventMulticaster(OsgiBundleApplicationContextEventMulticaster multicaster) {
		this.delegatedMulticaster = multicaster;
	}

	/**
	 * Sets the OSGi multicaster by using a Spring {@link ApplicationEventMulticaster}. This method is added as a
	 * covenience.
	 * 
	 * @param multicaster Spring multi-caster used for propagating OSGi specific events
	 * @see OsgiBundleApplicationContextEventMulticasterAdapter
	 */
	public void setDelegatedEventMulticaster(ApplicationEventMulticaster multicaster) {
		this.delegatedMulticaster = new OsgiBundleApplicationContextEventMulticasterAdapter(multicaster);
	}

	public OsgiBundleApplicationContextEventMulticaster getDelegatedEventMulticaster() {
		return this.delegatedMulticaster;
	}

	private void sendFailedEvent(Throwable cause) {
		if (delegatedMulticaster != null)
			delegatedMulticaster.multicastEvent(new OsgiBundleContextFailedEvent(this, this.getBundle(), cause));
	}

	private void sendRefreshedEvent() {
		if (delegatedMulticaster != null)
			delegatedMulticaster.multicastEvent(new OsgiBundleContextRefreshedEvent(this, this.getBundle()));
	}

	private void sendClosedEvent() {
		if (delegatedMulticaster != null)
			delegatedMulticaster.multicastEvent(new OsgiBundleContextClosedEvent(this, this.getBundle()));
	}

	private void sendClosedEvent(Throwable cause) {
		if (delegatedMulticaster != null)
			delegatedMulticaster.multicastEvent(new OsgiBundleContextClosedEvent(this, this.getBundle(), cause));
	}

	/**
	 * private method used for doing lazy-init-if-not-set for cclProvider
	 */
	private ContextClassLoaderProvider contextClassLoaderProvider() {
		if (cclProvider == null) {
			DefaultContextClassLoaderProvider defaultProvider = new DefaultContextClassLoaderProvider();
			defaultProvider.setBeanClassLoader(getClassLoader());
			cclProvider = defaultProvider;
		}
		return cclProvider;
	}

	/**
	 * Sets the {@link ContextClassLoaderProvider} used by this OSGi application context instance. By default,
	 * {@link DefaultContextClassLoaderProvider} is used.
	 * 
	 * @param contextClassLoaderProvider context class loader provider to use
	 * @see ContextClassLoaderProvider
	 * @see DefaultContextClassLoaderProvider
	 */
	public void setContextClassLoaderProvider(ContextClassLoaderProvider contextClassLoaderProvider) {
		this.cclProvider = contextClassLoaderProvider;
	}
}