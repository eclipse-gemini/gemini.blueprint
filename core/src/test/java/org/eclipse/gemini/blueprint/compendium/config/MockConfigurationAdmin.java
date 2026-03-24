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

package org.eclipse.gemini.blueprint.compendium.config;

import java.io.IOException;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;


/**
 * @author Costin Leau
 *
 */
public class MockConfigurationAdmin implements ConfigurationAdmin {

	public Configuration createFactoryConfiguration(String factoryPid, String location) throws IOException {
		return null;
	}

	public Configuration createFactoryConfiguration(String factoryPid) throws IOException {
		return null;
	}

	public Configuration getConfiguration(String pid, String location) throws IOException {
		return null;
	}

	public Configuration getConfiguration(String pid) throws IOException {
		return null;
	}

	public Configuration[] listConfigurations(String filter) throws IOException, InvalidSyntaxException {
		return null;
	}

	public Configuration getFactoryConfiguration(String factoryPid, String name, String location) throws IOException {
		return null;
	}

	public Configuration getFactoryConfiguration(String factoryPid, String name) throws IOException {
		return null;
	}

}
