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

package org.eclipse.gemini.blueprint.service.exporter.support.internal.support;

import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.springframework.beans.factory.BeanFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Class encapsulating the lazy dependency lookup of the target bean. Handles caching as well as the lazy listener
 * notifications.
 *
 * @author Costin Leau
 */
public class LazyTargetResolver implements UnregistrationNotifier {

    private final BeanFactory beanFactory;
    private final String beanName;
    private final boolean cacheService;
    private volatile Object target;
    private final Object lock = new Object();
    private final AtomicBoolean activated;
    private final ListenerNotifier notifier;
    private volatile ServiceRegistrationDecorator decorator;

    public LazyTargetResolver(Object target, BeanFactory beanFactory, String beanName, boolean cacheService,
                              ListenerNotifier notifier, boolean lazyListeners) {
        this.target = target;
        this.beanFactory = beanFactory;
        this.beanName = beanName;
        this.cacheService = cacheService;
        this.notifier = notifier;
        this.activated = new AtomicBoolean(!lazyListeners);
    }

    public void activate() {
        if (activated.compareAndSet(false, true) && notifier != null) {
            // no service registered
            if (decorator == null) {
                notifier.callUnregister(null, null);
            } else {
                Object target = getBeanIfAlreadyInstantiatedOrSingletonScoped().orElse(null);
                Map properties = (Map) OsgiServiceReferenceUtils.getServicePropertiesSnapshot(decorator.getReference());
                notifier.callRegister(target, properties);
            }
        }
    }

    public Object getBean() {
        if (target != null) {
            return target;
        }

        if (beanFactory.isSingleton(beanName) || !cacheService) {
            return beanFactory.getBean(beanName);
        }

        // Per Blueprint spec, 121.6.8 Scope, a service must be represented by a single component instance,
        // regardless of the component's scope. We must thus obtain the non-singleton component instance and treat
        // it like a singleton.
        //
        // However, multiple bean instances may exist and may be requested concurrently in this scenario. Following, we will explicitly allow multiple
        // bean instances to be created during concurrent access to avoid interlocking (independent locking here and in the bean factory) which would
        // always mean potential deadlocks.
        //
        // The bean may be created multiple times, however the guarantees of the OSGi spec are met: Exactly one instance
        // is shared as the service instance, superfluous instances are simply ignored.
        Object targetCandidate = beanFactory.getBean(beanName);
        if (target == null) {
            synchronized (lock) {
                if (target == null) {
                    target = targetCandidate;
                }
            }
        }

        return target;
    }

    public Class<?> getType() {
        if (target != null) {
            return target.getClass();
        }

        if (beanFactory.isSingleton(beanName)) {
            return beanFactory.getBean(beanName).getClass();
        }

        return beanFactory.getType(beanName);
    }

    public void unregister(Map properties) {
        if (activated.get() && notifier != null) {
            Object target = getBeanIfAlreadyInstantiatedOrSingletonScoped().orElse(null);
            notifier.callUnregister(target, properties);
        }
    }

    public void setDecorator(ServiceRegistrationDecorator decorator) {
        this.decorator = decorator;
        if (decorator != null) {
            decorator.setNotifier(this);
        }
    }

    public void notifyIfPossible() {
        if (activated.get() && notifier != null) {
            Object target = getBeanIfAlreadyInstantiatedOrSingletonScoped().orElse(null);
            Map properties = (Map) OsgiServiceReferenceUtils.getServicePropertiesSnapshot(decorator.getReference());
            notifier.callRegister(target, properties);
        }
    }

    // called when the exporter is activated but no service is published

    public void startupUnregisterIfPossible() {
        if (activated.get() && notifier != null) {
            notifier.callUnregister(null, null);
        }
    }

    private Optional<Object> getBeanIfAlreadyInstantiatedOrSingletonScoped() {
        if (target != null) {
            return of(target);
        }
        if (cacheService || beanFactory.isSingleton(beanName)) {
            return of(getBean());
        }
        return empty();
    }
}

