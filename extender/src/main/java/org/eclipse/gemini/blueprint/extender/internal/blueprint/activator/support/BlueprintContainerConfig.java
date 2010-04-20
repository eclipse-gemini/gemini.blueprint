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

package org.eclipse.gemini.blueprint.extender.internal.blueprint.activator.support;

import java.util.Dictionary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.eclipse.gemini.blueprint.extender.support.ApplicationContextConfiguration;
import org.eclipse.gemini.blueprint.extender.support.internal.ConfigUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;

/**
 * Extension to the default {@link ApplicationContextConfiguration} that overrides Spring DM settings with RFC 124.
 * 
 * @author Costin Leau
 * 
 */
public class BlueprintContainerConfig extends ApplicationContextConfiguration {

	/** logger */
	private static final Log log = LogFactory.getLog(BlueprintContainerConfig.class);

	private final long timeout;
	private final boolean createAsync;
	private final boolean waitForDep;
	private final boolean publishContext;
	private final boolean hasTimeout;
	private final String toString;

	public BlueprintContainerConfig(Bundle bundle) {
		super(bundle, new BlueprintConfigurationScanner());

		Dictionary headers = bundle.getHeaders();

		hasTimeout = BlueprintConfigUtils.hasTimeout(headers);
		long option = BlueprintConfigUtils.getTimeOut(headers);
		// no need to translate into ms
		timeout = (option >= 0 ? option : ConfigUtils.DIRECTIVE_TIMEOUT_DEFAULT * 1000);
		createAsync = BlueprintConfigUtils.getCreateAsync(headers);
		waitForDep = BlueprintConfigUtils.getWaitForDependencies(headers);
		publishContext = BlueprintConfigUtils.getPublishContext(headers);

		StringBuilder buf = new StringBuilder();
		buf.append("Blueprint Config [Bundle=");
		buf.append(OsgiStringUtils.nullSafeSymbolicName(bundle));
		buf.append("]isBlueprintBundle=");
		buf.append(isSpringPoweredBundle());
		buf.append("|async=");
		buf.append(createAsync);
		buf.append("|graceperiod=");
		buf.append(waitForDep);
		buf.append("|publishCtx=");
		buf.append(publishContext);
		buf.append("|timeout=");
		buf.append(timeout);
		buf.append("ms");
		toString = buf.toString();

		if (log.isTraceEnabled()) {
			log.trace("Configuration: " + toString);
		}
	}

	@Override
	public boolean isTimeoutDeclared() {
		return hasTimeout;
	}

	public long getTimeout() {
		return timeout;
	}

	public boolean isCreateAsynchronously() {
		return createAsync;
	}

	public boolean isWaitForDependencies() {
		return waitForDep;
	}

	public boolean isPublishContextAsService() {
		return publishContext;
	}

	public String toString() {
		return toString;
	}
}