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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;
import org.fedoraproject.eclipse.packager.koji.internal.core.KojiHubClientLoginException;

/**
 * Interface for Koji hub client implementations. At the moment there is only
 * a SSL login based implementation.
 *
 */
public interface IKojiHubClient {

	/**
	 * Log in on the remote host as specified by the
	 * hub client url and stores session information
	 * into XMLRPC config URL.
	 * 
	 * @return Login session information.
	 * @throws KojiHubClientLoginException
	 */
	public HashMap<?, ?> login() throws KojiHubClientLoginException;

	/**
	 * Logout from hub url and discard session.
	 * @throws XmlRpcException 
	 */
	public void logout() throws XmlRpcException;

	/**
	 * Initiate a build on the remote hub host.
	 * 
	 * @param target
	 * @param scmURL
	 * @param nvr 
	 * @param scratch
	 * @return The remote server's response.
	 * @throws KojiClientException
	 */
	public String build(String target, String scmURL, String nvr, boolean scratch)
			throws KojiClientException;
	
	/**
	 * Set the XMLRPC enabled hub URL for the client.
	 * 
	 * @param url The new URL.
	 * @throws MalformedURLException If the given URL is invalid.
	 */
	public void setHubUrl(String url) throws MalformedURLException;
	
	/**
	 * Get the XMLRPC enabled hub URL for the client.
	 * 
	 * @return The currently set hub URL.
	 */
	public URL getHubUrl();
}