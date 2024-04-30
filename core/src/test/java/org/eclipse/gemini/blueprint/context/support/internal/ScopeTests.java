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

package org.eclipse.gemini.blueprint.context.support.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

public class ScopeTests {

	private static Object tag;

	private static Runnable callback = null;


	private static abstract class AbstractScope implements Scope {

		public String getConversationId() {
			System.out.println("returning conversation id");
			return null;
		}

		public void registerDestructionCallback(String name, Runnable cb) {
			System.out.println("registering callback " + cb + " for bean " + name);
			callback = cb;
		}

		public Object remove(String name) {
			System.out.println("destroying bean " + name);
			return null;
		}

		public Object resolveContextualObject(String arg0) {
			return null;
		}
	}

	private class FooScope extends AbstractScope {

		public Object get(String name, ObjectFactory objectFactory) {
			System.out.println("tag is " + tag);
			System.out.println("requested " + name + " w/ objFact " + objectFactory);
			if (ScopeTests.tag == null) {
				Object obj = objectFactory.getObject();
				System.out.println("set tag to " + obj);
				System.out.println("obj is " + obj + "|hash=" + System.identityHashCode(obj));
				ScopeTests.tag = obj;
			}

			return tag;
		}

	}


	private DefaultListableBeanFactory bf;


	private class ScopedXmlFactory extends DefaultListableBeanFactory {
		private XmlBeanDefinitionReader reader;

		public ScopedXmlFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
			super(parentBeanFactory);
			reader = new XmlBeanDefinitionReader((BeanDefinitionRegistry)parentBeanFactory);
			reader.loadBeanDefinitions(resource);
		}

		public ScopedXmlFactory(Resource resource) throws BeansException {
			super();
			reader = new XmlBeanDefinitionReader(this);
			reader.loadBeanDefinitions(resource);
			registerScope("foo", new FooScope());
			registerScope("bar", new FooScope());
		}
	}

	@Before
	public void setup() throws Exception {
		Resource file = new ClassPathResource("scopes.xml");
		bf = new ScopedXmlFactory(file);

		callback = null;
		tag = null;
	}

	@After
	public void tearDown() throws Exception {
		bf.destroySingletons();
		callback = null;
		tag = null;
	}

	@Test
	public void testScopes() throws Exception {

		assertNull(tag);
		Object a = bf.getBean("a");
		System.out.println("got a" + a);
		assertNotNull(tag);

		((Properties) a).put("goo", "foo");

		Object b = bf.getBean("b");
		System.out.println("request b;got=" + b);
		System.out.println("b class is" + b.getClass());
		b = bf.getBean("b");
		System.out.println("request b;got=" + b);
		System.out.println("b class is" + b.getClass());

		Object scopedA = bf.getBean("a");
		System.out.println(scopedA.getClass());
		System.out.println(a);
		System.out.println(scopedA);
		System.out.println(ObjectUtils.nullSafeToString(ClassUtils.getAllInterfaces(scopedA)));
	}

	@Test
	public void testCallback() throws Exception {
		Object a = bf.getBean("a");
		// assertNotNull(callback);
		// Runnable aCallback = callback;
		Properties props = (Properties) a;
		props.put("foo", "bar");

		bf.destroyScopedBean("a");

		System.out.println(ObjectUtils.nullSafeToString(bf.getRegisteredScopeNames()));
		//assertTrue(props.isEmpty());
	}
}
