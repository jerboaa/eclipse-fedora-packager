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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.xmlrpc.XmlRpcException;
import org.fedoraproject.eclipse.packager.FedoraSSL;
import org.fedoraproject.eclipse.packager.FedoraSSLFactory;

/**
 * Koji hub client which uses certificate based
 * authentication (SSL).
 */
public class KojiSSLHubClient extends AbstractKojiHubBaseClient {
	
	/**
	 * @param kojiHubUrl The koji hub URL to use.
	 * 
	 * @throws MalformedURLException
	 */
	public KojiSSLHubClient(String kojiHubUrl) throws MalformedURLException {
		super(kojiHubUrl);
	}
	
	/**
	 * SSL implementation of XMLRPC based login()
	 * 
	 * Login to remote URL specified by constructor or setter using the SSL
	 * client certificate.
	 * 
	 * @see IKojiHubClient#login()
	 * 
	 * @return session key and session id as a Map.
	 * @throws IllegalStateException
	 *             if hub URL has not been configured.
	 * @throws KojiHubClientLoginException
	 *             if login fails for some other reason.
	 */
	@Override
	public HashMap<?, ?> login() throws KojiHubClientLoginException {
		if (this.kojiHubUrl == null) {
			throw new IllegalStateException(
					"Hub URL must be set before trying to login");
		}
		// Initialize SSL connection
		try {
			initSSLConnection();
		} catch (KojiClientException e) {
			throw new KojiHubClientLoginException(e);
		}
		return doSslLogin();
	}

	/**
	 * Log on to URL using SSL.
	 * 
	 * @throws KojiHubClientLoginException
	 *             if login returns something unexpected.
	 */
	private HashMap<?, ?> doSslLogin() throws KojiHubClientLoginException {
		// prepare XMLRPC
		setupXmlRpcConfig();
		// setup for SSL login
		setupSSLLoginXMLRPCConfig();
		setupXmlRpcClient();
		// do the login
		ArrayList<String> params = new ArrayList<String>();
		Object result = null;
		HashMap<?, ?> hashMap = null;
		try {
			result = xmlRpcClient.execute("sslLogin", params); //$NON-NLS-1$
			hashMap = (HashMap<?, ?>) result;
		} catch (ClassCastException e) {
			// TODO: Externalize
			throw new KojiHubClientLoginException(
					"Login returned unexpected result");
		} catch (XmlRpcException e) {
			throw new KojiHubClientLoginException(e);
		}
		return hashMap;
	}

	/**
	 * Initialize SSL connection
	 */
	private void initSSLConnection() throws KojiClientException {
		// Create empty HostnameVerifier
		HostnameVerifier hv = new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};
		FedoraSSL fedoraSSL = FedoraSSLFactory.getInstance();
		SSLContext ctxt = null;
		try {
			ctxt = fedoraSSL.getInitializedSSLContext();
		} catch (FileNotFoundException e) {
			// certs are missing
			throw new KojiClientException(e);
		} catch (GeneralSecurityException e) {
			throw new KojiClientException(e);
		} catch (IOException e) {
			throw new KojiClientException(e);
		}
		// set up the proper socket
		HttpsURLConnection.setDefaultSSLSocketFactory(ctxt.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}
	
	/**
	 * As of 2011-02-08 we need to use a different URL for SSL login. This
	 * method sets the server URL appropriately.
	 * 
	 * @throws KojiHubClientLoginException
	 */
	private void setupSSLLoginXMLRPCConfig() throws KojiHubClientLoginException {
		if (this.xmlRpcConfig == null) {
			// TODO: externalize!
			throw new KojiHubClientLoginException(new IllegalStateException(
					"xmlRpcConfig needs to be initialized!"));
		}
		URL sslLoginUrl = null;
		try {
			sslLoginUrl = new URL(this.kojiHubUrl.toString() + "/ssllogin"); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			// Ignore. If hub URL was invalid an exception should have been
			// thrown earlier.
		}
		this.xmlRpcConfig.setServerURL(sslLoginUrl);
	}
	
}
