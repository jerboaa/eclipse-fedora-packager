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
package org.fedoraproject.eclipse.packager.koji.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.fedoraproject.eclipse.packager.koji.KojiText;
import org.fedoraproject.eclipse.packager.koji.api.errors.BuildAlreadyExistsException;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientException;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientLoginException;
import org.fedoraproject.eclipse.packager.koji.internal.utils.KojiTypeFactory;

/**
 * Koji Base client.
 */
public abstract class AbstractKojiHubBaseClient implements IKojiHubClient {
	
	/**
	 * Default constructor to set up a basic client.
	 * 
	 * @param kojiHubUrl
	 *            The koji hub URL.
	 * @throws MalformedURLException
	 *             If the hub URL was invalid.
	 */
	public AbstractKojiHubBaseClient(String kojiHubUrl)
			throws MalformedURLException {
		this.kojiHubUrl = new URL(kojiHubUrl);
		setupXmlRpcConfig();
		setupXmlRpcClient();
	}
	
	/**
	 * URL of the Koji Hub/XMLRPC interface
	 */
	protected URL kojiHubUrl;
	protected XmlRpcClientConfigImpl xmlRpcConfig;
	protected XmlRpcClient xmlRpcClient;
	
	/**
	 * Store session info in XMLRPC configuration.
	 * 
	 * @param sessionKey
	 * @param sessionID
	 */
	protected void saveSessionInfo(String sessionKey, String sessionID) {
		try {
			xmlRpcConfig.setServerURL(new URL(this.kojiHubUrl.toString()
					+ "?session-key=" + sessionKey //$NON-NLS-1$
					+ "&session-id=" + sessionID)); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			// ignore, URL should be valid
		}
	}

	/**
	 * Discard session info previously stored in server URL via
	 * {@link AbstractKojiHubBaseClient#saveSessionInfo(String, String)}.
	 */
	protected void discardSession() {
		xmlRpcConfig.setServerURL(this.kojiHubUrl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fedoraproject.eclipse.packager.IKojiHubClient#sslLogin()
	 */
	@Override
	public abstract HashMap<?, ?> login() throws KojiHubClientLoginException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fedoraproject.eclipse.packager.IKojiHubClient#logout()
	 */
	@Override
	public void logout() throws KojiHubClientException {
		ArrayList<String> params = new ArrayList<String>();
		try {
			xmlRpcClient.execute("logout", params); //$NON-NLS-1$
		} catch (XmlRpcException e) {
			throw new KojiHubClientException(e);
		}
		discardSession();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient#build(java
	 * .lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public int build(String target, String scmURL, String nvr, boolean scratch)
			throws KojiHubClientException {
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(scmURL);
		params.add(target);
		if (scratch) {
			Map<String, Boolean> scratchParam = new HashMap<String, Boolean>();
			scratchParam.put("scratch", true); //$NON-NLS-1$
			params.add(scratchParam);
		} else if (nvr != null) {
			KojiBuildInfo buildInfo = getBuild(nvr);
			if (buildInfo != null && buildInfo.isComplete()) {
				throw new BuildAlreadyExistsException(buildInfo.getTaskId());
			}
		}
		Object result;
		try {
			result = xmlRpcClient.execute("build", params); //$NON-NLS-1$
		} catch (XmlRpcException e) {
			throw new KojiHubClientException(e);
		}
		int taskId;
		try {
			taskId = Integer.parseInt(result.toString());
		} catch (NumberFormatException e) {
			// no task ID returned, some other error must have happened.
			throw new KojiHubClientException(result.toString());
		}
		return taskId;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient#getBuild(java
	 * .lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public KojiBuildInfo getBuild(String nvr) throws KojiHubClientException {
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(nvr);
		Map<String, Object> rawBuildInfo;
		try {
			rawBuildInfo = (Map<String, Object>) xmlRpcClient.execute(
					"getBuild", params); //$NON-NLS-1$
		} catch (XmlRpcException e) {
			throw new KojiHubClientException(e);
		}
		if (rawBuildInfo != null) {
			return new KojiBuildInfo(rawBuildInfo);
		} else {
			return null;
		}
	}
	
	/**
	 * Configure XMLRPC connection
	 */
	protected void setupXmlRpcConfig() {
		xmlRpcConfig = new XmlRpcClientConfigImpl();
		xmlRpcConfig.setServerURL(this.kojiHubUrl);
		xmlRpcConfig.setEnabledForExtensions(true);
		xmlRpcConfig.setConnectionTimeout(30000);
	}
	
	/**
	 * Set up XMLRPC client.
	 * 
	 * @throws IllegalStateException If XMLRPC configuration hasn't been
	 *         properly set up.
	 */
	protected void setupXmlRpcClient() throws IllegalStateException {
		if (xmlRpcConfig == null) {
			throw new IllegalStateException(KojiText.xmlRPCconfigNotInitialized);
		}
		xmlRpcClient = new XmlRpcClient();
		xmlRpcClient.setTypeFactory(new KojiTypeFactory(this.xmlRpcClient));
		xmlRpcClient.setConfig(this.xmlRpcConfig);
	}
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient#uploadFile(java.lang.String, java.lang.String, int, java.lang.String, int, java.lang.String)
	 */
	@Override
	public boolean uploadFile(String path, String name, int size, String md5sum, int offset, String data)
		throws KojiHubClientException {
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(path);
		params.add(name);
		params.add(size);
		params.add(md5sum);
		params.add(offset);
		params.add(data);
		Object result;
		try {
			result = xmlRpcClient.execute("uploadFile", params); //$NON-NLS-1$
		} catch (XmlRpcException e) {
			throw new KojiHubClientException(e);
		}
		boolean success = Boolean.parseBoolean(result.toString());
		return success;
	}
}
