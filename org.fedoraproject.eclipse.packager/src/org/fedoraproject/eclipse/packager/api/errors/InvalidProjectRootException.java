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
 * Thrown if Eclipse Fedora Packager detected a problem with the current
 * Fedora project root (i.e. either .spec file or sources file is missing).
 */
public class InvalidProjectRootException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = -6133727213256708483L;
	
	/**
	 * @param message
	 */
	public InvalidProjectRootException(String message) {
		super(message);
	}

}
