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

public interface IUserValidationDialog {

	/**
	 * Returns the password entered by the user, or null if the user canceled.
	 * 
	 * @return the entered password
	 */
	public abstract String getPassword();

	/**
	 * Returns the user name entered by the user, or null if the user canceled.
	 * 
	 * @return the entered user name
	 */
	public abstract String getUsername();

	/**
	 * Returns <code>true</code> if the save password checkbox was selected.
	 * 
	 * @return <code>true</code> if the save password checkbox was selected and
	 *         <code>false</code> otherwise.
	 */
	public abstract boolean getAllowCaching();

	public abstract int open();

	public abstract int getReturnCode();

}