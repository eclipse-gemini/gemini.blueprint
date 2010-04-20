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

package org.eclipse.gemini.blueprint.extender.internal.blueprint.event;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.springframework.util.ObjectUtils;

/**
 * Actual {@link EventAdmin} dispatcher. Implemented as a package-protected generic class that can be further configured
 * by the user-facing classes. The additional benefit is that being a separate class, this class can be used only when
 * the org.osgi.service.event package is available, w/o triggering runtime class loading.
 * 
 * @author Costin Leau
 */
class OsgiEventDispatcher implements EventDispatcher, BlueprintConstants {

	/** logger */
	private static final Log log = LogFactory.getLog(OsgiEventDispatcher.class);

	private static final String EVENT_ADMIN = "org.osgi.service.event.EventAdmin";

	private final BundleContext bundleContext;
	private final PublishType publisher;

	public OsgiEventDispatcher(BundleContext bundleContext, PublishType publisher) {
		this.bundleContext = bundleContext;
		this.publisher = publisher;
	}

	public void afterClose(BlueprintEvent event) {
		Dictionary<String, Object> props = init(event);
		sendEvent(new Event(TOPIC_DESTROYED, props));
	}

	public void afterRefresh(BlueprintEvent event) {
		Dictionary<String, Object> props = init(event);
		sendEvent(new Event(TOPIC_CREATED, props));
	}

	public void beforeClose(BlueprintEvent event) {
		Dictionary<String, Object> props = init(event);
		sendEvent(new Event(TOPIC_DESTROYING, props));
	}

	public void beforeRefresh(BlueprintEvent event) {
		Dictionary<String, Object> props = init(event);
		sendEvent(new Event(TOPIC_CREATING, props));
	}

	public void refreshFailure(BlueprintEvent event) {
		Dictionary<String, Object> props = init(event);

		Throwable th = event.getCause();
		props.put(EXCEPTION, th);
		props.put(CAUSE, th);
		props.put(EXCEPTION_CLASS, th.getClass().getName());
		String msg = th.getMessage();
		props.put(EXCEPTION_MESSAGE, (msg != null ? msg : ""));
		initDependencies(props, event);
		sendEvent(new Event(TOPIC_FAILURE, props));
	}

	public void grace(BlueprintEvent event) {
		Dictionary<String, Object> props = init(event);
		initDependencies(props, event);
		sendEvent(new Event(TOPIC_GRACE, props));
	}

	public void waiting(BlueprintEvent event) {
		Dictionary<String, Object> props = init(event);
		initDependencies(props, event);
		sendEvent(new Event(TOPIC_WAITING, props));
	}

	private void initDependencies(Dictionary<String, Object> props, BlueprintEvent event) {
		String[] deps = event.getDependencies();
		if (!ObjectUtils.isEmpty(deps)) {
			props.put(DEPENDENCIES, deps);
			// props.put(SERVICE_FILTER, deps[0]);
			// props.put(SERVICE_FILTER_2, deps[0]);
			// props.put(SERVICE_OBJECTCLASS, extractObjectClassFromFilter(deps[0]));
			props.put(ALL_DEPENDENCIES, deps);
		}
	}

	private Dictionary<String, Object> init(BlueprintEvent event) {
		Dictionary<String, Object> props = new Hashtable<String, Object>();

		Bundle bundle = event.getBundle();

		// common properties
		props.put(TIMESTAMP, System.currentTimeMillis());
		props.put(EVENT, event);
		props.put(TYPE, Integer.valueOf(event.getType()));

		props.put(BUNDLE, event.getBundle());
		props.put(BUNDLE_ID, bundle.getBundleId());

		// name (under two keys)
		String name = OsgiStringUtils.nullSafeName(bundle);
		props.put(BUNDLE_NAME, name);
		props.put(Constants.BUNDLE_NAME, name);

		// sym name (under two keys)
		String symName = OsgiStringUtils.nullSafeSymbolicName(bundle);
		props.put(BUNDLE_SYM_NAME, symName);
		props.put(Constants.BUNDLE_SYMBOLICNAME, symName);

		// version (as well under two keys)
		Version version = OsgiBundleUtils.getBundleVersion(bundle);
		props.put(BUNDLE_VERSION, version);
		props.put(Constants.BUNDLE_VERSION, version);

		// extender bundle info
		Bundle extenderBundle = event.getExtenderBundle();

		props.put(EXTENDER_BUNDLE, extenderBundle);
		props.put(EXTENDER_BUNDLE_ID, extenderBundle.getBundleId());
		props.put(EXTENDER_BUNDLE_SYM_NAME, extenderBundle.getSymbolicName());
		Version extenderVersion = OsgiBundleUtils.getBundleVersion(extenderBundle);
		props.put(EXTENDER_BUNDLE_VERSION, extenderVersion);

		return props;
	}

	private void sendEvent(Event osgiEvent) {
		boolean trace = log.isTraceEnabled();
		ServiceReference ref = bundleContext.getServiceReference(EVENT_ADMIN);
		if (ref != null) {
			EventAdmin eventAdmin = (EventAdmin) bundleContext.getService(ref);
			if (eventAdmin != null) {
				if (trace) {
					StringBuilder sb = new StringBuilder();
					String[] names = osgiEvent.getPropertyNames();
					sb.append("{");
					for (int i = 0; i < names.length; i++) {
						String name = names[i];
						sb.append(name);
						sb.append("=");
						Object value = osgiEvent.getProperty(name);
						sb.append(ObjectUtils.getDisplayString(value));
						if (i < names.length - 1)
							sb.append(",");
					}
					sb.append("}");

					log.trace("Broadcasting OSGi event " + osgiEvent + " w/ props " + sb.toString());
				}
				publisher.publish(eventAdmin, osgiEvent);
			}
		} else {
			log.trace("No event admin found for broadcasting event " + osgiEvent);
		}
	}
}