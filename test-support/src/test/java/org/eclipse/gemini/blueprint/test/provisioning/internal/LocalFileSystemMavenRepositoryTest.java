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

package org.eclipse.gemini.blueprint.test.provisioning.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Costin Leau
 * 
 */
public class LocalFileSystemMavenRepositoryTest {

	private LocalFileSystemMavenRepository repository;

	@After
	public void tearDown() throws Exception {
		System.getProperties().remove("localRepository");
	}

	@Test
	public void testSystemProperty() throws Exception {
		String SYS_PROP = "fake/sys/location";
		System.setProperty("localRepository", SYS_PROP);
		repository = new LocalFileSystemMavenRepository();
		assertTrue("system property not used", repository.locateArtifact("foo", "bar", "1.0").toString().indexOf(
			SYS_PROP) >= -1);
	}

	@Test
	public void testLocalSettingsFile() throws Exception {
		repository = new LocalFileSystemMavenRepository();
		Resource res = new ClassPathResource("/org/eclipse/gemini/blueprint/test/provisioning/internal/settings.xml");
		String location = repository.getMavenSettingsLocalRepository(res);
		assertNotNull("location hasn't been picked up", location);
		assertEquals("wrong location discovered", location, "fake/location");
	}
}
