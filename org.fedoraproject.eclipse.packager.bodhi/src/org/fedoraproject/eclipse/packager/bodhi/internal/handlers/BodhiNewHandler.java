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
package org.fedoraproject.eclipse.packager.bodhi.internal.handlers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.TagSourcesListener;
import org.fedoraproject.eclipse.packager.api.UnpushedChangesListener;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.bodhi.BodhiPlugin;
import org.fedoraproject.eclipse.packager.bodhi.BodhiText;
import org.fedoraproject.eclipse.packager.bodhi.api.BodhiClient;
import org.fedoraproject.eclipse.packager.bodhi.api.PushUpdateCommand;
import org.fedoraproject.eclipse.packager.bodhi.api.PushUpdateResult;
import org.fedoraproject.eclipse.packager.bodhi.internal.ui.BodhiNewUpdateDialog;
import org.fedoraproject.eclipse.packager.bodhi.internal.ui.BodhiUpdateInfoDialog;
import org.fedoraproject.eclipse.packager.bodhi.internal.ui.UserValidationDialog;
import org.fedoraproject.eclipse.packager.bodhi.internal.ui.UserValidationResponse;
import org.fedoraproject.eclipse.packager.bodhi.internal.ui.ValidationJob;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler for pushing Bodhi updates.
 */
public class BodhiNewHandler extends FedoraPackagerAbstractHandler {

	private static final String BODHI_INSTANCE_URL_PROP = "org.fedoraproject.eclipse.packager.bodhi.instanceUrl";
	private Shell shell;
	private URL bodhiUrl;
	private final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
	private String username;
	private String password;
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// Need to have shell variable on heap not on a thread's stack.
		// Hence, the instance variable "shell".
		shell = getShell(event);
		// May set the bodhi URL via system property
		bodhiUrl = getBodhiUrl();
		final IProjectRoot fedoraProjectRoot;
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(event);
			fedoraProjectRoot = FedoraPackagerUtils
					.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e) {
			logger.logError(FedoraPackagerText.invalidFedoraProjectRootError, e);
			FedoraHandlerUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
					FedoraPackagerText.invalidFedoraProjectRootError);
			return null;
		}
		FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
		final PushUpdateCommand update;
		try {
			// Get DownloadSourceCommand from Fedora packager registry
			update = (PushUpdateCommand) fp
					.getCommandInstance(PushUpdateCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					fedoraProjectRoot.getProductStrings().getProductName(), e.getMessage());
			return null;
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					fedoraProjectRoot.getProductStrings().getProductName(), e.getMessage());
			return null;
		}
		// require valid credentials
		UserValidationResponse validationResponse = null;
		String validationErrorMsg = "";
		UserValidationDialog userDialog = null;
		do {
			userDialog = getAuthDialog(validationErrorMsg);
			int response = userDialog.open();
			if (response != Window.OK) {
				return Status.CANCEL_STATUS;
			}
			// validate log-in credentials this may take time so do it as a job
			ValidationJob validationJob = new ValidationJob(
					"Validating Bodhi credentials ...",
					userDialog.getUsername(), userDialog.getPassword(),
					bodhiUrl);
			validationJob.setUser(true);
			validationJob.schedule();
			try {
				validationJob.join();
			} catch (InterruptedException e) {
				return Status.CANCEL_STATUS;
			}
			validationResponse = validationJob.getValidationResponse();
			if (!validationResponse.isValid()) {
				validationErrorMsg = "Username/Password combination invalid";
			}
		} while (!validationResponse.isValid());
		
		username = validationResponse.getUsername();
		password = validationResponse.getPassword();
		// username password valid, store credentials if so desired
		if (userDialog.getAllowCaching()) {
			storeCredentials(username, password);
		}
		
		// FIXME: Parsing changelog from spec-file seems to be broken
		// This always returns "". See #49
		final String clog = "";
		final String bugIDs = findBug(clog);
		final String[] builds = fedoraProjectRoot.getPackageNVRs();
		final IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(fedoraProjectRoot);
		
		// open update dialog
		final BodhiNewUpdateDialog updateDialog = new BodhiNewUpdateDialog(shell,
				builds, bugIDs, clog);
		int response = updateDialog.open();
		if (response != Window.OK) {
			return Status.CANCEL_STATUS;
		}
		// all data gathered, push update
		Job job = new Job(BodhiText.BodhiNewHandler_jobName) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(BodhiText.BodhiNewHandler_createUpdateMsg, 
						IProgressMonitor.UNKNOWN);
				try {
					UnpushedChangesListener unpushedChangesListener = new UnpushedChangesListener(
							fedoraProjectRoot, monitor);
					// check for unpushed changes prior calling command
					update.addCommandListener(unpushedChangesListener);
					// tag sources if user wishes; TagSourcesListener takes care of this
					TagSourcesListener tagSources = new TagSourcesListener(fedoraProjectRoot, monitor, shell);
					update.addCommandListener(tagSources);

					final String release = projectBits.getCurrentBranchName().replace("-", "");
					update.usernamePassword(username, password);
					update.bugs(updateDialog.getBugs());
					update.builds(updateDialog.getBuilds());
					update.client(new BodhiClient(bodhiUrl));
					update.closeBugsWhenStable(updateDialog.isCloseBugs());
					update.comment(updateDialog.getComment());
					update.enableAutoKarma(updateDialog.isKarmaAutomatismEnabled());
					update.release(release);
					update.requestType(updateDialog.getRequestType());
					update.updateType(updateDialog.getUpdateType());
					update.stableKarmaThreshold(updateDialog.getStableKarmaThreshold()).unstableKarmaThreshold(updateDialog.getUnstableKarmaThreshold());
					PushUpdateResult updateResult = update.call(monitor);
					if (updateResult.wasSuccessful()) {
						final String bodhiBuildName = "";
						final String successMsg = "";
						PlatformUI.getWorkbench().getDisplay()
								.asyncExec(new Runnable() {
									@Override
									public void run() {
										BodhiUpdateInfoDialog infoDialog = new BodhiUpdateInfoDialog(
												shell, bodhiBuildName,
												successMsg);
										infoDialog.open();
									}
								});
						return Status.OK_STATUS;
					} else {
						// show some error with details
						return FedoraHandlerUtils.errorStatus(BodhiPlugin.PLUGIN_ID, "update failed");
					}
				} catch (Exception e) {
					// TODO: handle exception
					return null;
				}
			}
		};
		job.setUser(true);
		job.schedule();
		return null; // must be null


//							String message = result.getMessage();
//							String buildNameCandidate = "N/A";  //$NON-NLS-1$
//							for (IStatus child : result.getChildren()) {
//								// If successful we should have gotten the build name in
//								// response as part of child status messages
//								if (child.getMessage().equals(""/*buildName*/)) {
//									buildNameCandidate = ""; //buildName;
//								} else {
//									message += "\n" + child.getMessage(); //$NON-NLS-1$
//								}
//							}
//							final String successMsg = message;
//							final String bodhiBuildName = buildNameCandidate;
//							// success
//							if (result.isOK()) {
								// Show info about the update

//								if (getAuthDialog().getAllowCaching()) {
//									storeCredentials(username, password);
//								}
//							} else {
//								return FedoraHandlerUtils.errorStatus(BodhiPlugin.PLUGIN_ID, message);
//							}
				 /*catch (CoreException e) {
					e.printStackTrace();
					return FedoraHandlerUtils.errorStatus(BodhiPlugin.PLUGIN_ID, e.getMessage());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				} catch (BodhiClientInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
	}


	/*
	 * Either uses a the system property "org.fedoraproject.eclipse.packager.bodhi.instanceUrl"
	 * or the Fedora default.
	 */
	private URL getBodhiUrl() {
		String bodhiTestInstanceURL = System.getProperty(BODHI_INSTANCE_URL_PROP);
		URL url = null;
		if (bodhiTestInstanceURL == null) {
			try {
				url = new URL(BodhiClient.BODHI_URL);
			} catch (MalformedURLException ignored) {
				// ignore, should not happen
			}
			return url;
		} else {
			try {
				url = new URL(bodhiTestInstanceURL);
			} catch (MalformedURLException e) {
				logger.logError("Bodhi URL set via system URL invalid", e);
			}
		}
		return url;
	}

	/**
	 * Get user validation dialog.
	 * @param errorMessage 
	 * 
	 * @return The user validation dialog.
	 */
	private UserValidationDialog getAuthDialog(String errorMessage) {
		String cachedUsername = retrievePreference("username"); //$NON-NLS-1$
		String cachedPassword = null;
		if (cachedUsername != null) {
			cachedPassword = retrievePreference("password"); //$NON-NLS-1$
		}
		if (cachedPassword == null) {
			cachedUsername = System.getProperty("user.name"); //$NON-NLS-1$
			cachedPassword = ""; //$NON-NLS-1$
		}
		return new UserValidationDialog(shell, bodhiUrl, cachedUsername,
				cachedPassword, BodhiText.BodhiNewHandler_updateLoginMsg,
				"icons/bodhi-icon-48.png", errorMessage);
	}

	/*
	 * Store credentials is secure storage
	 */
	private void storeCredentials(String username, String password) {
		ISecurePreferences node = getBodhiNode();
		if (node != null) {
			try {
				node.put("username", username, false); //$NON-NLS-1$
				node.put("password", password, true); //$NON-NLS-1$
			} catch (StorageException e) {
				e.printStackTrace();
				FedoraHandlerUtils.showErrorDialog(shell, e.getMessage(), e.toString());
			}
		}
	}

	private String retrievePreference(String pref) {
		ISecurePreferences node = getBodhiNode();
		if (node == null)
			return null;
		try {
			String username = node.get(pref, null);
			if (username != null) {
				return username;
			}
		} catch (StorageException e) {
			e.printStackTrace();
			FedoraHandlerUtils.showErrorDialog(shell, e.getMessage(), e.toString());
		}
		return null;
	}

	/*
	 * Parse bugs listed in a changelog string
	 * 
	 * @param clog
	 * @return A comma separated list of bugs, or the empty string.
	 */
	private String findBug(String clog) {
		String bugs = ""; //$NON-NLS-1$
		Pattern p = Pattern.compile("#([0-9]*)"); //$NON-NLS-1$
		Matcher m = p.matcher(clog);
		while (m.find()) {
			bugs += m.group() + ","; //$NON-NLS-1$
		}
		return bugs.length() > 0 ? bugs.substring(0, bugs.length() - 1) : bugs;
	}

	private ISecurePreferences getBodhiNode() {
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		if (preferences == null)
			return null;

		try {
			return preferences.node("bodhi"); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
			return null; // invalid path
		}
	}

}
