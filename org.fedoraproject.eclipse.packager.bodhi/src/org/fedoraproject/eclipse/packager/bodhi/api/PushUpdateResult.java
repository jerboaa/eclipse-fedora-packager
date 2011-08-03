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

import org.fedoraproject.eclipse.packager.api.ICommandResult;
import org.fedoraproject.eclipse.packager.bodhi.api.deserialization.Build;

/**
 * Result of a {@link PushUpdateCommand}
 */
public class PushUpdateResult implements ICommandResult {

	// The underlying update response as it was parsed from
	// JSON
	private BodhiUpdateResponse response;

	/**
	 * Constructor
	 * 
	 * @param response The parsed JSON response object.
	 */
	public PushUpdateResult(BodhiUpdateResponse response) {
		this.response = response;
	}
	
	/**
	 * @return the response object of the update push.
	 */
	public BodhiUpdateResponse getUpdateResponse() {
		return this.response;
	}
	
	/**
	 * Will be successful if any one of the desired builds was successfully pushed.
	 */
	@Override
	public boolean wasSuccessful() {
		return response.getFlashMsg() != null
				&& response.getFlashMsg().contains("successful"); //$NON-NLS-1$
	}
	
	/**
	 * 
	 * @return Some details about the update pushing (if any). May give hints about the actual error.
	 */
	public String getDetails() {
		String details = "Unavailable"; //$NON-NLS-1$
		if (response != null && response.getFlashMsg() != null) {
			details = response.getFlashMsg();
		}
		return details;
	}
	
	/**
	 * 
	 * Retrieve the name of the update. This is usually a comma separated list
	 * of the build N-V-Rs, which were pushed. This only makes sense to call on
	 * an successful result.
	 * 
	 * @return The comma separated list of build NVRs, which got pushed as an update.
	 */
	public String getUpdateName() {
		StringBuilder updateNameBuilder = new StringBuilder();
		for (Build build: response.getUpdates()[0].getBuilds()) {
			updateNameBuilder.append(build.getNvr());
			updateNameBuilder.append(","); //$NON-NLS-1$
		}
		String result = updateNameBuilder.toString();
		return result.substring(0, result.length() - 1);
	}

}
