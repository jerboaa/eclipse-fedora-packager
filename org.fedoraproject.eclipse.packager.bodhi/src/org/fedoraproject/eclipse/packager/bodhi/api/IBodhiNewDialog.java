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
package org.fedoraproject.eclipse.packager.bodhi.api;

/**
 * Interface for Bodhi new update dialog.
 *
 */
public interface IBodhiNewDialog {

	/**
	 * Build name required by Bodhi.
	 * 
	 * @return Name of build for the current branch.
	 */
	public abstract String getBuildName();

	/**
	 * Release name as required by Bodhi.
	 * 
	 * @return The release name.
	 */
	public abstract String getRelease();

	/**
	 * Bugs related to this Bodhi update.
	 * 
	 * @return A list of bugs as String (e.g. "120, 233")
	 */
	public abstract String getBugs();

	/**
	 * Text for this Bodhi update.
	 * 
	 * @return Additional text related to this update.
	 */
	public abstract String getNotes();

	/**
	 * Type of Bodhi update.
	 * 
	 * @return Type as specified by user.
	 */
	public abstract String getType();

	/**
	 * Bodhi request.
	 * 
	 * @return Request.
	 */
	public abstract String getRequest();

	/**
	 * Open Bodhi dialog.
	 * 
	 * @return Return value.
	 */
	public abstract int open();

	/**
	 * Code as returned by the Bodhi server.
	 * 
	 * @return The return code.
	 */
	public abstract int getReturnCode();

}