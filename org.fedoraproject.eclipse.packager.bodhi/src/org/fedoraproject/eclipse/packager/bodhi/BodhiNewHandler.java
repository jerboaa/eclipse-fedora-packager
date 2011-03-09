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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.bodhi.stubs.BodhiClientStub;
import org.fedoraproject.eclipse.packager.bodhi.stubs.BodhiNewDialogStub;
import org.fedoraproject.eclipse.packager.bodhi.stubs.UserValidationDialogStub;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.utils.RPMUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handler for pushing Bodhi updates.
 */
public class BodhiNewHandler extends AbstractHandler {

	protected IBodhiNewDialog dialog;
	protected IUserValidationDialog authDialog;
	protected IBodhiClient bodhi;
	protected Shell shell;

    ///////////////////////////////////////////////////////////////
	// FIXME: This is used for testing only and is super-ugly, but mocking
	//        things in conjunction with SWTBotTests doesn't work. We really
	//        need to refactor this.
	//        Better ideas very welcome!
	/**
	 * Indicates if stub or fleshed out objects are returned.
	 */
	public static boolean inTestingMode = false;
	// stubs
	private IBodhiNewDialog bodhiDialogStub = new BodhiNewDialogStub(this);
	private IUserValidationDialog validationDialogStub = new UserValidationDialogStub();
	private IBodhiClient bodhiClientStub = new BodhiClientStub();
	
	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final FedoraProjectRoot fedoraProjectRoot;
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(e);
			fedoraProjectRoot = FedoraPackagerUtils
					.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e1) {
			// TODO Handle appropriately
			e1.printStackTrace();
			return null;
		}
		final IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(fedoraProjectRoot);
		// Need to have shell variable on heap not on a thread's stack.
		// Hence, the instance variable "shell".
		shell = getShell(e);
		
		Job job = new Job(Messages.bodhiNewHandler_jobName) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.bodhiNewHandler_createUpdateMsg, 
						IProgressMonitor.UNKNOWN);
				monitor.subTask(Messages.bodhiNewHandler_checkTagMsg);
				try {
					String tag = RPMUtils.makeTagName(fedoraProjectRoot);
					String branchName = projectBits.getCurrentBranchName();

					// ensure branch is tagged properly before proceeding
					if (!projectBits.needsTag() || projectBits.isVcsTagged(fedoraProjectRoot, tag)) {
						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						monitor.subTask(Messages.bodhiNewHandler_querySpecFileMsg);
						// Parsing changelog from spec-file seems to be broken
						// This always returns "". See #49
						String clog = "";
						String bugIDs = findBug(clog);
						String buildName = getBuildName(fedoraProjectRoot);
						String release = getReleaseName(fedoraProjectRoot);

						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						// if debugging, want to use stub
						if (!inTestingMode) {
							setDialog(new BodhiNewDialog(shell, buildName,
									release, bugIDs, clog));
						} else {
							setDialog(bodhiDialogStub);
						}
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								getDialog().open();
							}
						});

						if (getDialog().getReturnCode() == Window.OK) {
							String type = getDialog().getType();
							String request = getDialog().getRequest();
							String bugs = getDialog().getBugs();
							String notes = getDialog().getNotes();

							String cachedUsername = retrievePreference("username"); //$NON-NLS-1$
							String cachedPassword = null;
							if (cachedUsername != null) {
								cachedPassword = retrievePreference("password"); //$NON-NLS-1$
							}
							if (cachedPassword == null) {
								cachedUsername = System.getProperty("user.name"); //$NON-NLS-1$
								cachedPassword = ""; //$NON-NLS-1$
							}

							if (monitor.isCanceled()) {
								throw new OperationCanceledException();
							}
							if (!inTestingMode) {
								setAuthDialog(new UserValidationDialog(
										shell, BodhiClient.BODHI_URL, cachedUsername,
										cachedPassword,
										Messages.bodhiNewHandler_updateLoginMsg,
								"icons/bodhi-icon-48.png")); //$NON-NLS-1$
							} else {
								setAuthDialog(validationDialogStub);
							}
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									getAuthDialog().open();
								}
							});
							if (getAuthDialog().getReturnCode() != Window.OK) {
								// Canceled
								return Status.CANCEL_STATUS;
							}

							String username = getAuthDialog().getUsername();
							String password = getAuthDialog().getPassword();

							IStatus result = newUpdate(buildName, release,
									type, request, bugs, notes, username,
									password, monitor);


							String message = result.getMessage();
							String buildNameCandidate = "N/A";  //$NON-NLS-1$
							for (IStatus child : result.getChildren()) {
								// If successful we should have gotten the build name in
								// response as part of child status messages
								if (child.getMessage().equals(buildName)) {
									buildNameCandidate = buildName;
								} else {
									message += "\n" + child.getMessage(); //$NON-NLS-1$
								}
							}
							final String successMsg = message;
							final String bodhiBuildName = buildNameCandidate;
							// success
							if (result.isOK()) {
								// Show info about the update
								PlatformUI.getWorkbench().getDisplay()
										.asyncExec(new Runnable() {
											@Override
											public void run() {
												BodhiUpdateInfoDialog infoDialog = new BodhiUpdateInfoDialog(shell, bodhiBuildName, successMsg);
												infoDialog.open();
											}
										});

								if (getAuthDialog().getAllowCaching()) {
									storeCredentials(username, password);
								}
							} else {
								FedoraHandlerUtils.handleError(message);
							}
							return result;
						}
						else {
							return Status.CANCEL_STATUS;
						}
					} else {
						return FedoraHandlerUtils.handleError(NLS.bind(Messages.bodhiNewHandler_notCorrectTagFail, branchName, tag));
					}
				} catch (CoreException e) {
					e.printStackTrace();
					return FedoraHandlerUtils.handleError(e);
				}
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}


	/**
	 * Get Bodhi release name of the current branch.
	 * 
	 * @param projectRoot
	 * @return The release name.
	 * @throws CoreException
	 */
	public String getReleaseName(FedoraProjectRoot projectRoot) throws CoreException {
		IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(projectRoot);
		return projectBits.getCurrentBranchName().replaceAll("-", "");
	}

	/**
	 * Get Bodhi build name from spec file.
	 * 
	 * @param projectRoot
	 * @return The build name as specified in the spec file.
	 * @throws CoreException
	 */
	public String getBuildName(FedoraProjectRoot projectRoot) throws CoreException {
		return RPMUtils.rpmQuery(projectRoot, "NAME") + "-" //$NON-NLS-1$ //$NON-NLS-2$
		+ RPMUtils.rpmQuery(projectRoot, "VERSION") + "-" //$NON-NLS-1$ //$NON-NLS-2$
		+ RPMUtils.rpmQuery(projectRoot, "RELEASE"); //$NON-NLS-1$
	}

	/**
	 * Get the Bodhi client.
	 * 
	 * @return The real bodhi client or a stubbed client
	 *         depending if inTestingMode returns true or false.
	 */
	public IBodhiClient getBodhi() {
		if (!inTestingMode) {
			return bodhi;
		} else {
			return bodhiClientStub;
		}
	}

	/**
	 * Set Bodhi client instance.
	 * 
	 * @param bodhi
	 */
	public void setBodhi(IBodhiClient bodhi) {
		this.bodhi = bodhi;
	}

	/**
	 * Get user validation dialog.
	 * 
	 * @return The user validation dialog.
	 */
	public IUserValidationDialog getAuthDialog() {
		if (!inTestingMode) {
			return authDialog;
		} else {
			return validationDialogStub;
		}
	}

	/**
	 * Set user validation dialog.
	 * 
	 * @param authDialog
	 */
	public void setAuthDialog(IUserValidationDialog authDialog) {
		this.authDialog = authDialog;
	}

	/**
	 * Get the Bodhi UI dialog for pushing updates.
	 *  
	 * @return The UI dialog.
	 */
	public IBodhiNewDialog getDialog() {
		if (!inTestingMode) {
			return dialog;
		} else {
			return bodhiDialogStub;
		}
	}

	/**
	 * Set the Bodhi UI dialog.
	 * 
	 * @param dialog
	 */
	public void setDialog(IBodhiNewDialog dialog) {
		this.dialog = dialog;
	}

	private void storeCredentials(String username, String password) {
		ISecurePreferences node = getBodhiNode();
		if (node != null) {
			try {
				node.put("username", username, false); //$NON-NLS-1$
				node.put("password", password, true); //$NON-NLS-1$
			} catch (StorageException e) {
				e.printStackTrace();
				FedoraHandlerUtils.handleError(e);
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
			FedoraHandlerUtils.handleError(e);
		}
		return null;
	}

	/**
	 * FIXME: Passing things around wrapped in an IStatus seems pretty convoluted.
	 * There's got to be a better way of doing this.
	 * 
	 * @param buildName
	 * @param release
	 * @param type
	 * @param request
	 * @param bugs
	 * @param notes
	 * @param username
	 * @param password
	 * @param monitor
	 * @return
	 */
	protected IStatus newUpdate(String buildName, String release, String type,
			String request, String bugs, String notes, String username,
			String password, IProgressMonitor monitor) {
		IStatus status;

		try {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.subTask(Messages.bodhiNewHandler_connectToBodhi);
			if (!inTestingMode) {
				setBodhi(new BodhiClient());
			} else {
				setBodhi(bodhiClientStub);
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// Login
			monitor.subTask(Messages.bodhiNewHandler_loginBodhi);
			JSONObject result = getBodhi().login(username, password);
			if (result.has("message")) { //$NON-NLS-1$
				throw new IOException(result.getString("message")); //$NON-NLS-1$
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// create new update
			monitor.subTask(Messages.bodhiNewHandler_sendNewUpdate);
			result = getBodhi().newUpdate(buildName, release, type, request, bugs,
					notes, result.getString("_csrf_token"));
			// Note bodhi's "tg_flash" appears to be the Turbo Gears flash message
			// which shows up in the Web interface indicating something happened
			String bodhiRespStatus = "";
			if (result.has("tg_flash")) {
				bodhiRespStatus = result.getString("tg_flash");
			}
			status = new MultiStatus(BodhiPlugin.PLUGIN_ID, IStatus.OK,
					bodhiRespStatus, null);
			/* 
			 * Luke Macken <lmacken@redhat.com> as to what the "save" JSON method
			 * call returns:
			 *  
			 * The save method will return a dictionary of updates that were
			 * created with the update.  So, you could also grab the update title
			 * (which is just a comma-delimeted list of n-v-r's) via 
			 * result['updates'][0]['title'], or something of the sort.
			 */
			// If we've got "updates" in the response we should be OK for extracting
			// the update title.
			if (result.has("updates")) { //$NON-NLS-1$
				JSONArray updates = result.getJSONArray("updates");
				if (updates.length() > 0) {
					JSONObject update = updates.getJSONObject(0);
					if (update.has("title")) {
						((MultiStatus) status).add(new Status(IStatus.OK,
								BodhiPlugin.PLUGIN_ID, update.getString("title"))); //$NON-NLS-1$
					}
				}
			}
			
			// Logout
			monitor.subTask(Messages.bodhiNewHandler_logoutMsg);
			getBodhi().logout();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e.getMessage());
		} catch (ParseException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e.getMessage());
		} catch (JSONException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e.getMessage());
		}

		return status;
	}

	/**
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
	
	/**
	 * @param event
	 * @return the shell
	 * @throws ExecutionException
	 */
	private Shell getShell(ExecutionEvent event) throws ExecutionException {
		return HandlerUtil.getActiveShellChecked(event);
	}

}
