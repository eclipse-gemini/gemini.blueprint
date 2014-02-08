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

package org.eclipse.gemini.blueprint.test.internal.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Loads a property file performing key expansion.
 * 
 * Provides simple property substitution, without support for inner or nested placeholders. Also, the algorithm does
 * only one parsing so derivative placeholders are not supported.
 * 
 * 
 * @author Costin Leau
 * 
 */
public abstract class PropertiesUtil {

	private static final String DELIM_START = "${";

	private static final String DELIM_STOP = "}";

	private static final Properties EMPTY_PROPERTIES = new Properties();

	private static final class OrderedProperties extends Properties implements Cloneable {

		private final Map<Object, Object> map = new LinkedHashMap<Object, Object>();

		/**
		 * 
		 * @see java.util.Map#clear()
		 */
		public void clear() {
			map.clear();
		}

		/**
		 * @param key
		 * @return
		 * @see java.util.Map#containsKey(java.lang.Object)
		 */
		public boolean containsKey(Object key) {
			return map.containsKey(key);
		}

		/**
		 * @param value
		 * @return
		 * @see java.util.Map#containsValue(java.lang.Object)
		 */
		public boolean containsValue(Object value) {
			return map.containsValue(value);
		}

		/**
		 * @return
		 * @see java.util.Map#entrySet()
		 */
		public Set<Entry<Object, Object>> entrySet() {
			return map.entrySet();
		}

		/**
		 * @param o
		 * @return
		 * @see java.util.Map#equals(java.lang.Object)
		 */
		public boolean equals(Object o) {
			return map.equals(o);
		}

		/**
		 * @param key
		 * @return
		 * @see java.util.Map#get(java.lang.Object)
		 */
		public Object get(Object key) {
			return map.get(key);
		}

		@Override
		public String getProperty(String key) {
			Object oval = map.get(key);
			String sval = (oval instanceof String) ? (String) oval : null;
			return ((sval == null) && (defaults != null)) ? defaults.getProperty(key) : sval;
		}

		/**
		 * @return
		 * @see java.util.Map#hashCode()
		 */
		public int hashCode() {
			return map.hashCode();
		}

		/**
		 * @return
		 * @see java.util.Map#isEmpty()
		 */
		public boolean isEmpty() {
			return map.isEmpty();
		}

		/**
		 * @return
		 * @see java.util.Map#keySet()
		 */
		public Set<Object> keySet() {
			return map.keySet();
		}

		/**
		 * @param key
		 * @param value
		 * @return
		 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
		 */
		public Object put(Object key, Object value) {
			return map.put(key, value);
		}

		/**
		 * @param m
		 * @see java.util.Map#putAll(java.util.Map)
		 */
		public void putAll(Map<? extends Object, ? extends Object> m) {
			map.putAll(m);
		}

		/**
		 * @param key
		 * @return
		 * @see java.util.Map#remove(java.lang.Object)
		 */
		public Object remove(Object key) {
			return map.remove(key);
		}

		/**
		 * @return
		 * @see java.util.Map#size()
		 */
		public int size() {
			return map.size();
		}

		/**
		 * @return
		 * @see java.util.Map#values()
		 */
		public Collection<Object> values() {
			return map.values();
		}

		@Override
		public synchronized boolean contains(Object value) {
			return map.containsValue(value);
		}

		@Override
		public synchronized Enumeration<Object> elements() {
			return new IteratorBackedEnumeration(map.values().iterator());
		}

		@Override
		public synchronized Enumeration<Object> keys() {
			return new IteratorBackedEnumeration(map.keySet().iterator());
		}

		@Override
		public synchronized String toString() {
			return map.toString();
		}

		private final class IteratorBackedEnumeration<K> implements Enumeration<K> {

			private final Iterator<? extends K> iterator;

			public IteratorBackedEnumeration(Iterator<? extends K> iterator) {
				this.iterator = iterator;
			}

			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			public K nextElement() {
				return iterator.next();
			}
		}
	}

	/**
	 * Shortcut method - loads a property object from the given input stream and applies property expansion. The
	 * returned properties object preserves order at the expense of speed.
	 * 
	 * @param resource
	 * @return
	 */
	public static Properties loadAndExpand(Resource resource) {
		Properties props = new OrderedProperties();

		if (resource == null) {
			return props;
        }

		try {
			props.load(resource.getInputStream());
		} catch (IOException ex) {
			return null;
		}
		return expandProperties(props);
	}

	/**
	 * Filter/Eliminate keys that start with the given prefix.
	 * 
	 * @param properties
	 * @param prefix
	 * @return
	 */
	public static Properties filterKeysStartingWith(Properties properties, String prefix) {
		if (!StringUtils.hasText(prefix)) {
			return EMPTY_PROPERTIES;
        }

		Assert.notNull(properties);

		Properties excluded = (properties instanceof OrderedProperties ? new OrderedProperties() : new Properties());

		// filter ignore keys out
		for (Enumeration enm = properties.keys(); enm.hasMoreElements();) {
			String key = (String) enm.nextElement();
			if (key.startsWith(prefix)) {
				excluded.put(key, properties.get(key));
			}
		}
		
		for (Enumeration enm = excluded.keys(); enm.hasMoreElements();) {
			properties.remove(enm.nextElement());
		}
		return excluded;
	}

	/**
	 * Filter/Eliminate keys that have a value that starts with the given prefix.
	 * 
	 * @param properties
	 * @param prefix
	 * @return
	 */
	public static Properties filterValuesStartingWith(Properties properties, String prefix) {
		if (!StringUtils.hasText(prefix)) {
			return EMPTY_PROPERTIES;
        }

		Assert.notNull(properties);
		Properties excluded = (properties instanceof OrderedProperties ? new OrderedProperties() : new Properties());

		for (Enumeration enm = properties.keys(); enm.hasMoreElements();) {
			String key = (String) enm.nextElement();
			String value = properties.getProperty(key);
			if (value.startsWith(prefix)) {
				excluded.put(key, value);
			}
		}
		
		for (Enumeration enm = excluded.keys(); enm.hasMoreElements();) {
			properties.remove(enm.nextElement());
		}
		return excluded;
	}

	/**
	 * Apply placeholder expansion to the given properties object.
	 * 
	 * Will return a new properties object, containing the expanded entries. Note that both keys and values will be
	 * expanded.
	 * 
	 * @param props
	 * @return
	 */
	public static Properties expandProperties(Properties props) {
		Assert.notNull(props);

		Set entrySet = props.entrySet();

		Properties newProps = (props instanceof OrderedProperties ? new OrderedProperties() : new Properties());

		for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
			// first expand the keys
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();

			String resultKey = expandProperty(key, props);
			String resultValue = expandProperty(value, props);

			// replace old entry

			newProps.put(resultKey, resultValue);
		}

		return newProps;
	}

	private static String expandProperty(String prop, Properties properties) throws IllegalArgumentException {

		boolean hasPlaceholder = false;
		String copy = prop;

		StringBuilder result = new StringBuilder();

		int index = 0;
		// dig out the placeholders
		do {
			index = copy.indexOf(DELIM_START);
			if (index >= 0) {
				hasPlaceholder = true;

				// add stuff before the delimiter
				result.append(copy.substring(0, index));
				// remove the delimiter
				copy = copy.substring(index + DELIM_START.length());
				// find ending delim
				int stopIndex = copy.indexOf(DELIM_STOP);
				String token = null;

				if (stopIndex >= 0) {
					// discover token
					token = copy.substring(0, stopIndex);
					// remove ending delimiter
					copy = copy.substring(stopIndex + 1);
					// append the replacement for the token
					result.append(properties.getProperty(token));
				} else {
					throw new IllegalArgumentException("cannot interpret property " + prop + " due of token [" + copy
							+ "]");
                }

			} else {
				hasPlaceholder = false;
				// make sure to append the remaining string
				result.append(copy);
			}

		} while (hasPlaceholder);

		return result.toString();
	}
}
