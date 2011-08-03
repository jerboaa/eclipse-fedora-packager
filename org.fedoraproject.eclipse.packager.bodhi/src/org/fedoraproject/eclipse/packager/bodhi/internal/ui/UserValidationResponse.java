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
package org.fedoraproject.eclipse.packager.bodhi.internal.ui;

import java.net.URL;

import org.fedoraproject.eclipse.packager.bodhi.api.BodhiClient;
import org.fedoraproject.eclipse.packager.bodhi.api.BodhiLoginResponse;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientLoginException;

/**
 * Validation response if username/password combination is valid.
 */
public class UserValidationResponse {

	private String username;
	private String password;
	private URL bodhiUrl;
	private boolean valid;
	
	/**
	 * Create a validation response from dialog.
	 * @param username The username
	 * @param password The password
	 * @param bodhiUrl The URL of the bodhi instance to attempt to log in.
	 */
	public UserValidationResponse(String username, String password, URL bodhiUrl) {
		this.username = username;
		this.password = password;
		this.bodhiUrl = bodhiUrl;
		validate();
	}
	
	/*
	 * Logs onto bodhi and sets valid to true if log-in succeeded.
	 */
	private void validate() {
		BodhiClient client = new BodhiClient(bodhiUrl);
		BodhiLoginResponse loginResponse = null;
		try {
			loginResponse = client.login(username, password);
		} catch (BodhiClientLoginException e) {
			// login failed
			if (e.isInvalidCredentials()) {
				this.valid = false;
				return;
			}
		}
		// this is still a guess
		this.valid = loginResponse.getUser() != null;
		client.shutDownConnection();
	}

	/**
	 * 
	 * @return {@code true} if credentials are valid. {@code false} otherwise.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
}
