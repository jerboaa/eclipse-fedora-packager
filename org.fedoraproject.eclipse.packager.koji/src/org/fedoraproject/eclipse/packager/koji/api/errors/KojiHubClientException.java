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
package org.fedoraproject.eclipse.packager.koji.api.errors;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Exceptions thrown by koji clients.
 */
public class KojiHubClientException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = -3622365505538797782L;

	/**
	 * Default constructor
	 */
	public KojiHubClientException() {
		// empty
	}
	
	/**
	 * @param msg 
	 * @param cause
	 */
	public KojiHubClientException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * @param cause
	 */
	public KojiHubClientException(Throwable cause) {
		super("Client error", cause); //$NON-NLS-1$
	}
	
	/**
	 * @param msg
	 */
	public KojiHubClientException(String msg) {
		super(msg);
	}
}
