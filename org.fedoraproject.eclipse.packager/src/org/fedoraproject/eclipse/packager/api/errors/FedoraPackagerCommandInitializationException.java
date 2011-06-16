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
 * Exception thrown if packager command initialization failed.
 */
public class FedoraPackagerCommandInitializationException extends
		FedoraPackagerExtensionPointException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8053342144830979684L;
	
	/**
	 * @param message
	 * @param cause
	 */
	public FedoraPackagerCommandInitializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
