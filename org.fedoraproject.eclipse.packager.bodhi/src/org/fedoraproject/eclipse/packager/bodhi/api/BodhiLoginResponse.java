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

import org.fedoraproject.eclipse.packager.bodhi.fas.FASUser;

/**
 * Response to a bodhi client login.
 *
 */
public class BodhiLoginResponse {
	
	private String tg_flash;
	private String _csrf_token;
	private FASUser user;
	
	/**
	 * zero-arg constructor used by GSON
	 */
	public BodhiLoginResponse() {
		// empty
	}

	/**
	 * @return the tg_flash
	 */
	public String getTgFlash() {
		return tg_flash;
	}

	/**
	 * @return the _csrf_token
	 */
	public String getCsrfToken() {
		return _csrf_token;
	}

	/**
	 * @return the user
	 */
	public FASUser getUser() {
		return user;
	}
	
}
