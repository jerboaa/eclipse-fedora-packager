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
package org.fedoraproject.eclipse.packager.bodhi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.text.ParseException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.HttpSecureProtocol;
import org.apache.commons.ssl.TrustMaterial;
import org.fedoraproject.eclipse.packager.SSLUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class BodhiClient implements IBodhiClient {
	public static String BODHI_URL = "https://admin.fedoraproject.org/updates/"; //$NON-NLS-1$
	protected HttpClient httpclient;

	public BodhiClient() throws GeneralSecurityException, IOException {
		HttpSecureProtocol protocol = new HttpSecureProtocol();
		protocol.setKeyMaterial(SSLUtils.getKeyMaterial());
		protocol.setTrustMaterial(TrustMaterial.TRUST_ALL);
		Protocol.registerProtocol("https", new Protocol("https", //$NON-NLS-1$ //$NON-NLS-2$
				(ProtocolSocketFactory) protocol, 443));

		httpclient = new HttpClient();
		httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IBodhiClient#login(java.lang.String, java.lang.String)
	 */
	public JSONObject login(String username, String password)
	throws IOException, HttpException, ParseException, JSONException {
		PostMethod postMethod = new PostMethod(BODHI_URL
				+ "login?tg_format=json"); //$NON-NLS-1$
		NameValuePair[] data = { new NameValuePair("login", "Login"), //$NON-NLS-1$ //$NON-NLS-2$
				new NameValuePair("user_name", username), //$NON-NLS-1$
				new NameValuePair("password", password) }; //$NON-NLS-1$
		postMethod.setRequestBody(data);
		int code = httpclient.executeMethod(postMethod);
		if (code != HttpURLConnection.HTTP_OK) {
			throw new IOException(Messages.getString("BodhiClient.8") + code + " - " //$NON-NLS-1$ //$NON-NLS-2$
					+ postMethod.getStatusText());
		}

		return new JSONObject(postMethod.getResponseBodyAsString());
	}

	public void logout()
	throws IOException, HttpException, ParseException {
		PostMethod postMethod = new PostMethod(BODHI_URL
				+ "logout?tg_format=json"); //$NON-NLS-1$
		int code = httpclient.executeMethod(postMethod);
		if (code != HttpURLConnection.HTTP_OK) {
			throw new IOException(Messages.getString("BodhiClient.8") + code + " - " //$NON-NLS-1$ //$NON-NLS-2$
					+ postMethod.getStatusText());
		}
	}

	/* (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IBodhiClient#newUpdate(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public JSONObject newUpdate(String buildName, String release, String type,
			String request, String bugs, String notes) throws IOException,
			HttpException, ParseException, JSONException {
		PostMethod postMethod = new PostMethod(BODHI_URL
				+ "save?tg_format=json"); //$NON-NLS-1$
		NameValuePair[] data = { new NameValuePair("builds", buildName), //$NON-NLS-1$
				new NameValuePair("release", release), //$NON-NLS-1$
				new NameValuePair("type", type), //$NON-NLS-1$
				new NameValuePair("request", request), //$NON-NLS-1$
				new NameValuePair("bugs", bugs), //$NON-NLS-1$
				new NameValuePair("notes", notes) }; //$NON-NLS-1$
		postMethod.setRequestBody(data);
		int code = httpclient.executeMethod(postMethod);
		if (code != HttpURLConnection.HTTP_OK) {
			throw new IOException(Messages.getString("BodhiClient.8") + code + " - " //$NON-NLS-1$ //$NON-NLS-2$
					+ postMethod.getStatusText());
		}

		return new JSONObject(postMethod.getResponseBodyAsString());
	}
}
