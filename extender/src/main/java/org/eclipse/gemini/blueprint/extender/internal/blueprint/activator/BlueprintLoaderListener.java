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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEventMulticaster;
import org.eclipse.gemini.blueprint.extender.internal.activator.ApplicationContextConfigurationFactory;
import org.eclipse.gemini.blueprint.extender.internal.activator.ContextLoaderListener;
import org.eclipse.gemini.blueprint.extender.internal.activator.OsgiContextProcessor;
import org.eclipse.gemini.blueprint.extender.internal.activator.TypeCompatibilityChecker;
import org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.support.BlueprintConfigUtils;
import org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.support.BlueprintContainerConfig;
import org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.support.BlueprintExtenderConfiguration;
import org.eclipse.gemini.blueprint.extender.internal.blueprint.event.EventAdminDispatcher;
import org.eclipse.gemini.blueprint.extender.internal.support.ExtenderConfiguration;
import org.eclipse.gemini.blueprint.extender.support.ApplicationContextConfiguration;

/**
 * RFC124 extension to the Spring DM extender.
 * 
 * @author Costin Leau
 */
public class BlueprintLoaderListener extends ContextLoaderListener {

	private volatile EventAdminDispatcher dispatcher;
	private volatile BlueprintListenerManager listenerManager;
	private volatile Bundle bundle;
	private volatile BlueprintContainerProcessor contextProcessor;
	private volatile TypeCompatibilityChecker typeChecker;

	@Override
	public void start(BundleContext context) throws Exception {
		this.listenerManager = new BlueprintListenerManager(context);
		this.dispatcher = new EventAdminDispatcher(context);
		this.bundle = context.getBundle();
		this.contextProcessor = new BlueprintContainerProcessor(dispatcher, listenerManager, bundle);
		this.typeChecker = new BlueprintTypeCompatibilityChecker(bundle);

		super.start(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		listenerManager.destroy();
	}

	@Override
	protected ExtenderConfiguration initExtenderConfiguration(BundleContext bundleContext) {
		return new BlueprintExtenderConfiguration(bundleContext, log);
	}

	@Override
	protected ApplicationContextConfigurationFactory createContextConfigFactory() {
		return new ApplicationContextConfigurationFactory() {

			public ApplicationContextConfiguration createConfiguration(Bundle bundle) {
				return new BlueprintContainerConfig(bundle);
			}
		};
	}

	@Override
	protected OsgiContextProcessor createContextProcessor() {
		return contextProcessor;
	}

	@Override
	protected TypeCompatibilityChecker getTypeCompatibilityChecker() {
		return typeChecker;
	}

	@Override
	protected String getManagedBundleExtenderVersionHeader() {
		return BlueprintConfigUtils.EXTENDER_VERSION;
	}

	@Override
	protected void addApplicationListener(OsgiBundleApplicationContextEventMulticaster multicaster) {
		super.addApplicationListener(multicaster);
		// monitor bootstrapping events
		multicaster.addApplicationListener(contextProcessor);
	}

	protected ApplicationContextConfiguration createContextConfig(Bundle bundle) {
		return new BlueprintContainerConfig(bundle);
	}
}