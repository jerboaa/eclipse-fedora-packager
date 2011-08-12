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
import org.fedoraproject.eclipse.packager.FedoraSSLFactory;


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
	 * Do some analysis and determine if certificate (~/.fedora.cert) expired.
	 * 
	 * @return {@code true} If and only if we can say for sure that the
	 *         certificate expired.
	 */
	public boolean isCertificateExpired() {
		if (!FedoraSSLFactory.getInstance().isFedoraCertValid()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Do some analysis and determine if certificate (~/.fedora.cert) has been
	 * revoked.
	 * 
	 * @return {@code true} If it's very likely that the certificate has been
	 *         revoked.
	 */
	public boolean isCertificateRevoked() {
		// If cert is not expired, but we still get a SSLPeerUnverifiedException,
		// this likely means  ~/.fedora.cert was revoked.
		if (!isCertificateExpired()
				&& this.getCause() instanceof javax.net.ssl.SSLPeerUnverifiedException
				&& this.getCause().getMessage()
						.contains("peer not authenticated")) { //$NON-NLS-1$
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @return The HTTP response if available, {@code null} otherwise.
	 */
	public HttpResponse getHttpResponse() {
		return this.response;
	}
}
