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

package org.eclipse.gemini.blueprint.extender.internal.dependencies.startup;

/**
 * State of an application context while being processed by {@link DependencyWaiterApplicationContextExecutor}.
 * 
 * This enumeration holds the state of an application context at a certain time, beyond the official states such as
 * STARTED/STOPPED.
 * 
 * @author Hal Hildebrand
 * @author Costin Leau
 * 
 */
public enum ContextState {

	/**
	 * Application context has been initialized but not started (i.e. refresh hasn't been called).
	 */
	INITIALIZED,

	/**
	 * Application context has been started but the OSGi service dependencies haven't been yet resolved.
	 */
	RESOLVING_DEPENDENCIES,

	/**
	 * Application context has been started and the OSGi dependencies have been resolved. However the context is not
	 * fully initialized (i.e. refresh hasn't been completed).
	 */
	DEPENDENCIES_RESOLVED,

	/**
	 * Application context has been fully initialized. The OSGi dependencies have been resolved and refresh has fully
	 * completed.
	 */
	STARTED,

	/**
	 * Application context has been interrupted. This state occurs if the context is being closed before being fully
	 * started.
	 */
	INTERRUPTED,

	/**
	 * Application context has been stopped. This can occur even only if the context has been fully started for example;
	 * otherwise {@link #INTERRUPTED} state should be used.
	 */
	STOPPED;

	/**
	 * Indicates whether the state is 'down' or not - that is a context which has been either closed or stopped.
	 * 
	 * @return true if the context has been interrupted or stopped, false otherwise.
	 */
	public boolean isDown() {
		return (this.equals(INTERRUPTED) || this.equals(STOPPED));
	}

	/**
	 * Indicates whether the state is unresolved or not. An unresolved state means a state which is active (started) in
	 * RESOLVING_DEPENDENCIES state.
	 * 
	 * @return
	 */
	public boolean isUnresolved() {
		return (this.equals(RESOLVING_DEPENDENCIES) || this.equals(INITIALIZED));
	}

	public boolean isResolved() {
		return !isUnresolved();
	}
}