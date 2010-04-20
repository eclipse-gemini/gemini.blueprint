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

package org.eclipse.gemini.blueprint.compendium.internal.cm.util;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.springframework.util.CollectionUtils;

/**
 * @author Costin Leau
 */
public class PropertiesUtil {

	/**
	 * Merges the given map into the target properties object. Additionally it checks if there are any given local
	 * properties and whether these can override the source.
	 * 
	 * @return a new (Properties) object mergeing the local properties and the source
	 */
	public static Properties initProperties(Properties localMap, boolean localOverride, Map<?, ?> source,
			Properties target) {

		synchronized (target) {
			target.clear();

			// merge the local properties (upfront)
			if (localMap != null && !localOverride) {
				CollectionUtils.mergePropertiesIntoMap(localMap, target);
			}

			if (source != null) {
				target.putAll(source);
			}

			// merge local properties (if needed)
			if (localMap != null && localOverride) {
				CollectionUtils.mergePropertiesIntoMap(localMap, target);
			}

			return target;
		}
	}

	/**
	 * Merges the given dictionary into the target properties object. Additionally it checks if there are any given
	 * local properties and whether these can override the source. Identical to
	 * {@link #initProperties(Properties, boolean, Map, Properties)} excepts it reads the dictionary directly to avoid
	 * any mapping overhead.
	 * 
	 * @param localMap
	 * @param localOverride
	 * @param source
	 * @param target
	 * @return
	 */
	public static Properties initProperties(Properties localMap, boolean localOverride, Dictionary source,
			Properties target) {

		synchronized (target) {
			target.clear();

			// merge the local properties (upfront)
			if (localMap != null && !localOverride) {
				CollectionUtils.mergePropertiesIntoMap(localMap, target);
			}

			if (source != null) {
				Enumeration<?> keys = source.keys();
				for (; keys.hasMoreElements();) {
					Object key = keys.nextElement();
					Object value = source.get(key);
					if (key != null && value != null) {
						target.put(key, value);
					}
				}
			}

			// merge local properties (if needed)
			if (localMap != null && localOverride) {
				CollectionUtils.mergePropertiesIntoMap(localMap, target);
			}

			return target;
		}
	}
}