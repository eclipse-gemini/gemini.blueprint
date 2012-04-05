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

package org.eclipse.gemini.blueprint.iandt.nonosgicl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.Constants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;

/**
 * Integration test that checks whether the non-osgi classloader for JDK/OSGi
 * classes are properly considered when creating service proxies.
 * 
 * @author Costin Leau
 */
public class NonOSGiLoaderProxyTest extends BaseIntegrationTest {

	// a service that implements several custom classes
	private class Service implements DataSource, Comparator, InitializingBean, Constants {

		public Connection getConnection() throws SQLException {
			return null;
		}

		public Connection getConnection(String username, String password) throws SQLException {
			return null;
		}

		public int getLoginTimeout() throws SQLException {
			return 0;
		}

		public PrintWriter getLogWriter() throws SQLException {
			return null;
		}

		public void setLoginTimeout(int seconds) throws SQLException {
		}

		public void setLogWriter(PrintWriter out) throws SQLException {
		}

		public void afterPropertiesSet() throws Exception {
		}

		public int compare(Object arg0, Object arg1) {
			return 0;
		}

		public boolean isWrapperFor(Class<?> iface) {
			return false;
		}

		public <T> T unwrap(Class<T> iface) throws SQLException {
			return null;
		}

        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }
    }

	public void testProxy() throws Exception {
		// publish service
		bundleContext.registerService(new String[] { DataSource.class.getName(), Comparator.class.getName(),
			InitializingBean.class.getName(), Constants.class.getName() }, new Service(), new Hashtable());

		ConfigurableApplicationContext ctx = getNestedContext();
		assertNotNull(ctx);
		Object proxy = ctx.getBean("service");
		assertNotNull(proxy);
		assertTrue(proxy instanceof DataSource);
		assertTrue(proxy instanceof Comparator);
		assertTrue(proxy instanceof Constants);
		assertTrue(proxy instanceof InitializingBean);
		ctx.close();
	}

	private ConfigurableApplicationContext getNestedContext() throws Exception {
		OsgiBundleXmlApplicationContext ctx = new OsgiBundleXmlApplicationContext(
			new String[] { "org/eclipse/gemini/blueprint/iandt/nonosgicl/context.xml" });

		ctx.setBundleContext(bundleContext);
		ctx.refresh();
		return ctx;
	}
}