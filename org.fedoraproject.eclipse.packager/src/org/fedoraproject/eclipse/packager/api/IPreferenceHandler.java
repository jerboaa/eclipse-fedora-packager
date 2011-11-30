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

/**
 * Handlers requiring a preference, should implement this interface. 
 *
 */
public interface IPreferenceHandler {

	/**
	 * Gets the preference required for proper functioning of the handler.
	 * 
	 * @return The value of the preference. 
	 */
	public String getPreference();
}
