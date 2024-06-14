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

package org.eclipse.gemini.blueprint.iandt;

import java.io.File;
import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyPermission;
import java.util.jar.Manifest;

import org.eclipse.gemini.blueprint.test.AbstractConfigurableBundleCreatorTests;
import org.eclipse.gemini.blueprint.test.platform.OsgiPlatform;
import org.eclipse.gemini.blueprint.test.provisioning.ArtifactLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundlePermission;
import org.osgi.framework.Constants;
import org.osgi.framework.PackagePermission;
import org.osgi.framework.ServicePermission;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Base test class used for improving performance of integration tests by
 * creating bundles only with the classes within a package as opposed to all
 * resources available in the target folder.
 * 
 * <p/>
 * Additionally, the class checks for the presence Clover if a certain property
 * is set and uses a special setup to use the instrumented jars instead of the
 * naked ones.
 * 
 * @author Costin Leau
 * 
 */
public abstract class BaseIntegrationTest extends AbstractConfigurableBundleCreatorTests {

	private class CloverClassifiedArtifactLocator implements ArtifactLocator {

		private final ArtifactLocator delegate;


		public CloverClassifiedArtifactLocator(ArtifactLocator delegate) {
			this.delegate = delegate;
		}

		public Resource locateArtifact(String group, String id, String version, String type) {
			return parse(id + "-" + version, delegate.locateArtifact(group, id, version, type));
		}

		public Resource locateArtifact(String group, String id, String version) {
			return parse(id + "-" + version, delegate.locateArtifact(group, id, version));
		}

		private Resource parse(String id, Resource resource) {
			if (id.indexOf(SPRING_DM_PREFIX) > -1) {
				try {
					String relativePath = "";
					// check if it's a relative file
					if (StringUtils.cleanPath(resource.getURI().toString()).indexOf("/target/") > -1) {
						relativePath = "clover" + File.separator;
					}
					relativePath = relativePath + id + "-clover.jar";

					Resource res = resource.createRelative(relativePath);
					BaseIntegrationTest.this.logger.info("Using clover instrumented jar " + res.getDescription());
					return res;
				}
				catch (Exception ex) {
					throw (RuntimeException) new IllegalStateException(
						"Trying to find Clover instrumented class but none is available; disable clover or build the instrumented artifacts").initCause(ex);
				}
			}
			return resource;
		}
	}

	private static final String CLOVER_PROPERTY = "org.eclipse.gemini.blueprint.integration.testing.clover";

	private static final String CLOVER_PKG = "com_cenqua_clover";

	private static final String SPRING_DM_PREFIX = "spring-osgi";


	protected String[] getBundleContentPattern() {
		String pkg = getClass().getPackage().getName().replace('.', '/').concat("/");
		String[] patterns = new String[] { BaseIntegrationTest.class.getName().replace('.', '/').concat("*.class"),
			pkg + "**/*" };
		return patterns;
	}

	protected void preProcessBundleContext(BundleContext context) throws Exception {
		super.preProcessBundleContext(context);

		if (isCloverEnabled()) {
			logger.warn("Test coverage instrumentation (Clover) enabled");
		}
	}

	private boolean isCloverEnabled() {
		return Boolean.getBoolean(CLOVER_PROPERTY);
	}

	protected ArtifactLocator getLocator() {
		ArtifactLocator defaultLocator = super.getLocator();
		// redirect to the clover artifacts
		if (isCloverEnabled()) {
			return new CloverClassifiedArtifactLocator(defaultLocator);
		}
		return defaultLocator;
	}

	protected List getBootDelegationPackages() {
		List bootPkgs = super.getBootDelegationPackages();
		if (isCloverEnabled()) {
			bootPkgs.add(CLOVER_PKG);
		}
		return bootPkgs;
	}

	protected Manifest getManifest() {
		String permissionPackage = "org.osgi.service.permissionadmin";
		Manifest mf = super.getManifest();
		// make permission admin packages optional
		String impPackage = mf.getMainAttributes().getValue(Constants.IMPORT_PACKAGE);
		int startIndex = impPackage.indexOf(permissionPackage);
		String newImpPackage = impPackage;
		if (startIndex >= 0) {
			newImpPackage = impPackage.substring(0, startIndex) + permissionPackage + ";resolution:=optional"
					+ impPackage.substring(startIndex + permissionPackage.length());
		}
		mf.getMainAttributes().putValue(Constants.IMPORT_PACKAGE, newImpPackage);
		return mf;
	}

	/**
	 * Returns the list of permissions for the running test.
	 * 
	 * @return
	 */
	protected List<Permission> getTestPermissions() {
		List<Permission> perms = new ArrayList<Permission>();
		perms.add(new PackagePermission("*", PackagePermission.EXPORT));
		perms.add(new PackagePermission("*", PackagePermission.IMPORT));
		perms.add(new BundlePermission("*", BundlePermission.HOST));
		perms.add(new BundlePermission("*", BundlePermission.PROVIDE));
		perms.add(new BundlePermission("*", BundlePermission.REQUIRE));
		perms.add(new ServicePermission("*", ServicePermission.REGISTER));
		perms.add(new ServicePermission("*", ServicePermission.GET));
		perms.add(new PropertyPermission("*", "read,write"));
		// required by Spring
		perms.add(new RuntimePermission("*", "accessDeclaredMembers"));
		perms.add(new ReflectPermission("*", "suppressAccessChecks"));
		// logging permission
		perms.add(new FilePermission("-", "write"));
		perms.add(new FilePermission("-", "read"));
		return perms;
	}

	protected List<Permission> getIAndTPermissions() {
		List<Permission> perms = new ArrayList<Permission>();
		// export package
		perms.add(new PackagePermission("*", PackagePermission.EXPORT));
		perms.add(new PackagePermission("*", PackagePermission.IMPORT));
		perms.add(new BundlePermission("*", BundlePermission.FRAGMENT));
		perms.add(new BundlePermission("*", BundlePermission.PROVIDE));
		perms.add(new ServicePermission("*", ServicePermission.REGISTER));
		perms.add(new ServicePermission("*", ServicePermission.GET));
		perms.add(new PropertyPermission("*", "read,write"));

		// required by Spring
		perms.add(new RuntimePermission("*", "accessDeclaredMembers"));
		perms.add(new ReflectPermission("*", "suppressAccessChecks"));

		// logging permission
		perms.add(new FilePermission("-", "write"));
		perms.add(new FilePermission("-", "read"));

		return perms;
	}

	@Override
	protected OsgiPlatform createPlatform() {
		OsgiPlatform platform = super.createPlatform();
		
		
		platform.getConfigurationProperties().setProperty("org.osgi.framework.storage.clean", "onFirstInit");
		
		platform.getConfigurationProperties().setProperty("felix.fragment.validation", "warning");
        platform.getConfigurationProperties().setProperty("felix.log.level", "4");

        // todo: set system property to point to logback file.
        // todo: figure out a better way to attach logback includes.xml files.

		
		// KF 3.1 settings
		//platform.getConfigurationProperties().setProperty("org.knopflerfish.framework.all_signed", "false");
		//platform.getConfigurationProperties().setProperty("org.knopflerfish.framework.debug.classloader", "true");
		//platform.getConfigurationProperties().setProperty("org.knopflerfish.framework.bundlestorage", "file");
		
		return platform;
	}
}