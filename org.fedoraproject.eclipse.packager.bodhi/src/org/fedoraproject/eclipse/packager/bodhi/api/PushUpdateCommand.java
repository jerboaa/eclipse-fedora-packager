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
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.bodhi.BodhiText;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientException;
import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientLoginException;

/**
 * Command for pushing an update to Bodhi
 *
 */
public class PushUpdateCommand extends FedoraPackagerCommand<PushUpdateResult> {

	/**
	 * Default value for karma automatism
	 */
	public static final boolean DEFAULT_KARMA_AUTOMATISM = true;
	/**
	 * Default value for closing bugs when update becomes stable
	 */
	public static final boolean DEFAULT_CLOSE_BUGS_WHEN_STABLE = true;
	/**
	 * Default stable karma threshold
	 */
	public static final int DEFAULT_STABLE_KARMA_THRESHOLD = 3;
	/**
	 * Default unstable karma threshold
	 */
	public static final int DEFAULT_UNSTABLE_KARMA_THRESHOLD = -3;
	/**
	 * Default suggestion of reboot: {@code false}
	 */
	public static final boolean DEFAULT_SUGGEST_REBOOT = false;
	
	/**
	 * The unique ID of this command.
	 */
	public static final String ID = "PushUpdateCommand"; //$NON-NLS-1$
	
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
	private boolean suggestReboot = DEFAULT_SUGGEST_REBOOT;
	/*
	 * State info if karma automatism should be used for pushing/unpushing
	 */
	private boolean enableKarmaAutomatism = DEFAULT_KARMA_AUTOMATISM;
	private int stableKarmaThreshold = DEFAULT_STABLE_KARMA_THRESHOLD;
	private int unpushKarmaThreshold = DEFAULT_UNSTABLE_KARMA_THRESHOLD;
	private boolean closeBugsWhenStable = DEFAULT_CLOSE_BUGS_WHEN_STABLE;
	private String username;
	private String password;
	
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
	public PushUpdateCommand client(IBodhiClient client) {
		this.client = client;
		return this;
	}
	
	/**
	 * Set the advisory comment for the update. Required and may not be empty.
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
	
	/**
	 * Boolean flag which sets if associated bugs should get closed when the
	 * update becomes stable.
	 * 
	 * @param newValue
	 *            The new value.
	 * @return this instance
	 */
	public PushUpdateCommand closeBugsWhenStable(boolean newValue) {
		this.closeBugsWhenStable = newValue;
		return this;
	}
	
	/**
	 * Sets the username/password combination for the update push.
	 * @param username The username
	 * @param password The password
	 * @return this instance
	 */
	public PushUpdateCommand usernamePassword(String username, String password) {
		this.username = username;
		this.password = password;
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
		// require username/password
		if (username == null || password == null) {
			throw new CommandMisconfiguredException(
					BodhiText.PushUpdateCommand_configErrorUsernamePasswordUnset);
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
		if (comment == null || comment.equals("")) { //$NON-NLS-1$
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
	 * @throws BodhiClientLoginException If the login to bodhi failed.
	 * @throws BodhiClientException If pushing the update failed.
	 */
	@Override
	public PushUpdateResult call(IProgressMonitor monitor)
			throws CommandListenerException, CommandMisconfiguredException, BodhiClientLoginException, BodhiClientException {
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
		FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		logger.logInfo(BodhiText.PushUpdateCommand_pushingBodhiUpdateTaskMsg);
		
		assert this.client != null;
		
		monitor.beginTask(BodhiText.PushUpdateCommand_pushingBodhiUpdateTaskMsg, 4);
		monitor.subTask(BodhiText.PushUpdateCommand_loggingIn);
		monitor.worked(1);
		// login
		BodhiLoginResponse resp = this.client.login(username, password);
		monitor.worked(2);
		monitor.subTask(BodhiText.PushUpdateCommand_pushingUpdate);
		// push update
		String csrfToken = ""; //$NON-NLS-1$
		if (resp.getCsrfToken() != null) {
			csrfToken = resp.getCsrfToken();
		}
		// It looks like release is extracted from the build NVRs. Not sure, why it's there.
		BodhiUpdateResponse updateResponse = this.client
				.createNewUpdate(builds, release, updateType, requestType,
						bugs, comment, csrfToken, suggestReboot,
						enableKarmaAutomatism, stableKarmaThreshold,
						unpushKarmaThreshold, closeBugsWhenStable);
		PushUpdateResult result = new PushUpdateResult(updateResponse);
		monitor.worked(3);
		monitor.subTask(BodhiText.PushUpdateCommand_loggingOut);
		// logout
		this.client.logout();
		callPostExecListeners();
		setCallable(false); // reuse of instance's call() not allowed
		monitor.done();
		return result;
	}

}
