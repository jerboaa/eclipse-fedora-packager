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
package org.fedoraproject.eclipse.packager.bodhi.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.bodhi.BodhiText;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientException;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientLoginException;
import org.fedoraproject.eclipse.packager.bodhi.deserializers.DateTimeDeserializer;
import org.fedoraproject.eclipse.packager.bodhi.fas.DateTime;

/**
 * Bodhi JSON over HTTP client.
 */
public class BodhiClient implements IBodhiClient {
	
	// Use 30 sec connection timeout
	private static final int CONNECTION_TIMEOUT = 30000;
	// Delimiter for pushing several builds as one update
	private static final String BUILDS_DELIMITER = ",";
	
	// Parameter name constants for login
	private static final String LOGIN_PARAM_NAME = "login"; //$NON-NLS-1$
	private static final String LOGIN_PARAM_VALUE = "Login"; //$NON-NLS-1$
	private static final String USERNAME_PARAM_NAME = "user_name"; //$NON-NLS-1$
	private static final String PASSWORD_PARAM_NAME = "password"; //$NON-NLS-1$
	// Parameter name constants for login
	private static final String BUILDS_PARAM_NAME = "builds"; //$NON-NLS-1$
	private static final String TYPE_PARAM_NAME = "type_"; //$NON-NLS-1$
	private static final String REQUEST_PARAM_NAME = "request"; //$NON-NLS-1$
	private static final String BUGS_PARAM_NAME = "bugs"; //$NON-NLS-1$
	private static final String CSRF_PARAM_NAME = "_csrf_token"; //$NON-NLS-1$
	private static final String AUTOKARMA_PARAM_NAME = "autokarma"; //$NON-NLS-1$
	private static final String NOTES_PARAM_NAME = "notes"; //$NON-NLS-1$
	private static final String SUGGEST_REBOOT = "suggest_reboot"; //$NON-NLS-1$
	private static final String STABLE_KARMA = "stable_karma"; //$NON-NLS-1$
	private static final String UNSTABLE_KARMA = "unstable_karma"; //$NON-NLS-1$
	private static final String CLOSE_BUGS_WHEN_STABLE = "close_bugs"; //$NON-NLS-1$
	
	/**
	 *  URL of the Bodhi server to which to connect to.
	 */
	public static final String BODHI_URL = "https://admin.fedoraproject.org/updates/"; //$NON-NLS-1$
	
	// We want JSON responses from the server. Use these constants in order
	// to set the "Accept: application/json" HTTP header accordingly.
	private static final String ACCEPT_HTTP_HEADER_NAME = "Accept"; //$NON-NLS-1$
	private static final String MIME_JSON = "application/json"; //$NON-NLS-1$
	
	// The http client to use for transport
	protected HttpClient httpclient;
	
	// The base URL to use for connections
	protected URL bodhiServerUrl;

	/**
	 * Create a Bodhi client instance. Establishes HTTP connection.
	 * 
	 * @param bodhiServerURL
	 *            The base URL to the Bodhi server.
	 * 
	 */
	public BodhiClient(URL bodhiServerURL) {
		this.httpclient = getClient();
		this.bodhiServerUrl = bodhiServerURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fedoraproject.eclipse.packager.IBodhiClient#login(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public BodhiLoginResponse login(String username, String password)
			throws BodhiClientLoginException {
		BodhiLoginResponse result = null;
		try {
			HttpPost post = new HttpPost(getLoginUrl());
			// Add "Accept: application/json" HTTP header
			post.addHeader(ACCEPT_HTTP_HEADER_NAME, MIME_JSON);

			// Construct the multipart POST request body.
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart(LOGIN_PARAM_NAME, new StringBody(
					LOGIN_PARAM_VALUE));
			reqEntity.addPart(USERNAME_PARAM_NAME, new StringBody(username));
			reqEntity.addPart(PASSWORD_PARAM_NAME, new StringBody(password));

			post.setEntity(reqEntity);

			HttpResponse response = httpclient.execute(post);
			HttpEntity resEntity = response.getEntity();
			int returnCode = response.getStatusLine().getStatusCode();

			if (returnCode != HttpURLConnection.HTTP_OK) {
				throw new BodhiClientLoginException(NLS.bind(
						"{0} {1}", response.getStatusLine().getStatusCode(), //$NON-NLS-1$
						response.getStatusLine().getReasonPhrase()), response);
			} else {
				// Got a 200, response body is the JSON passed on from the
				// server.
				String jsonString = ""; //$NON-NLS-1$
				if (resEntity != null) {
					try {
						jsonString = parseResponse(resEntity);
					} catch (IOException e) {
						// ignore
					} finally {
						EntityUtils.consume(resEntity); // clean up resources
					}
				}
				// log JSON string if in debug mode
				if (PackagerPlugin.inDebugMode()) {
					FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
					logger.logInfo(NLS.bind(BodhiText.BodhiClient_rawJsonStringMsg, jsonString));
				}
				// Deserialize from JSON
				GsonBuilder gsonBuilder = new GsonBuilder();
				gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
				Gson gson = gsonBuilder.create();
				result = gson.fromJson(jsonString, BodhiLoginResponse.class);
			}
		} catch (IOException e) {
			throw new BodhiClientLoginException(e.getMessage(), e);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.bodhi.api.IBodhiClient#logout()
	 */
	@Override
	public void logout() throws BodhiClientException {
		try {
			HttpPost post = new HttpPost(getLogoutUrl());
			post.addHeader(ACCEPT_HTTP_HEADER_NAME, MIME_JSON);

			HttpResponse response = httpclient.execute(post);
			HttpEntity resEntity = response.getEntity();
			int returnCode = response.getStatusLine().getStatusCode();

			if (returnCode >= 400) {
				throw new BodhiClientException(NLS.bind(
						"{0} {1}", response.getStatusLine().getStatusCode(), //$NON-NLS-1$
						response.getStatusLine().getReasonPhrase()), response);
			} else {
				String responseString = ""; //$NON-NLS-1$
				if (resEntity != null) {
					try {
						responseString = parseResponse(resEntity);
					} catch (IOException e) {
						// ignore
					}
					EntityUtils.consume(resEntity); // clean up resources
				}
				// log JSON string if in debug mode
				if (PackagerPlugin.inDebugMode()) {
					FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
					logger.logInfo(NLS.bind(BodhiText.BodhiClient_rawJsonStringMsg, responseString));
				}
			}
		} catch (IOException e) {
			throw new BodhiClientException(e.getMessage(), e);
		} finally {
			shutDownConnection();
		}
	}

	/**
	 * Shut down the connection of this client.
	 */
	public void shutDownConnection() {
		// When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
		httpclient.getConnectionManager().shutdown();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fedoraproject.eclipse.packager.bodhi.api.IBodhiClient#createNewUpdate
	 * (java.lang.String[], java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * boolean, boolean, int, int, boolean)
	 */
	@Override
	public BodhiUpdateResponse createNewUpdate(String[] builds, String release,
			String type, String request, String bugs, String notes,
			String csrfToken, boolean suggestReboot,
			boolean enableKarmaAutomatism, int stableKarmaThreshold,
			int unstableKarmaThreshold, boolean closeBugsWhenStable) throws BodhiClientException {
		try {
			HttpPost post = new HttpPost(getPushUpdateUrl());
			post.addHeader(ACCEPT_HTTP_HEADER_NAME, MIME_JSON);

			StringBuffer buildsNVR = new StringBuffer();
			for (int i = 0; i < (builds.length - 1); i++) {
				buildsNVR.append(builds[i]);
				buildsNVR.append(BUILDS_DELIMITER);
			}
			buildsNVR.append(builds[(builds.length - 1)]);
			String buildsParamValue = buildsNVR.toString();

			// Construct the multipart POST request body.
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart(BUILDS_PARAM_NAME, new StringBody(
					buildsParamValue));
			reqEntity.addPart(TYPE_PARAM_NAME, new StringBody(type));
			reqEntity.addPart(REQUEST_PARAM_NAME, new StringBody(request));
			reqEntity.addPart(BUGS_PARAM_NAME, new StringBody(bugs));
			reqEntity.addPart(CSRF_PARAM_NAME, new StringBody(csrfToken));
			reqEntity.addPart(AUTOKARMA_PARAM_NAME,
					new StringBody(String.valueOf(enableKarmaAutomatism)));
			reqEntity.addPart(NOTES_PARAM_NAME, new StringBody(notes));
			reqEntity.addPart(SUGGEST_REBOOT,
					new StringBody(String.valueOf(suggestReboot)));
			reqEntity.addPart(STABLE_KARMA,
					new StringBody(String.valueOf(stableKarmaThreshold)));
			reqEntity.addPart(UNSTABLE_KARMA,
					new StringBody(String.valueOf(unstableKarmaThreshold)));
			reqEntity.addPart(CLOSE_BUGS_WHEN_STABLE,
					new StringBody(String.valueOf(closeBugsWhenStable)));

			post.setEntity(reqEntity);

			HttpResponse response = httpclient.execute(post);
			HttpEntity resEntity = response.getEntity();
			int returnCode = response.getStatusLine().getStatusCode();

			if (returnCode != HttpURLConnection.HTTP_OK) {
				throw new BodhiClientException(NLS.bind(
						"{0} {1}", response.getStatusLine().getStatusCode(), //$NON-NLS-1$
						response.getStatusLine().getReasonPhrase()), response);
			} else {
				String rawJsonString = ""; //$NON-NLS-1$
				if (resEntity != null) {
					try {
						rawJsonString = parseResponse(resEntity);
					} catch (IOException e) {
						// ignore
					}
					EntityUtils.consume(resEntity); // clean up resources
				}
				// log JSON string if in debug mode
				if (PackagerPlugin.inDebugMode()) {
					FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
					logger.logInfo(NLS.bind(BodhiText.BodhiClient_rawJsonStringMsg, rawJsonString));
				}
				// deserialize the result from the JSON response
				GsonBuilder gsonBuilder = new GsonBuilder();
				Gson gson = gsonBuilder.create();
				BodhiUpdateResponse result = gson.fromJson(rawJsonString, BodhiUpdateResponse.class);
				return result;
			}
		} catch (IOException e) {
			throw new BodhiClientException(e.getMessage(), e);
		}
	}
	
	/**
	 * @return A properly configured HTTP client instance
	 */
	private HttpClient getClient() {
		// Set up client with proper timeout
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
				CONNECTION_TIMEOUT);
		return new DefaultHttpClient(params);
	}
	
	/**
	 * Helper to read response from response entity.
	 * 
	 * @param responseEntity
	 * @return
	 * @throws IOException
	 */
	private String parseResponse(HttpEntity responseEntity)
			throws IOException {

		String responseText = ""; //$NON-NLS-1$
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					responseEntity.getContent()));
			String line;
			line = br.readLine();
			while (line != null) {
				responseText += line + "\n"; //$NON-NLS-1$
				line = br.readLine();
			}
		} finally {
			// cleanup
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return responseText.trim();
	}
	
	/**
	 * 
	 * @return The login URL.
	 */
	private String getLoginUrl() {
		return this.bodhiServerUrl.toString() + "login"; //$NON-NLS-1$
	}
	
	/**
	 * 
	 * @return The URL to be used for pushing updates or {@code null}.
	 */
	private String getPushUpdateUrl() {
		return this.bodhiServerUrl.toString() + "save"; //$NON-NLS-1$		
	}
	
	/**
	 * 
	 * @return The URL to be used for pushing updates or {@code null}.
	 */
	private String getLogoutUrl() {
		return this.bodhiServerUrl.toString() + "logout"; //$NON-NLS-1$		
	}
}
