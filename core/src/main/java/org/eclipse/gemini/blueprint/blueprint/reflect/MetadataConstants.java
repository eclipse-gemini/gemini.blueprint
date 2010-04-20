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

package org.eclipse.gemini.blueprint.blueprint.reflect;

import org.eclipse.gemini.blueprint.blueprint.reflect.internal.metadata.EnvironmentManagerFactoryBean;
import org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.osgi.service.blueprint.reflect.ComponentMetadata;

/**
 * Holder for various constants used by metadata factories.
 * 
 * @author Costin Leau
 */
interface MetadataConstants {

	// common properties shared across the metadata factories
	static final Class<OsgiServiceFactoryBean> EXPORTER_CLASS = OsgiServiceFactoryBean.class;
	static final Class<OsgiServiceProxyFactoryBean> SINGLE_SERVICE_IMPORTER_CLASS = OsgiServiceProxyFactoryBean.class;
	static final Class<OsgiServiceCollectionProxyFactoryBean> MULTI_SERVICE_IMPORTER_CLASS =
			OsgiServiceCollectionProxyFactoryBean.class;
	static final Class<EnvironmentManagerFactoryBean> ENV_FB_CLASS = EnvironmentManagerFactoryBean.class;

	// component metadata attribute holder (for spring bean definitions)
	static final String SPRING_DM_PREFIX = "spring.osgi.";
	static final String COMPONENT_METADATA_ATTRIBUTE = SPRING_DM_PREFIX + ComponentMetadata.class.getName();
	static final String COMPONENT_NAME = SPRING_DM_PREFIX + "component.name";

	// exporter properties
	static String EXPORTER_RANKING_PROP = "ranking";
	static String EXPORTER_INTFS_PROP = "interfaces";
	static String EXPORTER_PROPS_PROP = "serviceProperties";
	static String EXPORTER_AUTO_EXPORT_PROP = "interfaceDetector";
	static String EXPORTER_TARGET_BEAN_PROP = "targetBean";
	static String EXPORTER_TARGET_BEAN_NAME_PROP = "targetBeanName";

	// importer common properties
	static String IMPORTER_INTFS_PROP = "interfaces";
	static String IMPORTER_FILTER_PROP = "filter";
	static String IMPORTER_CARDINALITY_PROP = "cardinality";

	// single importer
	static String IMPORTER_BEAN_NAME_PROP = "serviceBeanName";
	static String IMPORTER_TIMEOUT_PROP = "timeout";

	// multi importer
	static String IMPORTER_COLLECTION_PROP = "collectionType";
}
