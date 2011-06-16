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

import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;

/**
 * Thrown if an error related to bodhi client logins occurred.
 *
 */
public class BodhiClientLoginException extends FedoraPackagerAPIException {

	private static final long serialVersionUID = 8396679855266934702L;
	private HttpResponse response;
	
	/**
	 * @param msg
	 * @param cause
	 */
	public BodhiClientLoginException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * @param message
	 * @param response
	 *            The HTTP response.
	 */
	public BodhiClientLoginException(String message, HttpResponse response) {
		super(message);
		this.response = response;
	}
	
	/**
	 * @return The HTTP response if available, {@code null} otherwise.
	 */
	public HttpResponse getHttpResponse() {
		return this.response;
	}
	
	/**
	 * 
	 * @return {@code true} if the user was not allowed access (i.e. 403,
	 *         Forbidden was returned).
	 */
	public boolean isInvalidCredentials() {
		// Comment from: [...]fedora/client/proxyclient.py
		// Check for auth failures
		// Note: old TG apps returned 403 Forbidden on authentication failures.
		// Updated apps return 401 Unauthorized
		// We need to accept both until all apps are updated to return 401.
		int responseCode = response.getStatusLine().getStatusCode();
		if (responseCode == HttpURLConnection.HTTP_FORBIDDEN ||
				responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
			// wrong username or password
			return true;
		}
		// some other, perhaps inconclusive? error
		return false;
	}
	
}
