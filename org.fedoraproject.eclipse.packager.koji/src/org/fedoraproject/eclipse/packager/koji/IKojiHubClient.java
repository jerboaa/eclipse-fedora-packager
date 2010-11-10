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
package org.fedoraproject.eclipse.packager.koji;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;

/**
 * Interface for Koji hub client implementations.
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
	 * @param scratch
	 * @return The remote server's response.
	 * @throws XmlRpcException
	 */
	public String build(String target, String scmURL, boolean scratch)
			throws XmlRpcException;
	
	/**
	 * Set the Web URL for the client.
	 * 
	 * @param url The new URL.
	 * @throws KojiHubClientInitException If the given URL is invalid.
	 */
	public void setWebUrl(String url) throws KojiHubClientInitException;
	
	/**
	 * Set the XMLRPC enabled hub URL for the client.
	 * 
	 * @param url The new URL.
	 * @throws KojiHubClientInitException If the given URL is invalid.
	 */
	public void setHubUrl(String url) throws KojiHubClientInitException;
	
	/**
	 * Get the Web URL for the client.
	 * 
	 * @return The currently set Web URL.
	 */
	public URL getWebUrl();
	
	/**
	 * Get the XMLRPC enabled hub URL for the client.
	 * 
	 * @return The currently set hub URL.
	 */
	public URL getHubUrl();
	
	/**
	 * Write message to the Fedora Packager console.
	 * 
	 * @param message
	 */
	public void writeToConsole(String message);
	
	/**
	 * Set Web URL and hub URL from preferences
	 * 
	 * @throws KojiHubClientInitException If URLs read from preferences
	 *         are invalid. 
	 */
	public void setUrlsFromPreferences() throws KojiHubClientInitException;
	
	/**
	 * Save session data in XMLRPC config URL.
	 *
	 * @param sessionKey
	 * @param sessionID
	 * @throws MalformedURLException
	 */
	public void saveSessionInfo(String sessionKey, String sessionID)
		throws MalformedURLException;
}