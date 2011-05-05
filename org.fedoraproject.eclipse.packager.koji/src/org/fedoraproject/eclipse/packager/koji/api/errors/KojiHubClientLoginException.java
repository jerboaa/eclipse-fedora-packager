package org.fedoraproject.eclipse.packager.koji.api.errors;

import java.security.GeneralSecurityException;

import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerAPIException;
import org.fedoraproject.eclipse.packager.koji.KojiText;

/**
 * Thrown on Koji login failure.
 */
public class KojiHubClientLoginException extends FedoraPackagerAPIException {
	
	private static final long serialVersionUID = 1540744448331042317L;

	private Throwable cause;
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
		this.cause = cause;
	}

	/**
	 * Do some analysis and determine if certificate (~/.fedora.cert) expired.
	 * 
	 * @return {@code true} If and only if we can say for sure that the
	 *         certificate expired.
	 */
	public boolean isCertificateExpired() {
		// Can only possibly be when a GeneralSecurityException was thrown
		if (cause instanceof GeneralSecurityException) {
			// TODO: implement
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
