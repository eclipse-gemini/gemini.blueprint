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

package org.eclipse.gemini.blueprint.util;

import junit.framework.TestCase;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.support.AbstractDelegatedExecutionApplicationContext;
import org.eclipse.gemini.blueprint.context.support.AbstractOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.util.internal.ClassUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.Closeable;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.gemini.blueprint.util.internal.ClassUtils.ClassSet.ALL_CLASSES;
import static org.eclipse.gemini.blueprint.util.internal.ClassUtils.ClassSet.CLASS_HIERARCHY;
import static org.eclipse.gemini.blueprint.util.internal.ClassUtils.ClassSet.INTERFACES;
import static org.eclipse.gemini.blueprint.util.internal.ClassUtils.getClassHierarchy;

/**
 * @author Costin Leau
 * @author Olaf Otto
 */
public class ClassUtilsTest extends TestCase {

    public void testAutoDetectClassesForPublishingDisabled() {
        Class<?>[] resolved = getClassHierarchy(Integer.class, INTERFACES);
        assertThat(resolved).hasSize(4);
    }

    public void testAutoDetectClassesForPublishingInterfaces() {
        Class<?>[] resolved = getClassHierarchy(HashMap.class, INTERFACES);
        assertThat(resolved).containsExactly(Map.class, Cloneable.class, Serializable.class);
    }

    public void testAutoDetectClassesForPublishingClassHierarchy() {
        Class<?>[] resolved = getClassHierarchy(HashMap.class, CLASS_HIERARCHY);
        assertThat(resolved).containsExactly(HashMap.class, AbstractMap.class);
    }

    public void testAutoDetectClassesForPublishingAll() {
        Class<?>[] resolved = getClassHierarchy(HashMap.class, ALL_CLASSES);

        assertThat(resolved).containsExactly(HashMap.class, Map.class, Cloneable.class, Serializable.class, AbstractMap.class);
    }

    public void testInterfacesHierarchy() {
        Class<?>[] resolved = ClassUtils.getAllInterfaces(DelegatedExecutionOsgiBundleApplicationContext.class);

        assertThat(resolved).containsExactly(ConfigurableOsgiBundleApplicationContext.class, ConfigurableApplicationContext.class,
                ApplicationContext.class, Lifecycle.class, Closeable.class, EnvironmentCapable.class, ListableBeanFactory.class,
                HierarchicalBeanFactory.class, MessageSource.class, ApplicationEventPublisher.class,
                ResourcePatternResolver.class, BeanFactory.class, ResourceLoader.class, AutoCloseable.class);
    }

    public void testAppContextClassHierarchy() {
        Class<?>[] resolved = getClassHierarchy(OsgiBundleXmlApplicationContext.class, ALL_CLASSES);

        assertThat(resolved).containsExactly(
                OsgiBundleXmlApplicationContext.class,
                DisposableBean.class,
                AbstractDelegatedExecutionApplicationContext.class,
                DelegatedExecutionOsgiBundleApplicationContext.class,
                ConfigurableOsgiBundleApplicationContext.class,
                ConfigurableApplicationContext.class,
                ApplicationContext.class,
                Lifecycle.class,
                Closeable.class,
                EnvironmentCapable.class,
                ListableBeanFactory.class,
                HierarchicalBeanFactory.class,
                MessageSource.class,
                ApplicationEventPublisher.class,
                ResourcePatternResolver.class,
                BeanFactory.class,
                ResourceLoader.class,
                AutoCloseable.class,
                AbstractOsgiBundleApplicationContext.class,
                AbstractRefreshableApplicationContext.class,
                AbstractApplicationContext.class,
                DefaultResourceLoader.class
        );
    }
}
