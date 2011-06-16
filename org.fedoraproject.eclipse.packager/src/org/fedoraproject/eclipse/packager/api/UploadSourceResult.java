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
package org.fedoraproject.eclipse.packager.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

/**
 * Represents the result of a {@code UploadSourceCommand}.
 * This may be more useful in future.
 */
public class UploadSourceResult implements ICommandResult {
	
	private boolean successful;
	private HttpResponse response;

	/**
	 * @param response
	 */
	public UploadSourceResult(HttpResponse response) {
		this.successful = (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK);
		this.response = response;
	}

	/**
	 * See {@link ICommandResult#wasSuccessful()}.
	 */
	@Override
	public boolean wasSuccessful() {
		return successful;
	}
	
	/**
	 * @return Error message if upload was not successful, null otherwise.
	 */
	public String getErrorString() {
		if (!successful) {
			StringBuilder error = new StringBuilder();
			error.append(response.getStatusLine().getStatusCode() + " " + //$NON-NLS-1$
					response.getStatusLine().getReasonPhrase() + "\n"); //$NON-NLS-1$
			// add body if there is one
			HttpEntity responseEntity = response.getEntity();
			if (responseEntity == null) {
				return error.toString();
			}
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(
						responseEntity.getContent()));
				String line;
				line = br.readLine();
				while (line != null) {
					error.append(line + "\n"); //$NON-NLS-1$
					line = br.readLine();
				}
			} catch (IOException e) {
				// ignore
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
			return error.toString();
		}
		return null;
	}
	
	/**
	 * @return The body of the request after uploading.
	 */
	public String getMessage() {
		StringBuilder responseBody = new StringBuilder();
		// add body if there is one
		HttpEntity responseEntity = response.getEntity();
		if (responseEntity == null) {
			return responseBody.toString();
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					responseEntity.getContent()));
			String line;
			line = br.readLine();
			while (line != null) {
				responseBody.append(line + "\n"); //$NON-NLS-1$
				line = br.readLine();
			}
		} catch (IOException e) {
			// ignore
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return responseBody.toString();
	}
}
