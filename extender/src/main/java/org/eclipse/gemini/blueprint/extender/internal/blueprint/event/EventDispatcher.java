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

import org.osgi.service.blueprint.container.BlueprintEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;

/**
 * Dispatcher of {@link OsgiBundleApplicationContextEvent events}. Normally used as an adapter to other event
 * infrastructure such as {@link org.springframework.context.EventAdmin}. If the need arises, this interface might be
 * promoted and moved into Spring DM core.
 * 
 * @author Costin Leau
 */
interface EventDispatcher {

	void beforeClose(BlueprintEvent event);

	void beforeRefresh(BlueprintEvent event);

	void afterClose(BlueprintEvent event);

	void afterRefresh(BlueprintEvent event);

	void refreshFailure(BlueprintEvent event);

	void waiting(BlueprintEvent event);

	void grace(BlueprintEvent event);
}
