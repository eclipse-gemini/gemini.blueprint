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

package org.eclipse.gemini.blueprint.iandt.postprocessors;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * Tests application of ordered and unordered {@link BeanDefinitionRegistryPostProcessor} and {@link BeanFactoryPostProcessor}.
 *
 * @author Olaf Otto
 */
public class BeanDefinitionRegistryPostProcessorTest extends BaseIntegrationTest {
    private static ThreadLocal<List<String>> INVOCATION_TRACKER = ThreadLocal.withInitial(ArrayList::new);

    public static class DefinitionPostProcessor implements BeanDefinitionRegistryPostProcessor {
        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            INVOCATION_TRACKER.get().add("DefinitionPostProcessor.postProcessBeanDefinitionRegistry");
            registry.registerBeanDefinition("DefinitionPostProcessorChild", genericBeanDefinition(CustomPostProcessor.class).getBeanDefinition());
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            INVOCATION_TRACKER.get().add("DefinitionPostProcessor.postProcessBeanFactory");
        }

        public static class CustomPostProcessor implements BeanDefinitionRegistryPostProcessor {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                INVOCATION_TRACKER.get().add("DefinitionPostProcessorChild.postProcessBeanDefinitionRegistry");
                registry.registerBeanDefinition("DefinitionPostProcessorChildChild", genericBeanDefinition(ArrayList.class).getBeanDefinition());
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                INVOCATION_TRACKER.get().add("DefinitionPostProcessorChild.postProcessBeanFactory");
            }
        }
    }

    public static class DefinitionPostProcessorWithPriorityOrder2 implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {
        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            INVOCATION_TRACKER.get().add("DefinitionPostProcessorWithPriorityOrder2.postProcessBeanDefinitionRegistry");
            registry.registerBeanDefinition("DefinitionPostProcessorWithPriorityOrder2Child", genericBeanDefinition(CustomPostProcessor.class).getBeanDefinition());
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            INVOCATION_TRACKER.get().add("DefinitionPostProcessorWithPriorityOrder2.postProcessBeanFactory");
        }

        @Override
        public int getOrder() {
            return 2;
        }

        public static class CustomPostProcessor implements BeanDefinitionRegistryPostProcessor {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                INVOCATION_TRACKER.get().add("DefinitionPostProcessorWithPriorityOrder2Child.postProcessBeanDefinitionRegistry");
                registry.registerBeanDefinition("DefinitionPostProcessorWithPriorityOrder2ChildChild", genericBeanDefinition(ArrayList.class).getBeanDefinition());
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                INVOCATION_TRACKER.get().add("DefinitionPostProcessorWithPriorityOrder2Child.postProcessBeanFactory");
            }
        }
    }

    public static class DefinitionPostProcessorWithOrder2 implements BeanDefinitionRegistryPostProcessor, Ordered {
        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            INVOCATION_TRACKER.get().add("DefinitionPostProcessorWithOrder2.postProcessBeanDefinitionRegistry");
            registry.registerBeanDefinition("DefinitionPostProcessorWithOrder2Child", genericBeanDefinition(CustomPostProcessor.class).getBeanDefinition());
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            INVOCATION_TRACKER.get().add("DefinitionPostProcessorWithOrder2.postProcessBeanFactory");
        }

        @Override
        public int getOrder() {
            return 2;
        }

        public static class CustomPostProcessor implements BeanDefinitionRegistryPostProcessor {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                INVOCATION_TRACKER.get().add("DefinitionPostProcessorWithOrder2Child.postProcessBeanDefinitionRegistry");
                registry.registerBeanDefinition("DefinitionPostProcessorWithOrder2Child", genericBeanDefinition(ArrayList.class).getBeanDefinition());
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                INVOCATION_TRACKER.get().add("DefinitionPostProcessorWithOrder2Child.postProcessBeanFactory");
            }
        }
    }

    public static class RegularPostProcessor implements BeanFactoryPostProcessor {
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            INVOCATION_TRACKER.get().add("RegularPostProcessor.postProcessBeanFactory");
        }
    }


    public static class RegularPostProcessorWithPriorityOrder2 implements BeanFactoryPostProcessor, PriorityOrdered {
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            INVOCATION_TRACKER.get().add("RegularPostProcessorWithPriorityOrder2.postProcessBeanFactory");
        }

        @Override
        public int getOrder() {
            return 2;
        }
    }


    public static class RegularPostProcessorWithOrder2 implements BeanFactoryPostProcessor, Ordered {
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            INVOCATION_TRACKER.get().add("RegularPostProcessorWithOrder2.postProcessBeanFactory");
        }

        @Override
        public int getOrder() {
            return 2;
        }
    }


    protected String getManifestLocation() {
        return null;
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/eclipse/gemini/blueprint/iandt/postprocessors/postprocessors.xml"};
    }

    @Test
    public void testOrderOfRegistryAndFactoryPostProcessorInvocations() throws Exception {
        List<String> trackedInvocations = INVOCATION_TRACKER.get();

        assertThat(trackedInvocations).containsExactly(
                // Registry post processors must be executed first as they may alter the registry itself
                // The initial registry post-processors are ordered and must come first.
                "DefinitionPostProcessorWithPriorityOrder2.postProcessBeanDefinitionRegistry",
                "DefinitionPostProcessorWithOrder2.postProcessBeanDefinitionRegistry",
                "DefinitionPostProcessor.postProcessBeanDefinitionRegistry",
                "DefinitionPostProcessorWithPriorityOrder2Child.postProcessBeanDefinitionRegistry",
                "DefinitionPostProcessorWithOrder2Child.postProcessBeanDefinitionRegistry",
                "DefinitionPostProcessorChild.postProcessBeanDefinitionRegistry",

                // Factory post processing must be executed after the registry was processed.
                // The initial factory post-processors are ordered and must come first.
                "DefinitionPostProcessorWithPriorityOrder2.postProcessBeanFactory",
                "DefinitionPostProcessorWithOrder2.postProcessBeanFactory",
                "DefinitionPostProcessor.postProcessBeanFactory",
                "DefinitionPostProcessorWithPriorityOrder2Child.postProcessBeanFactory",
                "DefinitionPostProcessorWithOrder2Child.postProcessBeanFactory",
                "DefinitionPostProcessorChild.postProcessBeanFactory",
                "RegularPostProcessorWithPriorityOrder2.postProcessBeanFactory",
                "RegularPostProcessorWithOrder2.postProcessBeanFactory",
                "RegularPostProcessor.postProcessBeanFactory"
        );
    }
}
