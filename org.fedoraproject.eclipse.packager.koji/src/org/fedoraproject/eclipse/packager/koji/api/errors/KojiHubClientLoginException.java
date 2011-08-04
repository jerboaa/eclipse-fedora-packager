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

import org.fedoraproject.eclipse.packager.FedoraSSLFactory;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;
import org.fedoraproject.eclipse.packager.koji.KojiText;

/**
 * Thrown on Koji login failure.
 */
public class KojiHubClientLoginException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = 1540744448331042317L;
	
	private boolean certificatesMissing = false;

	/**
	 * @param cause The root cause of the login problem.
	 * @param certsMissing Set to {@code true} if certificates are missing.
	 */
	public KojiHubClientLoginException(Throwable cause, boolean certsMissing) {
		this(cause);
		this.certificatesMissing = certsMissing;
	}
	
	
	/**
	 * @param cause The root cause of the login problem.
	 */
	public KojiHubClientLoginException(Throwable cause) {
		super(KojiText.KojiHubClientLoginException_loginFailedMsg, cause);
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
	 * Checks if ~/.fedora.cert exists
	 * 
	 * @return {@code true} If and only if the certificate was missing.
	 */
	public boolean isCertificateMissing() {
		return certificatesMissing;
	}

}
