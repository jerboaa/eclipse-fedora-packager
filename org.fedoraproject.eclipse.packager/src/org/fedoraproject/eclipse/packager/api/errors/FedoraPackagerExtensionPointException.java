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

/**
 * Thrown if the plug-in failed to instantiate an extension point provided class.
 *
 */
public class FedoraPackagerExtensionPointException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = 1317581835065207671L;

	/**
	 * @param message
	 * @param cause
	 */
	public FedoraPackagerExtensionPointException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @param message
	 */
	public FedoraPackagerExtensionPointException(String message) {
		super(message);
	}
}
