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

package org.eclipse.gemini.blueprint.iandt.syntheticEvents;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.util.OsgiServiceUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Integration test for synthetic events delivery of service listeners during
 * startup/shutdown.
 * 
 * @author Costin Leau
 * 
 */
public class ServiceListenerSyntheticEvents extends BaseIntegrationTest {

	private Shape area, rectangle, polygon;

	private ServiceRegistration areaReg, rectangleReg, polygonReg;

	private OsgiBundleXmlApplicationContext appCtx;

	private static List referenceBindServices, referenceUnbindServices;

	private static List collectionBindServices, collectionUnbindServices;


	public static class ReferenceListener {

		public void bind(Object service, Map properties) {
			referenceBindServices.add(service.toString());
		};

		public void unbind(Object service, Map properties) {
			referenceUnbindServices.add(service.toString());
		};
	}

	public static class CollectionListener {

		public void bind(Object service, Map properties) {
			collectionBindServices.add(service.toString());
		};

		public void unbind(Object service, Map properties) {
			collectionUnbindServices.add(service.toString());
		};
	}


	// register multiple services of the same type inside OSGi space
	private void registerMultipleServices() {
		area = new Area();
		rectangle = new Rectangle();
		polygon = new Polygon();

		Dictionary polygonProp = new Properties();
		polygonProp.put(Constants.SERVICE_RANKING, Integer.valueOf(1));
		// first register polygon
		polygonReg = bundleContext.registerService(Shape.class.getName(), polygon, polygonProp);

		// then rectangle
		Dictionary rectangleProp = new Properties();
		rectangleProp.put(Constants.SERVICE_RANKING, Integer.valueOf(10));
		rectangleReg = bundleContext.registerService(Shape.class.getName(), rectangle, rectangleProp);

		// then area
		Dictionary areaProp = new Properties();
		areaProp.put(Constants.SERVICE_RANKING, Integer.valueOf(100));
		areaReg = bundleContext.registerService(Shape.class.getName(), area, areaProp);

	}

	protected void onSetUp() {
		referenceBindServices = new ArrayList();
		referenceUnbindServices = new ArrayList();
		collectionBindServices = new ArrayList();
		collectionUnbindServices = new ArrayList();
	}

	protected void onTearDown() {
		OsgiServiceUtils.unregisterService(areaReg);
		OsgiServiceUtils.unregisterService(rectangleReg);
		OsgiServiceUtils.unregisterService(polygonReg);
		try {
			if (appCtx != null)
				appCtx.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		referenceBindServices = null;
		referenceUnbindServices = null;
		collectionBindServices = null;
		collectionUnbindServices = null;
	}

	private void createAppCtx() {
		appCtx = new OsgiBundleXmlApplicationContext(
			new String[] { "/org/eclipse/gemini/blueprint/iandt/syntheticEvents/importers.xml" });
		appCtx.setBundleContext(bundleContext);
		appCtx.refresh();
	}

	// create appCtx each time since we depend we test startup/shutdown behaviour
	// and cannot have shared states
	@Test
	public void testServiceReferenceEventsOnStartupWithMultipleServicesPresent() throws Exception {
		registerMultipleServices();
		createAppCtx();

		assertEquals("only one service bound at startup", 1, referenceBindServices.size());
		assertEquals("wrong service bound", area.toString(), referenceBindServices.get(0).toString());
	}

	@Test
	public void testServiceReferenceEventsDuringLifetimeWithMultipleServicesPresent() throws Exception {
		createAppCtx();
		registerMultipleServices();

		assertEquals("multiple services should have been bound during runtime", 3, referenceBindServices.size());
		assertEquals("wrong 1st service bound", polygon.toString(), referenceBindServices.get(0).toString());
		assertEquals("wrong 2nd service bound", rectangle.toString(), referenceBindServices.get(1).toString());
		assertEquals("wrong 3rd service bound", area.toString(), referenceBindServices.get(2).toString());
	}

	@Test
	public void testServiceReferenceEventsOnShutdownWithMultipleServicesPresent() throws Exception {
		createAppCtx();
		registerMultipleServices();
		appCtx.close();

		assertEquals("only one service unbound at shutdown", 1, referenceUnbindServices.size());
		assertEquals("wrong unbind at shutdown", area.toString(), referenceUnbindServices.get(0).toString());
		appCtx = null;
	}

	@Test
	public void testServiceCollectionEventsOnStartupWithMultipleServicesPresent() throws Exception {
		registerMultipleServices();
		createAppCtx();

		assertEquals("all services should have been bound at startup", 3, collectionBindServices.size());
		assertEquals("wrong service bound", polygon.toString(), collectionBindServices.get(0).toString());
		assertEquals("wrong service bound", rectangle.toString(), collectionBindServices.get(1).toString());
		assertEquals("wrong service bound", area.toString(), collectionBindServices.get(2).toString());

	}

	@Test
	public void testServiceCollectionEventsDuringLifetimeWithMultipleServicesPresent() throws Exception {
		createAppCtx();
		registerMultipleServices();

		assertEquals("multiple services should have been bound during runtime", 3, referenceBindServices.size());
		assertEquals("wrong 1st service bound", polygon.toString(), collectionBindServices.get(0).toString());
		assertEquals("wrong 2nd service bound", rectangle.toString(), collectionBindServices.get(1).toString());
		assertEquals("wrong 3rd service bound", area.toString(), collectionBindServices.get(2).toString());
	}

	@Test
	public void testServiceCollectionEventsOnShutdownWithMultipleServicesPresent() throws Exception {
		createAppCtx();
		registerMultipleServices();
		appCtx.close();

		assertEquals("all services should have been bound at startup", 3, collectionUnbindServices.size());
		assertEquals("wrong 1st service bound", polygon.toString(), collectionUnbindServices.get(0).toString());
		assertEquals("wrong 2nd service bound", rectangle.toString(), collectionUnbindServices.get(1).toString());
		assertEquals("wrong 3rd service bound", area.toString(), collectionUnbindServices.get(2).toString());
		appCtx = null;
	}

}
