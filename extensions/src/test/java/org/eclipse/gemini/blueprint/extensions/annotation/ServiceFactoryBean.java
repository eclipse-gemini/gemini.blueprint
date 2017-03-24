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

package org.eclipse.gemini.blueprint.extensions.annotation;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author Andy Piper
 * @since 2.1
 */
public class ServiceFactoryBean extends ServiceBean implements FactoryBean {
	public Object getObject() throws Exception {
		return new ServiceBean();
	}

	public Class<?> getObjectType() {
		return ServiceBean.class;
	}

	public boolean isSingleton() {
		return true;
	}
}
