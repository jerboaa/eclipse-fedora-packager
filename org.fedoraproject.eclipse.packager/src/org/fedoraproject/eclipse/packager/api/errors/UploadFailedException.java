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

import org.apache.http.HttpResponse;


/**
 * Thrown if an error occurred while uploading a source file into the lookaside
 * cache.
 */
public class UploadFailedException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = -8250214677451435086L;
	
	// Store response in order to be able to determine the real cause of
	// the exception if something went wrong on HTTP level. I.e. URL unavailable
	// or similar
	private HttpResponse response;
	
	/**
	 * @param message
	 * @param response
	 *            The HTTP response. Pass if some HTTP error occured. I.e.
	 *            status code != 200.
	 */
	public UploadFailedException(String message, HttpResponse response) {
		super(message);
		this.response = response;
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public UploadFailedException(String message, Throwable cause) {
		super(message, cause);
		this.response = null;
	}
	/**
	 * @param message
	 */
	public UploadFailedException(String message) {
		super(message);
		this.response = null;
	}
	
	/**
	 * @return The HTTP response if available, {@code null} otherwise.
	 */
	public HttpResponse getHttpResponse() {
		return this.response;
	}
}
