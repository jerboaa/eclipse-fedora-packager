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
package org.fedoraproject.eclipse.packager.bodhi.api.errors;

import org.apache.http.HttpResponse;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown if an error occurred during bodhi interaction.
 *
 */
public class BodhiClientException extends FedoraPackagerAPIException {

	private HttpResponse response;
	
	private static final long serialVersionUID = -2076679215232309371L;
	
	/**
	 * @param msg
	 * @param cause
	 */
	public BodhiClientException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * @param message
	 * @param response
	 *            The HTTP response. Pass if some HTTP error occured. I.e.
	 *            status code != 200.
	 */
	public BodhiClientException(String message, HttpResponse response) {
		super(message);
		this.response = response;
	}

	/**
	 * @return The HTTP response if available, {@code null} otherwise.
	 */
	public HttpResponse getHttpResponse() {
		return this.response;
	}

}
