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

package org.eclipse.gemini.blueprint.service.dependency.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.service.exporter.support.internal.controller.ExporterControllerUtils;
import org.eclipse.gemini.blueprint.service.exporter.support.internal.controller.ExporterInternalActions;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceDependency;
import org.eclipse.gemini.blueprint.service.importer.support.AbstractOsgiServiceImportFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.Availability;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean;
import org.eclipse.gemini.blueprint.service.importer.support.internal.controller.ImporterControllerUtils;
import org.eclipse.gemini.blueprint.service.importer.support.internal.controller.ImporterInternalActions;
import org.eclipse.gemini.blueprint.service.importer.support.internal.dependency.ImporterStateListener;
import org.eclipse.gemini.blueprint.util.internal.BeanFactoryUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation of {@link MandatoryServiceDependencyManager} which determines the relationship between
 * importers and exporters and unpublishes exported service if they dependent, transitively, on imported OSGi services
 * that are mandatory and cannot be satisfied.
 * 
 * <strong>Note:</strong> aimed for singleton beans only
 * 
 * @author Costin Leau
 * 
 */
public class DefaultMandatoryDependencyManager implements MandatoryServiceDependencyManager, BeanFactoryAware,
		DisposableBean {

	/**
	 * Importer state listener that gets associated with each exporter.
	 * 
	 * @author Costin Leau
	 */
	private class ImporterDependencyListener implements ImporterStateListener {

		private final Object exporter;
		private final String exporterName;

		private ImporterDependencyListener(Object exporter) {
			this.exporter = exporter;
			this.exporterName = (String) exporterToName.get(exporter);

		}

		public void importerSatisfied(Object importer, OsgiServiceDependency dependency) {

			boolean trace = log.isTraceEnabled();
			boolean exporterRemoved = false;

			// update importer status
			synchronized (exporter) {
				Map<Object, Boolean> importers = exporterToImporterDeps.get(exporter);
				exporterRemoved = !(importers != null);

				// if the list is not present (exporter was removed), bail out
				if (!exporterRemoved) {
					importers.put(importer, Boolean.TRUE);
					if (trace)
						log.trace("Importer [" + importerToName.get(importer)
								+ "] is satisfied; checking the rest of the dependencies for exporter "
								+ exporterToName.get(exporter));

					checkIfExporterShouldStart(exporter, importers);
				}
			}

			if (exporterRemoved && trace)
				log.trace("Exporter [" + exporterName + "] removed; ignoring dependency [" + dependency.getBeanName()
						+ "] update");
		}

		public void importerUnsatisfied(Object importer, OsgiServiceDependency dependency) {

			boolean exporterRemoved = false;

			synchronized (exporter) {
				Map<Object, Boolean> importers = exporterToImporterDeps.get(exporter);
				exporterRemoved = !(importers != null);
				if (!exporterRemoved) {
					// record the importer status
					importers.put(importer, Boolean.FALSE);
				}
			}

			boolean trace = log.isTraceEnabled();

			if (!exporterRemoved) {
				if (trace)
					log.trace("Exporter [" + exporterName + "] stopped; transitive OSGi dependency ["
							+ dependency.getBeanName() + "] is unsatifised");

				// if the importer goes down, simply shut down the exporter
				stopExporter(exporter);
			} else {
				if (trace) {
					log.trace("Exporter [" + exporterName + "] removed; ignoring dependency ["
							+ dependency.getBeanName() + "] update");
				}
			}
		}
	}

	private static final Log log = LogFactory.getLog(DefaultMandatoryDependencyManager.class);

	/** cache map - useful for avoiding double registration */
	private final ConcurrentMap<String, Object> exportersSeen = new ConcurrentHashMap<String, Object>(4);

	private static final Object VALUE = new Object();

	/**
	 * Importers on which an exporter depends. The exporter instance is used as a key, while the value is represented by
	 * a list of importers name and their status (up or down).
	 */
	private final Map<Object, Map<Object, Boolean>> exporterToImporterDeps =
			new ConcurrentHashMap<Object, Map<Object, Boolean>>(8);

	/** exporter -> importer listener map */
	private final Map<Object, ImporterStateListener> exporterListener =
			new ConcurrentHashMap<Object, ImporterStateListener>(8);

	/** importer -> name map */
	private final ConcurrentMap<Object, String> importerToName = new ConcurrentHashMap<Object, String>(8);

	/** exporter name map */
	private final Map<Object, String> exporterToName = new ConcurrentHashMap<Object, String>(8);

	/** owning bean factory */
	private ConfigurableListableBeanFactory beanFactory;

	public void addServiceExporter(Object exporter, String exporterBeanName) {
		Assert.hasText(exporterBeanName);

		if (exportersSeen.putIfAbsent(exporterBeanName, VALUE) == null) {

			String beanName = exporterBeanName;

			if (beanFactory.isFactoryBean(exporterBeanName))
				beanName = BeanFactory.FACTORY_BEAN_PREFIX + exporterBeanName;

			// check if it's factory bean (no need to check for abstract
			// definition since we're called by a BPP)
			if (!beanFactory.isSingleton(beanName)) {
				log.info("Exporter [" + beanName + "] is not singleton and will not be tracked");
			}

			else {
				if (log.isDebugEnabled())
					log.debug("Exporter [" + beanName + "] is being tracked for dependencies");

				exporterToName.put(exporter, exporterBeanName);
				// retrieve associated controller
				ExporterInternalActions controller = ExporterControllerUtils.getControllerFor(exporter);

				// disable publication at startup
				controller.registerServiceAtStartup(false);

				// populate the dependency maps
				discoverDependentImporterFor(exporterBeanName, exporter);
			}
		}
	}

	/**
	 * Discover all the importers for the given exporter. Since the importers are already created before the exporter
	 * instance is created, this method only does filtering based on the mandatory imports.
	 */
	protected void discoverDependentImporterFor(String exporterBeanName, Object exporter) {

		boolean trace = log.isTraceEnabled();

		// determine exporters
		String[] importerA =
				BeanFactoryUtils.getTransitiveDependenciesForBean(beanFactory, exporterBeanName, true,
						OsgiServiceProxyFactoryBean.class);

		String[] importerB =
				BeanFactoryUtils.getTransitiveDependenciesForBean(beanFactory, exporterBeanName, true,
						OsgiServiceCollectionProxyFactoryBean.class);

		String[] importerNames = StringUtils.concatenateStringArrays(importerA, importerB);

		// create map of associated importers
		Map<Object, String> dependingImporters = new LinkedHashMap<Object, String>(importerNames.length);

		if (trace)
			log.trace("Exporter [" + exporterBeanName + "] depends (transitively) on the following importers:"
					+ ObjectUtils.nullSafeToString(importerNames));

		// first create a listener for the exporter
		ImporterStateListener listener = new ImporterDependencyListener(exporter);
		exporterListener.put(exporter, listener);

		// exclude non-mandatory importers
		// non-singletons get added only once (as one instance is enough)
		for (int i = 0; i < importerNames.length; i++) {
			if (beanFactory.isSingleton(importerNames[i])) {
				Object importer = beanFactory.getBean(importerNames[i]);

				// create an importer -> exporter association
				if (isMandatory(importer)) {
					dependingImporters.put(importer, importerNames[i]);
					importerToName.putIfAbsent(importer, importerNames[i]);
				}

				else if (trace)
					log.trace("Importer [" + importerNames[i] + "] is optional; skipping it");
			} else if (trace)
				log.trace("Importer [" + importerNames[i] + "] is a non-singleton; ignoring it");
		}

		if (trace)
			log.trace("After filtering, exporter [" + exporterBeanName + "] depends on importers:"
					+ dependingImporters.values());

		Collection<Object> filteredImporters = dependingImporters.keySet();

		// add the importers and their status to the collection
		synchronized (exporter) {
			Map<Object, Boolean> importerStatuses = new LinkedHashMap<Object, Boolean>(filteredImporters.size());

			for (Iterator<Object> iter = filteredImporters.iterator(); iter.hasNext();) {
				Object importer = iter.next();
				importerStatuses.put(importer, Boolean.valueOf(isSatisfied(importer)));
				// add the listener after the importer status has been recorded
				addListener(importer, listener);
			}
			exporterToImporterDeps.put(exporter, importerStatuses);
			if (!checkIfExporterShouldStart(exporter, importerStatuses)) {
				callUnregisterOnStartup(exporter);
			}
		}
	}

	private boolean checkIfExporterShouldStart(Object exporter, Map<Object, Boolean> importers) {

		if (!importers.containsValue(Boolean.FALSE)) {
			startExporter(exporter);

			if (log.isDebugEnabled())
				log.trace("Exporter [" + exporterToName.get(exporter) + "] started; "
						+ "all its dependencies are satisfied");
			return true;

		} else {
			List<String> unsatisfiedDependencies = new ArrayList<String>(importers.size());

			for (Iterator<Map.Entry<Object, Boolean>> iterator = importers.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<Object, Boolean> entry = iterator.next();
				if (Boolean.FALSE.equals(entry.getValue()))
					unsatisfiedDependencies.add(importerToName.get(entry.getKey()));
			}

			if (log.isTraceEnabled()) {
				log.trace("Exporter [" + exporterToName.get(exporter)
						+ "] not started; there are still unsatisfied dependencies " + unsatisfiedDependencies);
			}

			return false;
		}
	}

	public void removeServiceExporter(Object bean, String beanName) {
		if (log.isTraceEnabled()) {
			log.trace("Removing exporter [" + beanName + "]");
		}

		// remove the exporter and its listeners from the map
		ImporterStateListener stateListener = (ImporterStateListener) exporterListener.remove(bean);

		Map<Object, Boolean> importers;

		synchronized (bean) {
			importers = exporterToImporterDeps.remove(bean);
		}

		// no need to do synchronization anymore since no other threads will find the collection
		if (importers != null)
			for (Iterator<Object> iterator = importers.keySet().iterator(); iterator.hasNext();) {
				Object importer = iterator.next();
				// get associated controller
				removeListener(importer, stateListener);
			}
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory);
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	public void destroy() {
		exportersSeen.clear();
		exporterListener.clear();
		exporterToImporterDeps.clear();
		exporterToName.clear();
		importerToName.clear();
	}

	private void startExporter(Object exporter) {
		ExporterControllerUtils.getControllerFor(exporter).registerService();
	}

	private void stopExporter(Object exporter) {
		ExporterControllerUtils.getControllerFor(exporter).unregisterService();
	}

	private void callUnregisterOnStartup(Object exporter) {
		ExporterControllerUtils.getControllerFor(exporter).callUnregisterOnStartup();
	}

	private void addListener(Object importer, ImporterStateListener stateListener) {
		ImporterInternalActions controller = ImporterControllerUtils.getControllerFor(importer);
		controller.addStateListener(stateListener);
	}

	private void removeListener(Object importer, ImporterStateListener stateListener) {
		ImporterInternalActions controller = ImporterControllerUtils.getControllerFor(importer);
		controller.removeStateListener(stateListener);
	}

	private boolean isSatisfied(Object importer) {
		return ImporterControllerUtils.getControllerFor(importer).isSatisfied();
	}

	private boolean isMandatory(Object importer) {
		if (importer instanceof AbstractOsgiServiceImportFactoryBean) {
			return Availability.MANDATORY.equals(((AbstractOsgiServiceImportFactoryBean) importer).getAvailability());
		}
		return false;
	}
}