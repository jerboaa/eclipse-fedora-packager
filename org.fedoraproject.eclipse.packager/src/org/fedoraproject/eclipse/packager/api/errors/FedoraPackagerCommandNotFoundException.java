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
package org.fedoraproject.eclipse.packager.api.errors;

import org.fedoraproject.eclipse.packager.api.FedoraPackager;

/**
 * Thrown if an Eclipse Fedora Packager command was not found in the registry.
 * @see FedoraPackager 
 *
 */
public class FedoraPackagerCommandNotFoundException extends
		FedoraPackagerExtensionPointException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -596745668287575312L;

	/**
	 * @param message
	 */
	public FedoraPackagerCommandNotFoundException(String message) {
		super(message);
	}
}
