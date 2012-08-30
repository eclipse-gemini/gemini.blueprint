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

package org.eclipse.gemini.blueprint.extender.internal.dependencies.shutdown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.springframework.util.ObjectUtils;

/**
 * Utility for sorting out bundles during shutdown based on the OSGi 4.2 shutdown algorithm. Please see section 121.3.11
 * in OSGi 4.2 release. Since sorting out the entire graph from the beginning is difficult (shutting down some bundles,
 * might allow others to be destroyed), this utility is meant to be called multiple times until the list is being
 * depleted.
 * 
 * @author Costin Leau
 */
public abstract class ShutdownSorter {

	private static final Log log = LogFactory.getLog(ShutdownSorter.class);

	/**
	 * Sorts the given bundles. The method extracts the bundles about to be destroyed from the given lists and returns
	 * them to the user. Since shutting down a bundle can influence the destruction of the others, this method should be
	 * called after all the returned bundles have been destroyed until the list is empty.
	 * 
	 * @param managedBundles
	 * @return
	 */
	public static Collection<Bundle> getBundles(Collection<Bundle> managedBundles) {

		List<Bundle> returned = null;
		try {
			// 1. eliminate unused bundles
			returned = unusedBundles(managedBundles);
			if (returned.isEmpty()) {
				// go to step 2, and pick the first bundle based on service properties
				returned = new ArrayList<Bundle>(1);
				returned.add(findBundleBasedOnServices(managedBundles));
			}
			return returned;
		} finally {
		    if (returned != null) {
		        managedBundles.removeAll(returned);
		    }
		}
	}

	private static List<Bundle> unusedBundles(Collection<Bundle> unsorted) {
		List<Bundle> unused = new ArrayList<Bundle>();

		boolean trace = log.isTraceEnabled();

		for (Bundle bundle : unsorted) {
			String bundleToString = null;
			if (trace) {
				bundleToString = OsgiStringUtils.nullSafeSymbolicName(bundle);
			}
			ServiceReference[] services = bundle.getRegisteredServices();
			if (ObjectUtils.isEmpty(services)) {
				if (trace) {
					log.trace("Bundle " + bundleToString + " has no registered services; added for shutdown");
				}
				unused.add(bundle);
			} else {
				boolean unusedBundle = true;
				for (ServiceReference serviceReference : services) {
					Bundle[] usingBundles = serviceReference.getUsingBundles();
					if (!ObjectUtils.isEmpty(usingBundles)) {
						if (trace)
							log.trace("Bundle " + bundleToString
									+ " has registered services in use; postponing shutdown. The using bundles are "
									+ Arrays.toString(usingBundles));
						unusedBundle = false;
						break;
					}

				}
				if (unusedBundle) {
					if (trace) {
						log.trace("Bundle " + bundleToString + " has unused registered services; added for shutdown");
					}
					unused.add(bundle);
				}
			}
		}

		Collections.sort(unused, ReverseBundleIdSorter.INSTANCE);

		return unused;
	}

	private static Bundle findBundleBasedOnServices(Collection<Bundle> managedBundles) {
		Bundle candidate = null;
		int ranking = 0;
		boolean tie = false;

		boolean trace = log.isTraceEnabled();

		String bundleToString = null;

		for (Bundle bundle : managedBundles) {
			if (trace) {
				bundleToString = OsgiStringUtils.nullSafeSymbolicName(bundle);
			}

			int localRanking = getRegisteredServiceInUseLowestRanking(bundle);

			if (trace) {
				log.trace("Bundle " + bundleToString + " lowest ranking registered service is " + localRanking);
			}
			if (candidate == null) {
				candidate = bundle;
				ranking = localRanking;
			} else {
				if (localRanking < ranking) {
					candidate = bundle;
					tie = false;
					ranking = localRanking;
				} else if (localRanking == ranking) {
					tie = true;
				}
			}
		}

		// there's a tie, so search for the bundle with the highest service id
		if (tie) {

			if (trace) {
				log.trace("Ranking tie; Looking for the highest service id...");
			}

			long serviceId = Long.MIN_VALUE;

			for (Bundle bundle : managedBundles) {
				if (trace) {
					bundleToString = OsgiStringUtils.nullSafeSymbolicName(bundle);
				}

				long localServiceId = getHighestServiceId(bundle);
				if (trace) {
					log.trace("Bundle " + bundleToString + " highest service id is " + localServiceId);
				}

				if (localServiceId > serviceId) {
					candidate = bundle;
					serviceId = localServiceId;
				}
			}

			if (trace) {
				log.trace("The bundle with the highest service id is "
						+ OsgiStringUtils.nullSafeSymbolicName(candidate));
			}
		} else {
			if (trace) {
				log.trace("No ranking tie. The bundle with the lowest ranking is "
						+ OsgiStringUtils.nullSafeSymbolicName(candidate));
			}
		}

		return candidate;
	}

	private static int getRegisteredServiceInUseLowestRanking(Bundle bundle) {
		ServiceReference[] services = bundle.getRegisteredServices();
		int min = Integer.MAX_VALUE;
		if (!ObjectUtils.isEmpty(services)) {
			for (ServiceReference ref : services) {
				// make sure somebody is using the service
				if (!ObjectUtils.isEmpty(ref.getUsingBundles())) {
					int localRank = OsgiServiceReferenceUtils.getServiceRanking(ref);
					if (localRank < min) {
						min = localRank;
					}
				}
			}
		}
		return min;
	}

	private static long getHighestServiceId(Bundle bundle) {
		ServiceReference[] services = bundle.getRegisteredServices();
		long max = Long.MIN_VALUE;
		if (!ObjectUtils.isEmpty(services)) {
			for (ServiceReference ref : services) {
				long id = OsgiServiceReferenceUtils.getServiceId(ref);
				if (id > max) {
					max = id;
				}
			}
		}
		return max;
	}

	static class ReverseBundleIdSorter implements Comparator<Bundle> {

		private static Comparator<Bundle> INSTANCE = new ReverseBundleIdSorter();

		public int compare(Bundle o1, Bundle o2) {
			return (int) (o2.getBundleId() - o1.getBundleId());
		}
	}
}