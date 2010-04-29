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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.fedoraproject.eclipse.packager.rpm.RPMHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class BodhiNewHandler extends RPMHandler {

	protected IBodhiNewDialog dialog;
	protected IUserValidationDialog authDialog;
	protected IBodhiClient bodhi;

	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		monitor.subTask(Messages.getString("BodhiNewHandler.0")); //$NON-NLS-1$
		try {
			String tag = makeTagName();
			String branchName = specfile.getParent().getName();

			// ensure branch is tagged properly before proceeding
			if (isTagged()) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.subTask(Messages.getString("BodhiNewHandler.1")); //$NON-NLS-1$
				String clog = getClog();
				String bugIDs = findBug(clog);
				String buildName = getBuildName();
				String release = getReleaseName();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				// if debugging, want to use stub
				if (!debug) {
					dialog = new BodhiNewDialog(shell, buildName,
							release, bugIDs, clog);
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							dialog.open();
						}						
					});
				}

				if (dialog.getReturnCode() == Window.OK) {
					String type = dialog.getType();
					String request = dialog.getRequest();
					String bugs = dialog.getBugs();
					String notes = dialog.getNotes();

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
					if (!debug) {
						authDialog = new UserValidationDialog(
								shell, BodhiClient.BODHI_URL, cachedUsername,
								cachedPassword,
								Messages.getString("BodhiNewHandler.6"), //$NON-NLS-1$
						"icons/bodhi-icon-48.png"); //$NON-NLS-1$
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								authDialog.open();
							}							
						});
					}
					if (authDialog.getReturnCode() != Window.OK) {
						// Canceled
						return Status.CANCEL_STATUS;
					}

					String username = authDialog.getUsername();
					String password = authDialog.getPassword();

					IStatus result = newUpdate(buildName, release,
							type, request, bugs, notes, username,
							password, monitor);


					String message = result.getMessage();
					for (IStatus child : result.getChildren()) {
						message += "\n" + child.getMessage(); //$NON-NLS-1$
					}

					// success
					if (result.isOK()) {
						handleOK(message, true);

						if (authDialog.getAllowCaching()) {
							storeCredentials(username, password);
						}
					} else {
						handleError(message);
					}
					return result;
				}
				else {
					return Status.CANCEL_STATUS;
				}
			} else {
				return handleError(NLS.bind(Messages.getString("BodhiNewHandler.7"), branchName, tag)); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return handleError(e);
		} catch (IOException e) {
			e.printStackTrace();
			return handleError(e);
		}
	}

	public String getReleaseName() throws CoreException {
		return getBranchName(
				specfile.getParent().getName()).replaceAll("-", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getBuildName() throws CoreException {
		return rpmQuery(specfile, "NAME") + "-" //$NON-NLS-1$ //$NON-NLS-2$
		+ rpmQuery(specfile, "VERSION") + "-" //$NON-NLS-1$ //$NON-NLS-2$
		+ rpmQuery(specfile, "RELEASE"); //$NON-NLS-1$
	}

	public IBodhiClient getBodhi() {
		return bodhi;
	}

	public void setBodhi(IBodhiClient bodhi) {
		this.bodhi = bodhi;
	}

	public IUserValidationDialog getAuthDialog() {
		return authDialog;
	}

	public void setAuthDialog(IUserValidationDialog authDialog) {
		this.authDialog = authDialog;
	}

	public IBodhiNewDialog getDialog() {
		return dialog;
	}

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
				handleError(e);
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
			handleError(e);
		}
		return null;
	}

	protected IStatus newUpdate(String buildName, String release, String type,
			String request, String bugs, String notes, String username,
			String password, IProgressMonitor monitor) {
		IStatus status;

		try {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (!debug) {
				monitor.subTask(Messages.getString("BodhiNewHandler.20")); //$NON-NLS-1$
				bodhi = new BodhiClient();
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// Login
			monitor.subTask(Messages.getString("BodhiNewHandler.21")); //$NON-NLS-1$
			JSONObject result = bodhi.login(username, password);
			if (result.has("message")) { //$NON-NLS-1$
				throw new IOException(result.getString("message")); //$NON-NLS-1$
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// create new update
			monitor.subTask(Messages.getString("BodhiNewHandler.24")); //$NON-NLS-1$
			result = bodhi.newUpdate(buildName, release, type, request, bugs,
					notes);
			status = new MultiStatus(BodhiPlugin.PLUGIN_ID, IStatus.OK, result
					.getString("tg_flash"), null); //$NON-NLS-1$
			if (result.has("update")) { //$NON-NLS-1$
				((MultiStatus) status).add(new Status(IStatus.OK,
						BodhiPlugin.PLUGIN_ID, result.getString("update"))); //$NON-NLS-1$
			}
			
			// Logout
			monitor.subTask(Messages.getString("BodhiNewHandler.28")); //$NON-NLS-1$
			bodhi.logout();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			status = handleError(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			status = handleError(e.getMessage());
		} catch (ParseException e) {
			e.printStackTrace();
			status = handleError(e.getMessage());
		} catch (JSONException e) {
			e.printStackTrace();
			status = handleError(e.getMessage());
		}

		return status;
	}

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

	@Override
	protected String getTaskName() {
		return Messages.getString("BodhiNewHandler.33"); //$NON-NLS-1$
	}

}
