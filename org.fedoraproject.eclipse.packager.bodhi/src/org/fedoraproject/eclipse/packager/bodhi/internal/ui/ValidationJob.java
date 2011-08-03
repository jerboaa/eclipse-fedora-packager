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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * 
 * Validating username/password by attempting a login on the server.
 *
 */
public class ValidationJob extends Job {

	
	private UserValidationResponse response;
	private String username;
	private String password;
	private URL bodhiUrl;
	
	/**
	 * 
	 * @param jobName 
	 * @param username
	 * @param password
	 * @param bodhiUrl
	 */
	public ValidationJob(String jobName, String username, String password, URL bodhiUrl) {
		super(jobName);
		this.username = username;
		this.password = password;
		this.bodhiUrl = bodhiUrl;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
		// validate log-in credentials
		response = new UserValidationResponse(username, password, bodhiUrl);
		monitor.done();
		return Status.OK_STATUS;
	}
	
	/**
	 * 
	 * @return The validation response.
	 */
	public UserValidationResponse getValidationResponse() {
		return this.response;
	}

}
