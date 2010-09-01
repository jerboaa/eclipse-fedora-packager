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
import java.text.ParseException;

import org.apache.commons.httpclient.HttpException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Interface for Bodhi clients.
 */
public interface IBodhiClient {

	/**
	 * Bodhi login with username and password.
	 * 
	 * @param username
	 * @param password
	 * @return Response of the Web server as JSON.
	 * @throws IOException
	 * @throws HttpException
	 * @throws ParseException
	 * @throws JSONException
	 */
	public abstract JSONObject login(String username, String password)
			throws IOException, HttpException, ParseException, JSONException;

	/**
	 * Push new Bodhi update.
	 * 
	 * @param buildName
	 * @param release
	 * @param type
	 * @param request
	 * @param bugs
	 * @param notes
	 * @param csrf_token
	 * @return Web server response.
	 * @throws IOException
	 * @throws HttpException
	 * @throws ParseException
	 * @throws JSONException
	 */
	public abstract JSONObject newUpdate(String buildName, String release,
			String type, String request, String bugs, String notes, String csrf_token)
			throws IOException, HttpException, ParseException, JSONException;

	/**
	 * Log out from Bodhi server.
	 * 
	 * @throws IOException
	 * @throws HttpException
	 * @throws ParseException
	 */
	public abstract void logout() throws IOException, HttpException, ParseException;

}