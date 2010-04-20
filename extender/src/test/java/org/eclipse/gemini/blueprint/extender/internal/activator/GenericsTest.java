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

package org.eclipse.gemini.blueprint.extender.internal.activator;

import junit.framework.TestCase;

import org.springframework.core.GenericTypeResolver;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextClosedEvent;
import org.eclipse.gemini.blueprint.extender.internal.activator.GenericsTest.SomeClass.AnotherClass.NestedListener;

/**
 * Basic generic detection test.
 * 
 * @author Costin Leau
 */
public class GenericsTest extends TestCase {

	public static class SomeClass {
		public static class AnotherClass {
			public static class NestedListener implements
					OsgiBundleApplicationContextListener<OsgiBundleContextClosedEvent> {

				public void onOsgiApplicationEvent(OsgiBundleContextClosedEvent event) {
				}
			}
		}
	}

	public void testRawType() throws Exception {
		assertSame(null, GenericTypeResolver.resolveTypeArgument(RawListener.class,
				OsgiBundleApplicationContextListener.class));
	}

	public void testGenericType() throws Exception {
		assertSame(OsgiBundleApplicationContextEvent.class, GenericTypeResolver.resolveTypeArgument(
				GenericListener.class, OsgiBundleApplicationContextListener.class));
	}

	public void testSpecializedType() throws Exception {
		assertSame(OsgiBundleContextClosedEvent.class, GenericTypeResolver.resolveTypeArgument(
				SpecializedListener.class, OsgiBundleApplicationContextListener.class));
	}

	public void testNestedListener() throws Exception {
		assertSame(OsgiBundleContextClosedEvent.class, GenericTypeResolver.resolveTypeArgument(NestedListener.class,
				OsgiBundleApplicationContextListener.class));
	}
}
