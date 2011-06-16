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
