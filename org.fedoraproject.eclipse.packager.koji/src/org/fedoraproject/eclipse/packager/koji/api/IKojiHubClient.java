/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.koji.api;

import java.util.HashMap;

import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientException;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientLoginException;

/**
 * Interface for Koji hub client implementations. At the moment there is only
 * a SSL login based implementation.
 *
 */
public interface IKojiHubClient {

	/**
	 * Log in on the remote host as specified by the hub client url and stores
	 * session information into XMLRPC config URL.
	 * 
	 * @return Login session information, which was stored.
	 * @throws KojiHubClientLoginException
	 *             If an error occurred.
	 */
	public HashMap<?, ?> login() throws KojiHubClientLoginException;

	/**
	 * Logout from hub url and discard session.
	 * 
	 * @throws KojiHubClientException
	 */
	public void logout() throws KojiHubClientException;

	/**
	 * Initiate a build on the remote hub host. Checks, if a build with the
	 * given "Name-Release-Version" (NVR) has been built already.
	 * 
	 * @param target
	 *            The dist-tag (see: $ koji list-targets).
	 * @param scmURL
	 * @param nvr
	 *            Name-Version-Release (see: RPM package naming).
	 * @param scratch
	 *            Set to {@code true} for a scratch build.
	 * @return The task ID.
	 * @throws KojiHubClientException
	 *             If some error occurred.
	 */
	public int build(String target, String scmURL, String nvr, boolean scratch)
			throws KojiHubClientException;
	
	/**
	 * Fetches information related to a name-version-release token.
	 * 
	 * @param nvr
	 *            The name-version-release of the build for which to fetch
	 *            information.
	 * 
	 * @throws KojiHubClientException
	 *             If some error occurred.
	 * 
	 * @return The build information for the given nvr or {@code null} if build
	 *         does not exist
	 */
	public KojiBuildInfo getBuild(String nvr) throws KojiHubClientException;
	
	/**
	 * @param path 
	 * 				Path to upload to on the koji server.
	 * @param name
	 * 				The name of the file being uploaded. 
	 * @param size
	 * 				The size of the file being uploaded in bytes.
	 * @param md5sum
	 * 				The MD5 sum of the file being uploaded.
	 * @param offset
	 * 				The number of bytes to skip when building the file 
	 * 				from multiple uploads. 
	 * @param data
	 * 				The Base64 representation of the file.
	 * @return True if successful, false if not.
	 * @throws KojiHubClientException
	 */
	public boolean uploadFile(String path, String name, int size, String md5sum, int offset, String data)
		throws KojiHubClientException;
}