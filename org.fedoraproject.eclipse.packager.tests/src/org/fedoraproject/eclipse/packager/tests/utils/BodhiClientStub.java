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
package org.fedoraproject.eclipse.packager.tests.utils;


import org.fedoraproject.eclipse.packager.bodhi.api.BodhiLoginResponse;
import org.fedoraproject.eclipse.packager.bodhi.api.BodhiUpdateResponse;
import org.fedoraproject.eclipse.packager.bodhi.api.IBodhiClient;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientException;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientLoginException;

/**
 * Stubbed bodhi client for used for testing.
 *
 */
public class BodhiClientStub implements IBodhiClient {
	public String username;
	public String password;
	public String buildName;
	public String release;
	public String type;
	public String request;
	public String bugs;
	public String notes;
	public boolean suggestReboot;
	public boolean enableAutoKarma;
	public int stableKarmaThreshold;
	public int unstableKarmaThreshold;
	public boolean closeBugsWhenStable;
	
	@Override
	public BodhiLoginResponse login(String username, String password)
			throws BodhiClientLoginException {
		this.username = username;
		this.password = password;
		//Gson gson = new Gson();
		//return new JSONObject("{\"_csrf_token\": \"fake_token\"}");
		return new BodhiLoginResponse();
	}

	@Override
	public BodhiUpdateResponse createNewUpdate(String[] buildName, String release, String type,
			String request, String bugs, String notes, String csrfToken, boolean suggestReboot,
			boolean enableKarmaAutomatism, int stableKarmaThreshold,
			int unstableKarmaThreshold, boolean closeBugsWhenStable) throws BodhiClientException {
		this.buildName = buildName[0];
		this.release = release;
		this.type = type;
		this.request = request;
		this.bugs = bugs;
		this.notes = notes;
		this.suggestReboot = suggestReboot;
		this.enableAutoKarma = enableKarmaAutomatism;
		this.stableKarmaThreshold = stableKarmaThreshold;
		this.unstableKarmaThreshold = unstableKarmaThreshold;
		this.closeBugsWhenStable = closeBugsWhenStable;
//		return new JSONObject("{\"tg_flash\": \"Update created\"," +
//				"\"update\": \"Update id: 7331\"," +
//				"\"updates\": [ { \"title\": \"" + buildName + "\", \"something-else\": 12 } ] }");
		return new BodhiUpdateResponse();
	}

	
	@Override
	public void logout() throws BodhiClientException {
	}

}
