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
package org.fedoraproject.eclipse.packager.bodhi.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.fedoraproject.eclipse.packager.FedoraSSL;
import org.fedoraproject.eclipse.packager.FedoraSSLFactory;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientException;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientInitException;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientLoginException;

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
	
	/**
	 *  URL of the Bodhi server to which to connect to.
	 */
	public static final String BODHI_URL = "https://admin.fedoraproject.org/updates/"; //$NON-NLS-1$
	
	// Set the "Accept" HTTP header to
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
	 * @throws BodhiClientInitException
	 * 
	 */
	public BodhiClient(URL bodhiServerURL) throws BodhiClientInitException {
		// set up a proper client.
		// FIXME: Make this so that it only uses SSL, but does not require
		// Fedora certs.
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
		try {
			HttpPost post = new HttpPost(getLoginUrl());
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
				throw new BodhiClientLoginException(response.getStatusLine()
						.getReasonPhrase(), response);
			} else {
				String resString = ""; //$NON-NLS-1$
				if (resEntity != null) {
					try {
						resString = parseResponse(resEntity);
					} catch (IOException e) {
						// ignore
					}
					EntityUtils.consume(resEntity); // clean up resources
				}
				System.out
						.println("Response was:\n--------------------------\n"
								+ resString);
			}
		} catch (IOException e) {
			throw new BodhiClientLoginException(e.getMessage(), e);
		}
		return null;
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

			if (returnCode != HttpURLConnection.HTTP_OK) {
				throw new BodhiClientException(response.getStatusLine()
						.getReasonPhrase(), response);
			} else {
				String resString = ""; //$NON-NLS-1$
				if (resEntity != null) {
					try {
						resString = parseResponse(resEntity);
					} catch (IOException e) {
						// ignore
					}
					EntityUtils.consume(resEntity); // clean up resources
				}
				System.out
						.println("Response was:\n--------------------------\n"
								+ resString);
			}
		} catch (IOException e) {
			throw new BodhiClientException(e.getMessage(), e);
		} finally {
			// When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fedoraproject.eclipse.packager.IBodhiClient#newUpdate(java.lang.String
	 * , java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public BodhiUpdateResponse createNewUpdate(String[] builds, String release, String type,
			String request, String bugs, String notes, String csrfToken) throws BodhiClientException {
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
					new StringBody(String.valueOf(true)));
			reqEntity.addPart(NOTES_PARAM_NAME, new StringBody(notes));

			post.setEntity(reqEntity);

			HttpResponse response = httpclient.execute(post);
			HttpEntity resEntity = response.getEntity();
			int returnCode = response.getStatusLine().getStatusCode();

			if (returnCode != HttpURLConnection.HTTP_OK) {
				throw new BodhiClientException(response.getStatusLine()
						.getReasonPhrase(), response);
			} else {
				String resString = ""; //$NON-NLS-1$
				if (resEntity != null) {
					try {
						resString = parseResponse(resEntity);
					} catch (IOException e) {
						// ignore
					}
					EntityUtils.consume(resEntity); // clean up resources
				}
				System.out
						.println("Response was:\n--------------------------\n"
								+ resString);
			}
		} catch (IOException e) {
			throw new BodhiClientException(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * @return A properly configured HTTP client instance
	 */
	private HttpClient getClient() throws BodhiClientInitException {
		// Set up client with proper timeout
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
				CONNECTION_TIMEOUT);
		HttpClient client = null;
		try {
			client = sslEnable(new DefaultHttpClient(params));
		} catch (FileNotFoundException e) {
			throw new BodhiClientInitException(e.getMessage(), e);
		} catch (GeneralSecurityException e) {
			throw new BodhiClientInitException(e.getMessage(), e);
		} catch (IOException e) {
			throw new BodhiClientInitException(e.getMessage(), e);
		}
		return client;
	}
	
	/**
	 * Wrap a basic HttpClient object in a SSL enabled HttpClient (including
	 * Fedora SSL authentication cert) object.
	 * 
	 * @param base The HttpClient to wrap.
	 * @return The SSL wrapped HttpClient.
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	private HttpClient sslEnable(HttpClient base)
			throws GeneralSecurityException, FileNotFoundException, IOException {
		
		// Get a SSL related instance for setting up SSL connections.
		FedoraSSL fedoraSSL = FedoraSSLFactory.getInstance();
		SSLSocketFactory sf = new SSLSocketFactory(
				fedoraSSL.getInitializedSSLContext(), // may throw FileNotFoundE
				SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		ClientConnectionManager ccm = base.getConnectionManager();
		SchemeRegistry sr = ccm.getSchemeRegistry();
		Scheme https = new Scheme("https", 443, sf); //$NON-NLS-1$
		sr.register(https);
		return new DefaultHttpClient(ccm, base.getParams());
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
		return this.bodhiServerUrl.toString() + "login?tg_format=json"; //$NON-NLS-1$
	}
	
	/**
	 * 
	 * @return The URL to be used for pushing updates or {@code null}.
	 */
	private String getPushUpdateUrl() {
		return this.bodhiServerUrl.toString() + "save?tg_format=json"; //$NON-NLS-1$		
	}
	
	/**
	 * 
	 * @return The URL to be used for pushing updates or {@code null}.
	 */
	private String getLogoutUrl() {
		return this.bodhiServerUrl.toString() + "logout?tg_format=json"; //$NON-NLS-1$		
	}
}
