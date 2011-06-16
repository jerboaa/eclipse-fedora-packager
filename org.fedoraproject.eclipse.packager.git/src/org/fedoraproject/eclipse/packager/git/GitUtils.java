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
package org.fedoraproject.eclipse.packager.git;

import org.fedoraproject.eclipse.packager.FedoraSSL;
import org.fedoraproject.eclipse.packager.FedoraSSLFactory;


/**
 * Utility class for Fedora Git related things.
 */
public class GitUtils {

	/**
	 * @param gitBaseUrl
	 * @param packageName
	 * @return The full clone URL for the given package.
	 */
	public static String getFullGitURL(String gitBaseUrl, String packageName) {
		return gitBaseUrl + packageName + GitConstants.GIT_REPO_SUFFIX;
	}

	/**
	 * @return The anonymous base URL to clone from.
	 */
	public static String getAnonymousGitBaseUrl() {
		return GitConstants.ANONYMOUS_PROTOCOL
				+ GitPreferencesConstants.DEFAULT_CLONE_BASE_URL;
	}

	/**
	 * @param username
	 * @return The SSH base URL to clone from.
	 */
	public static String getAuthenticatedGitBaseUrl(String username) {
		return GitConstants.AUTHENTICATED_PROTOCOL + username
				+ GitConstants.USERNAME_SEPARATOR
				+ GitPreferencesConstants.DEFAULT_CLONE_BASE_URL;
	}
	
	/**
	 * Determine the default Git base URL for cloning. Based on ~/.fedora.cert
	 * 
	 * @return The default Git base URL for cloning.
	 */
	public static String getDefaultGitBaseUrl() {
		// Figure out if we have an anonymous or a FAS user
		String user = FedoraSSLFactory.getInstance().getUsernameFromCert();
		String gitURL;
		if (!user.equals(FedoraSSL.UNKNOWN_USER)) {
			gitURL = GitUtils.getAuthenticatedGitBaseUrl(user);
		} else {
			gitURL = GitUtils.getAnonymousGitBaseUrl();
		}
		return gitURL;
	}
}
