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

package org.eclipse.gemini.blueprint.service.exporter.support.internal.controller;

import org.springframework.util.Assert;

/**
 * Internal class that allows framework code (in other packages) to work with
 * the exporter internals without exposing the methods on the exporter public
 * API.
 * 
 * 
 * @author Costin Leau
 */
public class ExporterController implements ExporterInternalActions {

	private ExporterInternalActions executor;


	public ExporterController(ExporterInternalActions executor) {
		Assert.notNull(executor, "executor is required");
		this.executor = executor;
	}

	public void registerService() {
		executor.registerService();
	}

	public void registerServiceAtStartup(boolean register) {
		executor.registerServiceAtStartup(register);
	}

	public void unregisterService() {
		executor.unregisterService();
	}

	public void callUnregisterOnStartup() {
		executor.callUnregisterOnStartup();
	}
}
