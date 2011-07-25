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

import org.eclipse.core.runtime.IProgressMonitor;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.bodhi.BodhiText;

/**
 * Command for pushing an update to Bodhi
 *
 */
public class PushUpdateCommand extends FedoraPackagerCommand<PushUpdateResult> {

	private IBodhiClient client;
	private String[] builds; // the list of builds for the update
	private String release;  // the Fedora release for the update
	/*
	 * A comma-separated list of bugs which should get closed with
	 * this update.
	 */
	private String bugs;
	/*
	 * The comment for this update
	 */
	private String comment;
	/*
	 * String representation of the update type
	 */
	private String updateType;
	/*
	 * String representation of the update type
	 */
	private String requestType;
	/*
	 * State info if reboot after this update is suggested
	 */
	private boolean suggestReboot = false;
	/*
	 * State info if karma automatism should be used for pushing/unpushing
	 */
	private boolean enableKarmaAutomatism = true;
	private int stableKarmaThreshold = 3;
	private int unpushKarmaThreshold = -3;
	
	/**
	 * Use this if no bugs should get changed with this update.
	 */
	public static final String NO_BUGS = ""; //$NON-NLS-1$
	
	/**
	 * The type of the update.
	 *
	 */
	public static enum UpdateType {
		/**
		 * Enhancement update
		 */
		ENHANCEMENT,
		/**
		 * Bugfix update
		 */
		BUGFIX,
		/**
		 * Security update
		 */
		SECURITY,
		/**
		 * This package is newly introduced with this update
		 */
		NEWPACKAGE
	}
	
	/**
	 * The request type of the update.
	 *
	 */
	public static enum RequestType {
		/**
		 * Request this update to get pushed to stable
		 */
		STABLE,
		/**
		 * Request this update to get pushed to testing
		 */
		TESTING,
		/**
		 * No request type
		 */
		NONE
	}

	/**
	 * Setter for the update type.
	 * 
	 * @param type
	 *            The type of the update
	 * @return this instance
	 */
	public PushUpdateCommand updateType(UpdateType type) {
		switch (type) {
		case ENHANCEMENT:
			this.updateType = "enhancement"; //$NON-NLS-1$
			break;
		case BUGFIX:
			this.updateType = "bugfix"; //$NON-NLS-1$
			break;
		case SECURITY:
			this.updateType = "security"; //$NON-NLS-1$
			break;
		case NEWPACKAGE:
			this.updateType = "newpackage"; //$NON-NLS-1$
		}
		return this;
	}
	
	/**
	 * Into which repo should this update get tagged?
	 * 
	 * @param type
	 *            The type.
	 * @return this instance
	 */
	public PushUpdateCommand requestType(RequestType type) {
		switch (type) {
		case STABLE:
			this.requestType = "stable"; //$NON-NLS-1$
			break;
		case TESTING:
			this.requestType = "testing"; //$NON-NLS-1$
			break;
		case NONE:
			this.requestType = "None"; //$NON-NLS-1$
			break;
		}
		return this;
	}
	
	/**
	 * A list of bugs which this update may address.
	 * 
	 * @param bugs
	 *            A comma or space separated list of bugs or aliases. For
	 *            example: {@code #1234, 789 CVE-2008-0001}
	 * @return this instance
	 */
	public PushUpdateCommand bugs(String bugs) {
		this.bugs = bugs;
		return this;
	}
	
	/**
	 * The Fedora release for which the update should get pushed.
	 * 
	 * @param release
	 *            For example: {@code F15}
	 * @return This instance
	 */
	public PushUpdateCommand release(String release) {
		this.release = release;
		return this;
	}
	
	/**
	 * The list of builds (NVR's) which should be included in this update.
	 * Required.
	 * 
	 * @param builds
	 *            The NVRs of the builds.
	 * @return this instance.
	 */
	public PushUpdateCommand builds(String[] builds) {
		this.builds = builds;
		return this;
	}
	
	/**
	 * The client to be used. Required.
	 * 
	 * @param client A bodhi client.
	 * @return this instance.
	 */
	public PushUpdateCommand setClient(IBodhiClient client) {
		this.client = client;
		return this;
	}
	
	/**
	 * Set the advisory comment for the update. Required.
	 * 
	 * @param comment The advisory comment.
	 * @return this instance.
	 */
	public PushUpdateCommand comment(String comment) {
		this.comment = comment;
		return this;
	}
	
	/**
	 * Sets if a reboot will be suggested after a user installs this update.
	 * Optional.
	 * 
	 * @param newValue
	 *            {@code true} if a reboot should get suggested, {@code false}
	 *            otherwise.
	 * @return this instance.
	 */
	public PushUpdateCommand suggestReboot(boolean newValue) {
		this.suggestReboot = newValue;
		return this;
	}
	
	/**
	 * Set this to true if you wish that Karma automatism should be used.
	 * Optional.
	 * 
	 * @param newValue
	 *            {@code true} if karma automatism should be used, {@code false}
	 *            otherwise.
	 * @return this instance.
	 */
	public PushUpdateCommand enableAutoKarma(boolean newValue) {
		this.suggestReboot = newValue;
		return this;
	}
	
	/**
	 * Sets the stable Karma threshold for the next call(). This requires
	 * enableAutoKarma to be set to {@code true}, which is the default.
	 * 
	 * @param newThreshold
	 * @return this instance
	 */
	public PushUpdateCommand stableKarmaThreshold(int newThreshold) {
		this.stableKarmaThreshold = newThreshold;
		return this;
	}
	
	/**
	 * Sets the unstable/unpushing Karma thresold for the next call(). This requires
	 * enableAutoKarma to be set to {@code true}, which is the default.
	 * 
	 * @param newThreshold
	 * @return this instance
	 */
	public PushUpdateCommand unstableKarmaThreshold(int newThreshold) {
		this.unpushKarmaThreshold = newThreshold;
		return this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand#checkConfiguration()
	 */
	@Override
	protected void checkConfiguration() throws CommandMisconfiguredException {
		// need a bodhi client
		if (client == null) {
			throw new CommandMisconfiguredException(
					BodhiText.PushUpdateCommand_configErrorNoClient);
		}
		// need to know the updated type
		if (updateType == null) {
			throw new CommandMisconfiguredException(
					BodhiText.PushUpdateCommand_configErrorNoUpdateType);
		}
		// require the Fedora release
		if (release == null) {
			throw new CommandMisconfiguredException(
					BodhiText.PushUpdateCommand_configErrorNoFedoraRelease);
		}
		// require a comment
		if (comment == null) {
			throw new CommandMisconfiguredException(
					BodhiText.PushUpdateCommand_configErrorNoUpdateComment);
		}
		// require a list of builds
		if (builds == null || builds.length == 0) {
			throw new CommandMisconfiguredException(
					BodhiText.PushUpdateCommand_configErrorNoBuilds);
		}
	}

	/**
	 * Perform the update.
	 * 
	 * @throws CommandMisconfiguredException
	 *             If the command was configured wrongly.
	 * @throws CommandListenerException
	 *             If a command listener threw an exception.
	 * 
	 * @return the result of the update. You may use it to determine
	 *         success/failure and/or retrieve other information.
	 */
	@Override
	public PushUpdateResult call(IProgressMonitor monitor)
			throws CommandListenerException, CommandMisconfiguredException {
		try {
			callPreExecListeners();
		} catch (CommandListenerException e) {
			if (e.getCause() instanceof CommandMisconfiguredException) {
				// explicitly throw the specific exception
				throw (CommandMisconfiguredException)e.getCause();
			}
			throw e;
		}
		// bugs list may be unset
		if (this.bugs == null) {
			this.bugs = NO_BUGS;
		}
		
		
		callPostExecListeners();
		setCallable(false); // reuse of instance's call() not allowed
		
		return null;
	}

}
