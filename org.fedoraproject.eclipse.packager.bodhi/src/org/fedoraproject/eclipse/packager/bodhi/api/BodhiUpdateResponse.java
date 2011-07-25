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

import org.fedoraproject.eclipse.packager.bodhi.api.deserialization.BodhiUpdate;

/**
 * Response returned on
 * {@code IBodhiClient#newUpdate(String, String, String, String, String, String, String)}
 * 
 */
public class BodhiUpdateResponse {
	
	private String tg_flash;
	private BodhiUpdate[] updates;
	
	/**
	 * Google GSON wants this.
	 */
	public BodhiUpdateResponse() {
		// nothing
	}
	
	/**
	 * @return the tg_flash
	 */
	public String getFlashMsg() {
		return tg_flash;
	}
	/**
	 * @return the updates
	 */
	public BodhiUpdate[] getUpdates() {
		return updates;
	}
}
