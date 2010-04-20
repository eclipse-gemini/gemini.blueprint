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

package org.eclipse.gemini.blueprint.io.internal.resolver;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.io.internal.OsgiHeaderUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * {@link PackageAdmin} based dependency resolver.
 * 
 * <p/>
 * This implementation uses the OSGi PackageAdmin service to determine
 * dependencies between bundles. Since it's highly dependent on an external
 * service, it might be better to use a listener based implementation for poor
 * performing environments.
 * 
 * <p/>
 * This implementation does consider required bundles.
 * 
 * @author Costin Leau
 * 
 */
public class PackageAdminResolver implements DependencyResolver {

	/** logger */
	private static final Log log = LogFactory.getLog(PackageAdminResolver.class);

	private final BundleContext bundleContext;


	public PackageAdminResolver(BundleContext bundleContext) {
		Assert.notNull(bundleContext);
		this.bundleContext = bundleContext;
	}

	public ImportedBundle[] getImportedBundles(Bundle bundle) {
		boolean trace = log.isTraceEnabled();

		PackageAdmin pa = getPackageAdmin();

		// create map with bundles as keys and a list of packages as value
		Map<Bundle, List<String>> importedBundles = new LinkedHashMap<Bundle, List<String>>(8);

		// 1. consider required bundles first

		// see if there are required bundle(s) defined
		String[] entries = OsgiHeaderUtils.getRequireBundle(bundle);

		// 1. if so, locate the bundles
		for (int i = 0; i < entries.length; i++) {
			String[] parsed = OsgiHeaderUtils.parseRequiredBundleString(entries[i]);
			// trim the strings just to be on the safe side (some implementations allows whitespaces, some don't)
			String symName = parsed[0].trim();
			String versionRange = parsed[1].trim();
			Bundle[] foundBundles = pa.getBundles(symName, versionRange);

			if (!ObjectUtils.isEmpty(foundBundles)) {
				Bundle requiredBundle = foundBundles[0];

				// find exported packages
				ExportedPackage[] exportedPackages = pa.getExportedPackages(requiredBundle);
				if (exportedPackages != null)
					addExportedPackages(importedBundles, requiredBundle, exportedPackages);
			}
			else {
				if (trace) {
					log.trace("Cannot find required bundle " + symName + "|" + versionRange);
				}
			}
		}

		// 2. determine imported bundles 
		// get all bundles
		Bundle[] bundles = bundleContext.getBundles();

		for (int i = 0; i < bundles.length; i++) {
			Bundle analyzedBundle = bundles[i];
			// if the bundle is already included (it's a required one), there's no need to look at it again
			if (!importedBundles.containsKey(analyzedBundle)) {
				ExportedPackage[] epa = pa.getExportedPackages(analyzedBundle);
				if (epa != null)
					for (int j = 0; j < epa.length; j++) {
						ExportedPackage exportedPackage = epa[j];
						Bundle[] importingBundles = exportedPackage.getImportingBundles();
						if (importingBundles != null)
							for (int k = 0; k < importingBundles.length; k++) {
								if (bundle.equals(importingBundles[k])) {
									addImportedBundle(importedBundles, exportedPackage);
								}
							}
					}
			}
		}

		List<ImportedBundle> importedBundlesList = new ArrayList<ImportedBundle>(importedBundles.size());

		for (Map.Entry<Bundle, List<String>> entry : importedBundles.entrySet()) {
			Bundle importedBundle = entry.getKey();
			List<String> packages = entry.getValue();
			importedBundlesList.add(new ImportedBundle(importedBundle,
				(String[]) packages.toArray(new String[packages.size()])));
		}

		return (ImportedBundle[]) importedBundlesList.toArray(new ImportedBundle[importedBundlesList.size()]);
	}

	/**
	 * Adds the imported bundle to the map of packages.
	 * 
	 * @param map
	 * @param bundle
	 * @param packageName
	 */
	private void addImportedBundle(Map<Bundle, List<String>> map, ExportedPackage expPackage) {
		Bundle bnd = expPackage.getExportingBundle();
		List<String> packages = map.get(bnd);
		if (packages == null) {
			packages = new ArrayList<String>(4);
			map.put(bnd, packages);
		}
		packages.add(new String(expPackage.getName()));
	}

	/**
	 * Adds the bundle exporting the given packages which are then imported by
	 * the owning bundle. This applies to special imports (such as
	 * Require-Bundle).
	 * 
	 * @param map
	 * @param bundle
	 * @param pkgs
	 */
	private void addExportedPackages(Map<Bundle, List<String>> map, Bundle bundle, ExportedPackage[] pkgs) {
		List<String> packages = map.get(bundle);
		if (packages == null) {
			packages = new ArrayList<String>(pkgs.length);
			map.put(bundle, packages);
		}
		for (int i = 0; i < pkgs.length; i++) {
			packages.add(pkgs[i].getName());
		}
	}

	private PackageAdmin getPackageAdmin() {

		return AccessController.doPrivileged(new PrivilegedAction<PackageAdmin>() {

			public PackageAdmin run() {
				ServiceReference ref = bundleContext.getServiceReference(PackageAdmin.class.getName());
				if (ref == null)
					throw new IllegalStateException(PackageAdmin.class.getName() + " service is required");
				// don't do any proxying since PackageAdmin is normally a framework service
				// we can assume for now that it will always be available
				return (PackageAdmin) bundleContext.getService(ref);
			}
		});
	}
}