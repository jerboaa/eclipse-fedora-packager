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
package org.fedoraproject.eclipse.packager.git.api;

import org.eclipse.jgit.api.Git;
import org.fedoraproject.eclipse.packager.api.ICommandResult;

/**
 * Represents the result of a {@code ConvertLocalToRemoteCommand}.
 *
 */
public class ConvertLocalResult implements ICommandResult {
	private boolean successful = false;
	private Git git;

	/**
	 * @param successful the successful to set
	 */
	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	/**
	 * @param git
	 */
	public ConvertLocalResult(Git git) {
		super();
		this.git = git;
	}

	/**
	 * See {@link ICommandResult#wasSuccessful()}.
	 */
	@Override
	public boolean wasSuccessful() {
		return successful;
	}
	
	/**
	 * @return Git
	 */
	public Git getGit() {
		return this.git;
	}
}
