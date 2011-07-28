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

}
