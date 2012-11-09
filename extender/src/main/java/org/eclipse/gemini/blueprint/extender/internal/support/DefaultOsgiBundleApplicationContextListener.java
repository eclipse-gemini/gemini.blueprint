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

import org.apache.commons.logging.Log;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextClosedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextFailedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextRefreshedEvent;

/**
 * Default application context event logger. Logs (using the {@link org.eclipse.gemini.blueprint.extender.internal.activator.ContextLoaderListener} logger, the events received.
 * 
 * @author Costin Leau
 * @author Andy Piper
 */
public class DefaultOsgiBundleApplicationContextListener implements
		OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent> {

	/** logger */
	private final Log log;

	public DefaultOsgiBundleApplicationContextListener(Log log) {
		this.log = log;
	}

	public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
		String applicationContextString = event.getApplicationContext().getDisplayName();

		if (event instanceof OsgiBundleContextRefreshedEvent) {
			log.info("Application context successfully refreshed (" + applicationContextString + ")");
		}

		if (event instanceof OsgiBundleContextFailedEvent) {
			OsgiBundleContextFailedEvent failureEvent = (OsgiBundleContextFailedEvent) event;
			log.error("Application context refresh failed (" + applicationContextString + ")", failureEvent
					.getFailureCause());

		}

		if (event instanceof OsgiBundleContextClosedEvent) {
			OsgiBundleContextClosedEvent closedEvent = (OsgiBundleContextClosedEvent) event;
			Throwable error = closedEvent.getFailureCause();

			if (error == null) {
				log.info("Application context succesfully closed (" + applicationContextString + ")");
			} else {
				log.error("Application context close failed (" + applicationContextString + ")", error);
			}
		}
	}
}