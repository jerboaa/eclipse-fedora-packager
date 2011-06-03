package org.fedoraproject.eclipse.packager.cvs;

import org.fedoraproject.eclipse.packager.FedoraSSL;
import org.fedoraproject.eclipse.packager.FedoraSSLFactory;


/**
 * Utility class for Fedora Git related things.
 */
public class CVSUtils {

	/**
	 * @return The anonymous base URL.
	 */
	public static String getAnonymousCVSBaseUrl() {
		return ":pserver;username=anonymous;hostname=cvs.fedoraproject.org:/cvs/pkgs"; //$NON-NLS-1$
	}

	/**
	 * @param username
	 * @return The SSH base URL to clone from.
	 */
	public static String getAuthenticatedCVSBaseUrl(String username) {
		return ":ext;username=" + username //$NON-NLS-1$
				+ ";hostname=cvs.fedoraproject.org:/cvs/pkgs"; //$NON-NLS-1$
	}
	
	/**
	 * Determine the default CVS base URL. Based on ~/.fedora.cert
	 * 
	 * @return The default CVS base URL.
	 */
	public static String getDefaultCVSBaseUrl() {
		// Figure out if we have an anonymous or a FAS user
		String user = FedoraSSLFactory.getInstance().getUsernameFromCert();
		String cvsURL;
		if (!user.equals(FedoraSSL.UNKNOWN_USER)) {
			cvsURL = CVSUtils.getAuthenticatedCVSBaseUrl(user);
		} else {
			cvsURL = CVSUtils.getAnonymousCVSBaseUrl();
		}
		return cvsURL;
	}
}
