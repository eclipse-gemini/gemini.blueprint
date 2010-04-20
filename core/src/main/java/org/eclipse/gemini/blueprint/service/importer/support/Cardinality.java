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

package org.eclipse.gemini.blueprint.service.importer.support;

import org.springframework.core.enums.StaticLabeledEnum;

/**
 * Enum-like class containing the OSGi importer services cardinality. Indicates the number of expected matching services
 * and whether the presence is mandatory or not.
 * 
 * @author Costin Leau
 * @deprecated as of Spring DM 2.0, replaced by {@link Availability}
 */
public class Cardinality extends StaticLabeledEnum {

	private static final long serialVersionUID = 6377096464873348405L;

	/**
	 * Optional, single cardinality. At most one OSGi service is expected. This cardinality indicates an OSGi service
	 * reference proxy.
	 */
	public static final Cardinality C_0__1 = new Cardinality(0, "0..1");

	/**
	 * Optional, multiple cardinality. Zero, one or multiple OSGi services are expected. This cardinality indicates an
	 * OSGi service managed collection.
	 */
	public static final Cardinality C_0__N = new Cardinality(1, "0..N");

	/**
	 * Mandatory, single cardinality. Exactly one OSGi service is expected. This cardinality indicates an OSGi service
	 * reference proxy.
	 */
	public static final Cardinality C_1__1 = new Cardinality(2, "1..1");

	/**
	 * Mandatory, multiple cardinality. At least one OSGi service is expected. This cardinality indicates an OSGi
	 * service managed collection.
	 */
	public static final Cardinality C_1__N = new Cardinality(3, "1..N");

	/**
	 * Indicates if this cardinality implies that at most one service is expected.
	 * 
	 * @return true if the given cardinality is single, false otherwise
	 */
	public boolean isSingle() {
		return Cardinality.C_0__1.equals(this) || Cardinality.C_1__1.equals(this);
	}

	/**
	 * Indicates if this cardinality implies that multiple services are expected.
	 * 
	 * @return true if this cardinality is multiple, false otherwise
	 */
	public boolean isMultiple() {
		return Cardinality.C_0__N.equals(this) || Cardinality.C_1__N.equals(this);
	}

	/**
	 * Indicates if this cardinality implies that at least one service is expected (mandatory cardinality).
	 * 
	 * @return true if this cardinality is mandatory, false otherwise
	 */
	public boolean isMandatory() {
		return Cardinality.C_1__1.equals(this) || Cardinality.C_1__N.equals(this);
	}

	/**
	 * Indicates if this cardinality implies that is acceptable for no matching services to be found.
	 * 
	 * @return true if this cardinality is optional, false otherwise
	 */
	public boolean isOptional() {
		return !isMandatory();
	}

	/**
	 * Constructs a new <code>Cardinality</code> instance.
	 * 
	 * @param code
	 * @param label
	 */
	private Cardinality(int code, String label) {
		super(code, label);
	}

	Availability getAvailability() {
		return (isMandatory() ? Availability.MANDATORY : Availability.OPTIONAL);
	}
}