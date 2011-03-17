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
import org.eclipse.jface.preference.IPreferenceStore;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraSSL;
import org.fedoraproject.eclipse.packager.FedoraSSLFactory;
import org.fedoraproject.eclipse.packager.PackagerPlugin;

/**
 * Koji hub client which uses certificate based
 * authentication (SSL).
 */
public class KojiHubClient extends AbstractKojiHubClient {
	
	
	/**
	 * Empty constructor
	 */
	public KojiHubClient() {
	}
	
	/**
	 * Constructor taking a Koji hub and Web URL.
	 * 
	 * @param hubUrl 
	 * @param webUrl 
	 * 
	 * @throws KojiHubClientInitException if either one of the 
	 *         provided URLs is invalid.
	 */
	public KojiHubClient(String hubUrl, String webUrl) throws KojiHubClientInitException {
		setHubUrl(hubUrl);
		setWebUrl(webUrl);
	}
	
	/**
	 * SSL implementation of XMLRPC based login()
	 * 
	 * Login to remote URL specified by constructor or setter
	 * using the SSL client certificate. It is the user's
	 * responsibility to initialize the SSL connection as
	 * appropriate.
	 * 
	 * @see IKojiHubClient#login()
	 * 
	 * @return session key and session id as a Map.
	 * @throws IllegalStateException if hub URL has not been
	 *         configured.
	 * @throws KojiHubClientLoginException if login fails for some
	 *         other reason.
	 */
	@Override
	public HashMap<?, ?> login() throws KojiHubClientLoginException {
		if (getHubUrl() == null) {
			throw new IllegalStateException("Hub URL must be set before trying to login");
		}
		// Initialize SSL connection
		try {
			initSSLConnection();
		} catch (KojiHubClientInitException e) {
			throw new KojiHubClientLoginException(e);
		}
		return doSslLogin();
	}
	
	/**
	 * Set Koji Web- and hub URL according to preferences 
	 */
	@Override
	public synchronized void setUrlsFromPreferences() throws KojiHubClientInitException {
		// Sets Koji host according to preferences and statically sets kojiHubUrl and kojiWebUrl
		IPreferenceStore kojiPrefStore = PackagerPlugin.getDefault().getPreferenceStore();
		String preference = kojiPrefStore.getString(FedoraPackagerPreferencesConstants.PREF_KOJI_HUB_URL);
		// Eclipse does not seem to store default preference values in metadata.
		if (preference.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			setHubUrl(FedoraPackagerPreferencesConstants.DEFAULT_KOJI_HUB_URL);
		} else {
			setHubUrl(preference);
		}
		preference = kojiPrefStore.getString(FedoraPackagerPreferencesConstants.PREF_KOJI_WEB_URL);
		// Eclipse does not seem to store default preference values in metadata.
		if (preference.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			setWebUrl(FedoraPackagerPreferencesConstants.DEFAULT_KOJI_WEB_URL);
		} else {
			setWebUrl(preference);
		}	
	}

	/**
	 * Log on to URL using SSL.
	 * @throws KojiHubClientLoginException if login returns something unexpected.
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
			hashMap = (HashMap<?, ?>)result;
		} catch (ClassCastException e) {
			// TODO: Externalize
			throw new KojiHubClientLoginException("Login returned unexpected result");
		} catch (XmlRpcException e) {
			throw new KojiHubClientLoginException(e);
		}
		return hashMap;
	}

	/**
	 * Initialize SSL connection
	 */
	private void initSSLConnection() throws KojiHubClientInitException {
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
			throw new KojiHubClientInitException(e);
		} catch (GeneralSecurityException e) {
			throw new KojiHubClientInitException(e);
		} catch (IOException e) {
			throw new KojiHubClientInitException(e);
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
		if (this.xmlRpcConfig == null ) {
			throw new KojiHubClientLoginException(
					new IllegalStateException("xmlRpcConfig needs to be initialized!"));
			// TODO: externalize!
		}
		URL sslLoginUrl = null;
		try {
			sslLoginUrl = new URL(getHubUrl().toString() + "/ssllogin"); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			// Ignore. If hub URL was invalid an exception should have been
			// thrown earlier.
		}
		this.xmlRpcConfig.setServerURL(sslLoginUrl);
	}
	
}
