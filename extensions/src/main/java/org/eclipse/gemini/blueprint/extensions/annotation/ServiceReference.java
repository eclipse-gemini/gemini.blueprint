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

import org.eclipse.gemini.blueprint.service.importer.support.Availability;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method  (typically a JavaBean setter method) or a field as requiring an OSGi service reference.
 * 
 * @author Andy Piper
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface ServiceReference {
    /**
	 * The name of the bean that backs the injected service. May be null.
	 */
	String serviceBeanName() default "";

	/**
	 * The cardinality of the service reference, defaults to mandatory.
	 */
	Availability cardinality() default Availability.MANDATORY;

    /**
	 * The invocation context classloader setting. Defalts to the classloader of the client.
	 */
	ServiceReferenceClassLoader contextClassLoader() default ServiceReferenceClassLoader.CLIENT;

	/**
	 * Timeout for service resolution in milliseconds.
	 */
	int timeout() default 300000;

	/**
	 * Interface (or class) of the service to be injected
	 */
	Class<?>[] serviceTypes() default ServiceReference.class;

    /**
     * Whether or not to proxy greedily in collection references.
     */
    boolean greedyProxying() default false;

    /**
     * Whether or not to create a 'sticky' (singular) service reference.
     */
    boolean sticky() default true;

    /**
	 * filter used to narrow service matches, may be null
	 */
	String filter() default "";
}
