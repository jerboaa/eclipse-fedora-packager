/*******************************************************************************
 * Copyright (c) 2010-2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.bodhi.api.deserialization;

import com.google.gson.annotations.SerializedName;

/**
 * Class which represents a Koji build. Deserialized from
 * a JSON String by google.gson.
 */
public class Build {
	
	private String nvr;
	// serialized name in the JSON string is "package", which
	// is not a valid Java identifier.
	@SerializedName("package") private FedoraPackage pkg;
	
	/**
	 * Google GSON requires this
	 */
	public Build() {
		// nothing
	}
	
	/**
	 * @return the nvr
	 */
	public String getNvr() {
		return nvr;
	}
	/**
	 * @return the pkg
	 */
	public FedoraPackage getPkg() {
		return pkg;
	}
	
}
