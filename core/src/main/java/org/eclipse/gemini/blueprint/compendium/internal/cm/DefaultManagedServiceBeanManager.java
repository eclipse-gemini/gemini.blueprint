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

package org.eclipse.gemini.blueprint.compendium.internal.cm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.support.AbstractBeanFactory;

/**
 * Default implementation for {@link ManagedServiceBeanManager}.
 * 
 * @author Costin Leau
 * 
 */
public class DefaultManagedServiceBeanManager implements DisposableBean, ManagedServiceBeanManager {

	/** logger */
	private static final Log log = LogFactory.getLog(DefaultManagedServiceBeanManager.class);

	private final Map<Integer, Object> instanceRegistry = new ConcurrentHashMap<Integer, Object>(8);
	private final UpdateCallback updateCallback;
	private final ConfigurationAdminManager cam;
	private final AbstractBeanFactory bf;

	public DefaultManagedServiceBeanManager(boolean autowireOnUpdate, String methodName,
			ConfigurationAdminManager cam, BeanFactory beanFactory) {

		updateCallback = CMUtils.createCallback(autowireOnUpdate, methodName, beanFactory);
		bf = (beanFactory instanceof AbstractBeanFactory ? (AbstractBeanFactory) beanFactory : null);
		this.cam = cam;
		this.cam.setBeanManager(this);
	}

	public Object register(Object bean) {
		int hashCode = System.identityHashCode(bean);
		if (log.isTraceEnabled())
			log.trace("Start tracking instance " + bean.getClass().getName() + "@" + hashCode);
		instanceRegistry.put(Integer.valueOf(hashCode), bean);
		applyInitialInjection(bean, cam.getConfiguration());
		return bean;
	}

	void applyInitialInjection(Object instance, Map configuration) {
		if (log.isTraceEnabled())
			log.trace("Applying injection to instance " + instance.getClass() + "@" + System.identityHashCode(instance)
					+ " using map " + configuration);
		CMUtils.applyMapOntoInstance(instance, configuration, bf);
	}

	public void unregister(Object bean) {
		int hashCode = System.identityHashCode(bean);
		if (log.isTraceEnabled())
			log.trace("Stopped tracking instance " + bean.getClass().getName() + "@" + hashCode);

		instanceRegistry.remove(new Integer(hashCode));
	}

	public void updated(Map properties) {
		if (updateCallback != null) {
			CMUtils.bulkUpdate(updateCallback, instanceRegistry.values(), properties);
		}
	}

	public void destroy() {
		// unregister CM services
		cam.destroy();
		// remove the tracked beans
		instanceRegistry.clear();
	}
}