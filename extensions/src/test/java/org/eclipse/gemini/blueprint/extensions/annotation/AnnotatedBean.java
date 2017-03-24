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
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.SortedSet;

/**
 * @author Andy Piper
 * @since 2.1
 */
public class AnnotatedBean {

	private String stringType;
	private int intType;
	private AnnotatedBean annotatedBeanTypeSimple;
	private AnnotatedBean annotatedBeanTypeComplex;
	private ApplicationContext applicatonContextType;
	private AnnotatedBean annotatedBeanTypeWithCardinality1_1;
	private AnnotatedBean annotatedBeanTypeWithCardinality0_1;
	private List<AnnotatedBean> annotatedBeanTypeWithCardinality0_N;
	private SortedSet<AnnotatedBean> annotatedBeanTypeWithCardinality1_N;
    private SortedSet<AnnotatedBean> annotatedBeanErrorTypeWithCardinality1_N;
	private AnnotatedBean annotatedBeanTypeWithClassLoaderClient;
	private AnnotatedBean annotatedBeanTypeWithClassLoaderServiceProvider;
	private AnnotatedBean annotatedBeanTypeWithClassLoaderUmanaged;
	private AnnotatedBean annotatedBeanTypeWithBeanName;
	private AnnotatedBean annotatedBeanTypeWithFilter;
	private AnnotatedBean annotatedBeanTypeWithTimeout;
	private AnnotatedBean annotatedBeanTypeWithServiceType;

	public String getStringType() {
		return stringType;
	}

	@ServiceReference
	public void setStringType(String stringType) {
		this.stringType = stringType;
	}

	public int getIntType() {
		return intType;
	}

	@ServiceReference
	public void setIntType(int intType) {
		this.intType = intType;
	}

	public AnnotatedBean getAnnotatedBeanTypeSimple() {
		return annotatedBeanTypeSimple;
	}

	@ServiceReference
	public void setAnnotatedBeanTypeSimple(AnnotatedBean annotatedBeanTypeSimple) {
		this.annotatedBeanTypeSimple = annotatedBeanTypeSimple;
	}

	public AnnotatedBean getAnnotatedBeanTypeComplex() {
		return annotatedBeanTypeComplex;
	}

	@ServiceReference(serviceBeanName = "myBean", cardinality = Availability.OPTIONAL,
		contextClassLoader = ServiceReferenceClassLoader.SERVICE_PROVIDER, timeout = 100, filter =  "(id=fooey)")
	public void setAnnotatedBeanTypeComplex(AnnotatedBean annotatedBeanTypeComplex) {
		this.annotatedBeanTypeComplex = annotatedBeanTypeComplex;
	}

	public ApplicationContext getApplicatonContextType() {
		return applicatonContextType;
	}

	@ServiceReference
	public void setApplicatonContextType(ApplicationContext applicatonContextType) {
		this.applicatonContextType = applicatonContextType;
	}

	public AnnotatedBean getAnnotatedBeanTypeWithCardinality1_1() {
		return annotatedBeanTypeWithCardinality1_1;
	}

	@ServiceReference(cardinality = Availability.MANDATORY)
	public void setAnnotatedBeanTypeWithCardinality1_1(AnnotatedBean annotatedBeanTypeWithCardinality1_1) {
		this.annotatedBeanTypeWithCardinality1_1 = annotatedBeanTypeWithCardinality1_1;
	}

	public AnnotatedBean getAnnotatedBeanTypeWithCardinality0_1() {
		return annotatedBeanTypeWithCardinality0_1;
	}

	@ServiceReference(cardinality = Availability.OPTIONAL)
	public void setAnnotatedBeanTypeWithCardinality0_1(AnnotatedBean annotatedBeanTypeWithCardinality0_1) {
		this.annotatedBeanTypeWithCardinality0_1 = annotatedBeanTypeWithCardinality0_1;
	}

	public List<AnnotatedBean> getAnnotatedBeanTypeWithCardinality0_N() {
		return annotatedBeanTypeWithCardinality0_N;
	}

	@ServiceReference(cardinality = Availability.OPTIONAL, serviceTypes = AnnotatedBean.class)
	public void setAnnotatedBeanTypeWithCardinality0_N(List<AnnotatedBean> annotatedBeanTypeWithCardinality0_N) {
		this.annotatedBeanTypeWithCardinality0_N = annotatedBeanTypeWithCardinality0_N;
	}

	public SortedSet<AnnotatedBean> getAnnotatedBeanErrorTypeWithCardinality1_N() {
		return annotatedBeanErrorTypeWithCardinality1_N;
	}

	@ServiceReference(cardinality = Availability.MANDATORY)
	public void setAnnotatedBeanErrorTypeWithCardinality1_N(SortedSet<AnnotatedBean> annotatedBeanTypeWithCardinality1_N) {
		this.annotatedBeanErrorTypeWithCardinality1_N = annotatedBeanTypeWithCardinality1_N;
	}

    public SortedSet<AnnotatedBean> getAnnotatedBeanTypeWithCardinality1_N() {
        return annotatedBeanTypeWithCardinality1_N;
    }

    @ServiceReference(cardinality = Availability.MANDATORY, serviceTypes = AnnotatedBean.class)
    public void setAnnotatedBeanTypeWithCardinality1_N(SortedSet<AnnotatedBean> annotatedBeanTypeWithCardinality1_N) {
        this.annotatedBeanTypeWithCardinality1_N = annotatedBeanTypeWithCardinality1_N;
    }
	public AnnotatedBean getAnnotatedBeanTypeWithClassLoaderClient() {
		return annotatedBeanTypeWithClassLoaderClient;
	}

	@ServiceReference(contextClassLoader = ServiceReferenceClassLoader.CLIENT)
	public void setAnnotatedBeanTypeWithClassLoaderClient(AnnotatedBean annotatedBeanTypeWithClassLoaderClient) {
		this.annotatedBeanTypeWithClassLoaderClient = annotatedBeanTypeWithClassLoaderClient;
	}

	public AnnotatedBean getAnnotatedBeanTypeWithClassLoaderServiceProvider() {
		return annotatedBeanTypeWithClassLoaderServiceProvider;
	}

	@ServiceReference(contextClassLoader = ServiceReferenceClassLoader.SERVICE_PROVIDER)
	public void setAnnotatedBeanTypeWithClassLoaderServiceProvider(AnnotatedBean annotatedBeanTypeWithClassLoaderServiceProvider) {
		this.annotatedBeanTypeWithClassLoaderServiceProvider = annotatedBeanTypeWithClassLoaderServiceProvider;
	}

	public AnnotatedBean getAnnotatedBeanTypeWithClassLoaderUmanaged() {
		return annotatedBeanTypeWithClassLoaderUmanaged;
	}

	@ServiceReference(contextClassLoader = ServiceReferenceClassLoader.UNMANAGED)
	public void setAnnotatedBeanTypeWithClassLoaderUmanaged(AnnotatedBean annotatedBeanTypeWithClassLoaderUmanaged) {
		this.annotatedBeanTypeWithClassLoaderUmanaged = annotatedBeanTypeWithClassLoaderUmanaged;
	}

	public AnnotatedBean getAnnotatedBeanTypeWithBeanName() {
		return annotatedBeanTypeWithBeanName;
	}

	@ServiceReference(serviceBeanName = "myBean")
	public void setAnnotatedBeanTypeWithBeanName(AnnotatedBean annotatedBeanTypeWithBeanName) {
		this.annotatedBeanTypeWithBeanName = annotatedBeanTypeWithBeanName;
	}

	public AnnotatedBean getAnnotatedBeanTypeWithFilter() {
		return annotatedBeanTypeWithFilter;
	}

	@ServiceReference(filter = "(wooey=fooo)")
	public void setAnnotatedBeanTypeWithFilter(AnnotatedBean annotatedBeanTypeWithFilter) {
		this.annotatedBeanTypeWithFilter = annotatedBeanTypeWithFilter;
	}

	public AnnotatedBean getAnnotatedBeanTypeWithTimeout() {
		return annotatedBeanTypeWithTimeout;
	}

	@ServiceReference(timeout = 12345)
	public void setAnnotatedBeanTypeWithTimeout(AnnotatedBean annotatedBeanTypeWithTimeout) {
		this.annotatedBeanTypeWithTimeout = annotatedBeanTypeWithTimeout;
	}

	public AnnotatedBean getAnnotatedBeanTypeWithServiceType() {
		return annotatedBeanTypeWithServiceType;
	}

	@ServiceReference(serviceTypes = Object.class)
	public void setAnnotatedBeanTypeWithServiceType(AnnotatedBean annotatedBeanTypeWithServiceType) {
		this.annotatedBeanTypeWithServiceType = annotatedBeanTypeWithServiceType;
	}

    @ServiceReference(serviceTypes = { Object.class, AnnotatedBean.class })
    public void setAnnotatedBeanTypeWithMultipleServiceTypes(AnnotatedBean annotatedBeanTypeWithServiceType) {
        this.annotatedBeanTypeWithServiceType = annotatedBeanTypeWithServiceType;
    }
}
