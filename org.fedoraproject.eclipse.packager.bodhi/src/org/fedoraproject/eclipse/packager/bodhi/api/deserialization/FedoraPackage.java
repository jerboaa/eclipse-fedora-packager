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

/**
 * Class which represents a Fedora package. Deserialized from
 * a JSON String by google.gson.
 */
public class FedoraPackage {
	
	private boolean suggest_reboot;
	private String[] committers;
	private String name;
	
	/**
	 * Google GSON requires this
	 */
	public FedoraPackage() {
		// nothing
	}
	
	/**
	 * @return {@code true} if a reboot is suggested for this package,
	 *         {@code false} otherwise.
	 */
	public boolean isSuggest_reboot() {
		return suggest_reboot;
	}
	
	/**
	 * @return the committers on this package
	 */
	public String[] getCommitters() {
		return committers;
	}
	
	/**
	 * @return the package name.
	 */
	public String getName() {
		return name;
	}
}
