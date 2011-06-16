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
 * Thrown if Eclipse Fedora Packager detected errors while updating the VCS
 * ignore file.
 */
public class VCSIgnoreFileUpdateException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = 960289323631735201L;
	
	/**
	 * @param message
	 * @param cause
	 */
	public VCSIgnoreFileUpdateException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * @param message
	 */
	public VCSIgnoreFileUpdateException(String message) {
		super(message);
	}
}
