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
import org.fedoraproject.eclipse.packager.git.FedoraPackagerGitText;

/**
 * Represents the result of a {@code ConvertLocalToRemoteCommand}.
 *
 */
public class ConvertLocalResult implements ICommandResult {
	private boolean successful = false;
	private boolean addRemote = false;
	private boolean addBranch = false;
	private boolean hadFetched = false;
	private Git git;

	/**
	 * @param successful the successful to set
	 */
	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	/**
	 * @param git
	 * @param addBranch
	 * @param addRemote
	 * @param hadFetched
	 */
	public ConvertLocalResult(Git git, boolean addRemote, boolean addBranch,
			boolean hadFetched) {
		super();
		this.git = git;
		this.addRemote = addRemote;
		this.addBranch = addBranch;
		this.hadFetched = hadFetched;
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

	/**
	 * @return boolean
	 */
	public boolean getAddRemote() {
		return this.addRemote;
	}

	/**
	 * @return boolean
	 */
	public boolean getAddBranch() {
		return this.addBranch;
	}

	/**
	 * @return boolean
	 */
	public boolean getHadFetched() {
		return this.hadFetched;
	}

	/**
	 * @param message
	 *            the initial message
	 * @return String the message to be shown to the user
	 */
	public String getHumanReadableMessage(String message) {
		if (addRemote && hadFetched) {
			message = message
					.concat(FedoraPackagerGitText.ConvertLocalToRemoteHandler_remoteCreatedNotifier);
		} else if (addRemote && !hadFetched) {
			message = message
					.concat(FedoraPackagerGitText.ConvertLocalToRemoteHandler_remoteAndFetchCreatedNotifier);
		} else {
			message = message
					.concat(FedoraPackagerGitText.ConvertLocalToRemoteHandler_remoteNotCreatedNotifier);
		}

		if (addBranch) {
			message = message
					.concat(FedoraPackagerGitText.ConvertLocalToRemoteHandler_branchCreatedNotifier);
		} else {
			message = message
					.concat(FedoraPackagerGitText.ConvertLocalToRemoteHandler_branchNotCreatedNotifier);
		}

		return message;
	}
}

