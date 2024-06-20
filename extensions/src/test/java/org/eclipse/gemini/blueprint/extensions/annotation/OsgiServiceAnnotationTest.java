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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.easymock.EasyMock;
import org.easymock.internal.MocksControl;
import org.eclipse.gemini.blueprint.mock.MockBundleContext;
import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.eclipse.gemini.blueprint.service.importer.support.ImportContextClassLoaderEnum;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.SortedSet;

/**
 * @author Andy Piper
 */
public class OsgiServiceAnnotationTest {

	private ServiceReferenceInjectionBeanPostProcessor processor;

	private BundleContext context;

	@Before
	public void setup() throws Exception {
		processor = new ServiceReferenceInjectionBeanPostProcessor();
		context = new MockBundleContext();
		processor.setBundleContext(context);
		processor.setBeanClassLoader(getClass().getClassLoader());
		BeanFactory factory = EasyMock.createMock(BeanFactory.class);
		processor.setBeanFactory(factory);
	}

	/**
	 * Disabled since it doesn't work as we can't proxy final classes.
	 */
	@Test
	@Ignore
	public void tstGetServicePropertySetters() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setStringType", new Class<?>[] { String.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);

		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		Class<?>[] intfs = (Class[]) getPrivateProperty(pfb, "serviceTypes");
		assertEquals(intfs[0], String.class);

		setter = AnnotatedBean.class.getMethod("setIntType", new Class<?>[] { Integer.TYPE });
		ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);

		pfb = new OsgiServiceProxyFactoryBean();
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		intfs = (Class[]) getPrivateProperty(pfb, "serviceTypes");
		assertEquals(intfs[0], Integer.TYPE);

	}

	@Test
	public void testGetServicePropertyCardinality() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithCardinality1_1",
			new Class<?>[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		assertTrue(pfb.getAvailability() == Availability.MANDATORY);

		setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithCardinality0_1",
			new Class<?>[] { AnnotatedBean.class });
		ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		pfb = new OsgiServiceProxyFactoryBean();
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		assertFalse(pfb.getAvailability() == Availability.MANDATORY);
	}

	@Test
	public void testProperMultiCardinality() throws Exception {
		OsgiServiceCollectionProxyFactoryBean pfb = new OsgiServiceCollectionProxyFactoryBean();

		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithCardinality0_N",
			new Class<?>[] { List.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		assertFalse(pfb.getAvailability() == Availability.MANDATORY);

		setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithCardinality1_N",
			new Class<?>[] { SortedSet.class });
		ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		pfb = new OsgiServiceCollectionProxyFactoryBean();
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		assertTrue(pfb.getAvailability() == Availability.MANDATORY);
	}

	@Test
	public void testErrorMultiCardinality() throws Exception {
		OsgiServiceCollectionProxyFactoryBean pfb = new OsgiServiceCollectionProxyFactoryBean();

		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanErrorTypeWithCardinality1_N",
			new Class<?>[] { SortedSet.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		pfb = new OsgiServiceCollectionProxyFactoryBean();
		try {
			processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
			fail("IllegalArgumentException should have been thrown");
		}
		catch (Exception e) {
		}
	}

	@Test
	public void testGetServicePropertyClassloader() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithClassLoaderClient",
			new Class<?>[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		assertEquals(pfb.getImportContextClassLoader(), ImportContextClassLoaderEnum.CLIENT);

		pfb = new OsgiServiceProxyFactoryBean();
		setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithClassLoaderUmanaged",
			new Class<?>[] { AnnotatedBean.class });
		ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);

		assertEquals(pfb.getImportContextClassLoader(), ImportContextClassLoaderEnum.UNMANAGED);

		pfb = new OsgiServiceProxyFactoryBean();
		setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithClassLoaderServiceProvider",
			new Class<?>[] { AnnotatedBean.class });
		ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		assertEquals(pfb.getImportContextClassLoader(), ImportContextClassLoaderEnum.SERVICE_PROVIDER);
	}

	@Test
	public void testGetServicePropertyBeanName() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithBeanName",
			new Class<?>[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		String beanName = (String) getPrivateProperty(pfb, "serviceBeanName");
		;
		assertEquals(beanName, "myBean");
	}

	@Test
	public void testGetServicePropertyFilter() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithFilter",
			new Class<?>[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		String filter = (String) getPrivateProperty(pfb, "filter");
		;
		assertEquals(filter, "(wooey=fooo)");
	}

	@Test
	public void testGetServicePropertyServiceClass() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeWithServiceType",
			new Class<?>[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		Class<?>[] intfs = (Class[]) getPrivateProperty(pfb, "interfaces");
		assertEquals(intfs[0], Object.class);
	}

	@Test
	public void testGetServicePropertyComplex() throws Exception {
		OsgiServiceProxyFactoryBean pfb = new OsgiServiceProxyFactoryBean();
		Method setter = AnnotatedBean.class.getMethod("setAnnotatedBeanTypeComplex",
			new Class<?>[] { AnnotatedBean.class });
		ServiceReference ref = AnnotationUtils.getAnnotation(setter, ServiceReference.class);
		processor.getServiceProperty(pfb, ref, setter.getParameterTypes()[0], null);
		Class<?>[] intfs = (Class[]) getPrivateProperty(pfb, "interfaces");
		String filter = (String) getPrivateProperty(pfb, "filter");
		String beanName = (String) getPrivateProperty(pfb, "serviceBeanName");
		assertEquals(intfs[0], AnnotatedBean.class);
		assertFalse(pfb.getAvailability() == Availability.MANDATORY);
		assertEquals(ImportContextClassLoaderEnum.SERVICE_PROVIDER, pfb.getImportContextClassLoader());
		assertEquals(filter, "(id=fooey)");
		assertEquals(beanName, "myBean");
	}

	@Test
	public void testServiceBeanInjection() throws Exception {
		ServiceBean bean = new ServiceBean();
		final MyService bean1 = new MyService() {

			public Object getId() {
				return this;
			}
		};
		final Serializable bean2 = new Serializable() {

			public String toString() {
				return "bean2";
			}
		};

		BundleContext context = new MockBundleContext() {

			public Object getService(org.osgi.framework.ServiceReference reference) {
				String clazz = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
				if (clazz == null)
					return null;
				else if (clazz.equals(MyService.class.getName())) {
					return bean1;
				}
				else if (clazz.equals(Serializable.class.getName())) {
					return bean2;
				}
				return null;
			}

		};

		ServiceReferenceInjectionBeanPostProcessor p = new ServiceReferenceInjectionBeanPostProcessor();
		p.setBundleContext(context);
		p.setBeanClassLoader(getClass().getClassLoader());
		BeanFactory factory = EasyMock.createMock(BeanFactory.class);
		MocksControl factoryControl = MocksControl.getControl(factory);
		EasyMock.expect(factory.containsBean("&myBean")).andReturn(true);
		EasyMock.replay(factory);
    	p.setBeanFactory(factory);

		p.postProcessAfterInitialization(bean, "myBean");
		assertSame(bean1.getId(), bean.getServiceBean().getId());
		assertSame(bean2.toString(), bean.getSerializableBean().toString());

		factoryControl.verify();
	}

	@Test
	public void testServiceBeanWithAnnotatedFieldsInjection() throws Exception {
		ServiceBeanWithAnnotatedFields bean = new ServiceBeanWithAnnotatedFields();
		final MyService bean1 = new MyService() {

			public Object getId() {
				return this;
			}
		};
		final Serializable bean2 = new Serializable() {

			public String toString() {
				return "bean2";
			}
		};

		BundleContext context = new MockBundleContext() {

			public Object getService(org.osgi.framework.ServiceReference reference) {
				String clazz = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
				if (clazz == null)
					return null;
				else if (clazz.equals(MyService.class.getName())) {
					return bean1;
				}
				else if (clazz.equals(Serializable.class.getName())) {
					return bean2;
				}
				return null;
			}

		};

		ServiceReferenceInjectionBeanPostProcessor p = new ServiceReferenceInjectionBeanPostProcessor();
		p.setBundleContext(context);
		p.setBeanClassLoader(getClass().getClassLoader());
		BeanFactory factory = EasyMock.createMock(BeanFactory.class);
		EasyMock.replay(factory);
		p.setBeanFactory(factory);

		p.postProcessBeforeInitialization(bean, "myBean");
		assertSame(bean1.getId(), bean.getServiceBean().getId());
		assertSame(bean2.toString(), bean.getSerializableBean().toString());
	}

	@Test
	public void testServiceFactoryBeanNotInjected() throws Exception {
		ServiceFactoryBean bean = new ServiceFactoryBean();
		final MyService bean1 = new MyService() {

			public Object getId() {
				return this;
			}
		};
		final Serializable bean2 = new Serializable() {

			public String toString() {
				return "bean2";
			}
		};

		BundleContext context = new MockBundleContext() {

			public Object getService(org.osgi.framework.ServiceReference reference) {
				String clazz = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
				if (clazz == null)
					return null;
				else if (clazz.equals(MyService.class.getName())) {
					return bean1;
				}
				else if (clazz.equals(Serializable.class.getName())) {
					return bean2;
				}
				return null;
			}

		};

		ServiceReferenceInjectionBeanPostProcessor p = new ServiceReferenceInjectionBeanPostProcessor();
		p.setBundleContext(context);
		p.postProcessAfterInitialization(bean, "myBean");
		assertNull(bean.getServiceBean());
		assertNull(bean.getSerializableBean());
	}

	@Test
	public void testServiceFactoryBeanInjected() throws Exception {
		ServiceFactoryBean bean = new ServiceFactoryBean();
		final MyService bean1 = new MyService() {

			public Object getId() {
				return this;
			}
		};
		final Serializable bean2 = new Serializable() {

			public String toString() {
				return "bean2";
			}
		};

		BundleContext context = new MockBundleContext() {

			public Object getService(org.osgi.framework.ServiceReference reference) {
				String clazz = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
				if (clazz == null)
					return null;
				else if (clazz.equals(MyService.class.getName())) {
					return bean1;
				}
				else if (clazz.equals(Serializable.class.getName())) {
					return bean2;
				}
				return null;
			}

		};

		ServiceReferenceInjectionBeanPostProcessor p = new ServiceReferenceInjectionBeanPostProcessor();
		p.setBundleContext(context);
		p.setBeanClassLoader(getClass().getClassLoader());
		PropertyValues pvs = p.postProcessPropertyValues(new MutablePropertyValues(), new PropertyDescriptor[] {
			new PropertyDescriptor("serviceBean", ServiceFactoryBean.class),
			new PropertyDescriptor("serializableBean", ServiceFactoryBean.class) }, bean, "myBean");

		MyService msb = (MyService) pvs.getPropertyValue("serviceBean").getValue();
		Serializable ssb = (Serializable) pvs.getPropertyValue("serializableBean").getValue();

		assertNotNull(msb);
		assertNotNull(ssb);

		assertSame(bean1.getId(), msb.getId());
		assertSame(bean2.toString(), ssb.toString());
	}

	@Test
	public void testServiceBeanInjectedValues() throws Exception {
		ServiceBean bean = new ServiceBean();
		final MyService bean1 = new MyService() {

			public Object getId() {
				return this;
			}
		};
		final Serializable bean2 = new Serializable() {

			public String toString() {
				return "bean2";
			}
		};

		BundleContext context = new MockBundleContext() {

			public Object getService(org.osgi.framework.ServiceReference reference) {
				String clazz = ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
				if (clazz == null)
					return null;
				else if (clazz.equals(MyService.class.getName())) {
					return bean1;
				}
				else if (clazz.equals(Serializable.class.getName())) {
					return bean2;
				}
				return null;
			}

		};

		ServiceReferenceInjectionBeanPostProcessor p = new ServiceReferenceInjectionBeanPostProcessor();
		p.setBundleContext(context);
		p.setBeanClassLoader(getClass().getClassLoader());
		PropertyValues pvs = p.postProcessPropertyValues(new MutablePropertyValues(), new PropertyDescriptor[] {
			new PropertyDescriptor("serviceBean", ServiceBean.class),
			new PropertyDescriptor("serializableBean", ServiceBean.class) }, bean, "myBean");

		MyService msb = (MyService) pvs.getPropertyValue("serviceBean").getValue();
		Serializable ssb = (Serializable) pvs.getPropertyValue("serializableBean").getValue();

		assertNotNull(msb);
		assertNotNull(ssb);

		assertSame(bean1.getId(), msb.getId());
		assertSame(bean2.toString(), ssb.toString());
	}

	protected Object getPrivateProperty(final Object target, final String fieldName) {
		final Field foundField[] = new Field[1];

		ReflectionUtils.doWithFields(target.getClass(), new FieldCallback() {

			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				field.setAccessible(true);
				foundField[0] = field;
			}

		}, new FieldFilter() {

			public boolean matches(Field field) {
				return fieldName.equals(field.getName());
			}

		});

		try {
			return foundField[0].get(target);
		}
		catch (Exception ex) {
			// translate
			throw new RuntimeException(ex);
		}
	}
}