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
package org.fedoraproject.eclipse.packager.test_utils;

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.httpclient.HttpException;
import org.fedoraproject.eclipse.packager.bodhi.IBodhiClient;
import org.json.JSONException;
import org.json.JSONObject;

public class BodhiClientStub implements IBodhiClient {
	public String username;
	public String password;
	public String buildName;
	public String release;
	public String type;
	public String request;
	public String bugs;
	public String notes;
	
	public JSONObject login(String username, String password)
			throws IOException, HttpException, ParseException, JSONException {
		this.username = username;
		this.password = password;
		return new JSONObject();
	}

	public JSONObject newUpdate(String buildName, String release, String type,
			String request, String bugs, String notes, String csrfToken) throws IOException,
			HttpException, ParseException, JSONException {
		this.buildName = buildName;
		this.release = release;
		this.type = type;
		this.request = request;
		this.bugs = bugs;
		this.notes = notes;
		return new JSONObject("{\"tg_flash\": \"1337\", \"update\": \"7331\"}");
	}

	public void logout() throws IOException, HttpException, ParseException {
	}

}
