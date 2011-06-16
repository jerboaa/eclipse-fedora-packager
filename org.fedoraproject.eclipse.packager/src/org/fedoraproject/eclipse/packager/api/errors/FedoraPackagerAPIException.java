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
 * Superclass of all exceptions thrown by the API classes in
 * {@code org.fedoraproject.eclipse.packager.api}
 *
 */
public class FedoraPackagerAPIException extends Exception {

	private static final long serialVersionUID = -3988956265787343112L;

	protected FedoraPackagerAPIException(String message, Throwable cause) {
		super(message, cause);
	}

	protected FedoraPackagerAPIException(String message) {
		super(message);
	}

	protected FedoraPackagerAPIException() {
	}
}
