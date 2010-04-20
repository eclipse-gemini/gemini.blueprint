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

import org.osgi.service.event.EventConstants;

/**
 * Utility interface aggregating the event properties from various OSGi APIs in one single place.
 * 
 * @author Costin Leau
 */
interface BlueprintConstants {

	static final String BUNDLE = "bundle";
	static final String BUNDLE_ID = "bundle.id";
	static final String BUNDLE_NAME = "bundle.name";
	static final String BUNDLE_SYM_NAME = EventConstants.BUNDLE_SYMBOLICNAME;
	static final String BUNDLE_VERSION = "bundle.version";
	static final String TIMESTAMP = EventConstants.TIMESTAMP;

	static final String EVENT = EventConstants.EVENT;
	static final String TYPE = org.osgi.service.blueprint.container.EventConstants.TYPE;

	static final String EXTENDER_BUNDLE = org.osgi.service.blueprint.container.EventConstants.EXTENDER_BUNDLE;
	static final String EXTENDER_BUNDLE_ID = org.osgi.service.blueprint.container.EventConstants.EXTENDER_BUNDLE_ID;
	static final String EXTENDER_BUNDLE_SYM_NAME =
			org.osgi.service.blueprint.container.EventConstants.EXTENDER_BUNDLE_SYMBOLICNAME;
	static final String EXTENDER_BUNDLE_VERSION =
			org.osgi.service.blueprint.container.EventConstants.EXTENDER_BUNDLE_VERSION;

	static final String EXCEPTION = EventConstants.EXCEPTION;
	static final String CAUSE = "cause";
	static final String EXCEPTION_CLASS = EventConstants.EXECPTION_CLASS;
	static final String EXCEPTION_MESSAGE = EventConstants.EXCEPTION_MESSAGE;

	static final String SERVICE_OBJECTCLASS = EventConstants.SERVICE_OBJECTCLASS;
	static final String SERVICE_FILTER = "service.filter";
	static final String SERVICE_FILTER_2 = "service.Filter";
	static final String DEPENDENCIES = "dependencies";
	static final String ALL_DEPENDENCIES = "dependencies.all";

	static final String TOPIC_BLUEPRINT_EVENTS = "org/osgi/service/blueprint";
	static final String TOPIC_CREATING = TOPIC_BLUEPRINT_EVENTS + "/container/CREATING";
	static final String TOPIC_GRACE = TOPIC_BLUEPRINT_EVENTS + "/container/GRACE_PERIOD";
	static final String TOPIC_CREATED = TOPIC_BLUEPRINT_EVENTS + "/container/CREATED";
	static final String TOPIC_DESTROYING = TOPIC_BLUEPRINT_EVENTS + "/container/DESTROYING";
	static final String TOPIC_DESTROYED = TOPIC_BLUEPRINT_EVENTS + "/container/DESTROYED";
	static final String TOPIC_WAITING = TOPIC_BLUEPRINT_EVENTS + "/container/WAITING";
	static final String TOPIC_FAILURE = TOPIC_BLUEPRINT_EVENTS + "/container/FAILURE";

	static final String EVENT_FILTER = EventConstants.EVENT_FILTER;
}
