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
package org.fedoraproject.eclipse.packager.internal.handlers;


import java.io.File;
import java.net.MalformedURLException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.IPreferenceHandler;
import org.fedoraproject.eclipse.packager.api.SourcesFileUpdater;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;
import org.fedoraproject.eclipse.packager.api.UploadSourceResult;
import org.fedoraproject.eclipse.packager.api.VCSIgnoreFileUpdater;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.api.errors.FileAvailableInLookasideCacheException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidUploadFileException;
import org.fedoraproject.eclipse.packager.api.errors.SourcesFileUpdateException;
import org.fedoraproject.eclipse.packager.api.errors.UploadFailedException;
import org.fedoraproject.eclipse.packager.api.errors.VCSIgnoreFileUpdateException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Class responsible for uploading source files.
 * 
 * @see UploadSourceCommand
 * @see VCSIgnoreFileUpdater
 * @see SourcesFileUpdater
 */
public class UploadHandler extends FedoraPackagerAbstractHandler implements IPreferenceHandler {

	/**
	 *  Performs upload of sources (independent of VCS used), updates "sources"
	 *  file and performs necessary CVS operations to bring branch in sync.
	 *  Checks if sources have changed.
	 *  
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Shell shell = getShell(event);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		final IResource resource = FedoraHandlerUtils.getResource(event);
		try {
			setProjectRoot(FedoraPackagerUtils.getProjectRoot(resource));
		} catch (InvalidProjectRootException e) {
			logger.logError(FedoraPackagerText.invalidFedoraProjectRootError, e);
			FedoraHandlerUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
					FedoraPackagerText.invalidFedoraProjectRootError);
			return null;
		}
		FedoraPackager packager = new FedoraPackager(getProjectRoot());
		final UploadSourceCommand uploadCmd;
		try {
			// Get DownloadSourceCommand from Fedora packager registry
			uploadCmd = (UploadSourceCommand) packager
					.getCommandInstance(UploadSourceCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					getProjectRoot().getProductStrings().getProductName(), e.getMessage());
			return null;
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					getProjectRoot().getProductStrings().getProductName(), e.getMessage());
			return null;
		}
		final IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(getProjectRoot());
		// Do the uploading
		Job job = new Job(FedoraPackagerText.UploadHandler_taskName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask(FedoraPackagerText.UploadHandler_taskName, 1);

				File newUploadFile = resource.getLocation().toFile();
				SourcesFile sourceFile = getProjectRoot().getSourcesFile();
				if (sourceFile.getSources().containsKey(resource.getName())) {
					String checksum = SourcesFile.calculateChecksum(newUploadFile);
					if (checksum.equals(sourceFile.getSources().get(resource.getName()))) {
						// Candidate file already in sources and up-to-date
						logger.logDebug(NLS.bind(
								FedoraPackagerText.UploadHandler_versionOfFileExistsAndUpToDate,
								resource.getName()));
						FedoraHandlerUtils.showInformationDialog(shell,
								getProjectRoot().getProductStrings().getProductName(),
								NLS.bind(
										FedoraPackagerText.UploadHandler_versionOfFileExistsAndUpToDate,
										resource.getName()));
						return Status.OK_STATUS;
					}
				}

				SourcesFileUpdater sourcesUpdater = new SourcesFileUpdater(getProjectRoot(),
						newUploadFile);
				sourcesUpdater.setShouldReplace(shouldReplaceSources());
				// Note that ignore file may not exist, yet
				IFile gitIgnore = getProjectRoot().getIgnoreFile();
				VCSIgnoreFileUpdater vcsIgnoreFileUpdater = new VCSIgnoreFileUpdater(newUploadFile, gitIgnore);
				
				UploadSourceResult result = null;
				try {
					String uploadUrl = getPreference();
					if (uploadUrl != null) {
						// "http://upload-cgi/cgi-bin/upload.cgi"
						uploadCmd.setUploadURL(uploadUrl);
					}
					uploadCmd.setFileToUpload(newUploadFile);
					// Set the SSL policy. We have different policies for Fedora and
					// RHEL. This should be kept in placed as it is overridden in the Red Hat version.
					setSSLPolicy(uploadCmd, uploadUrl);
					uploadCmd.addCommandListener(sourcesUpdater);
					uploadCmd.addCommandListener(vcsIgnoreFileUpdater);
					logger.logDebug(NLS.bind(FedoraPackagerText.callingCommand,
							UploadSourceCommand.class.getName()));
					try {
						result = uploadCmd.call(new SubProgressMonitor(monitor, 1));
					} catch (FileAvailableInLookasideCacheException e) {
						// File already in lookaside cache. This means we do not
						// need to upload, but we should still update sources files
						// and vcs ignore files as required.
						logger.logDebug(e.getMessage(), e);
						sourcesUpdater.postExecution();
						vcsIgnoreFileUpdater.postExecution();
						// report that there was no upload required.
						FedoraHandlerUtils.showInformationDialog(shell,
								getProjectRoot().getProductStrings().getProductName(),
								e.getMessage());
						return Status.OK_STATUS;
					} 
				} catch (CommandListenerException e) {
					// sources file updating or vcs ignore file updating may
					// have caused an exception.
					if (e.getCause() instanceof VCSIgnoreFileUpdateException ||
							e.getCause() instanceof SourcesFileUpdateException	) {
						String message = e.getCause().getMessage();
						logger.logError(message, e.getCause());
						return FedoraHandlerUtils
								.errorStatus(PackagerPlugin.PLUGIN_ID, message,
										e.getCause());
					}
					// Something else failed
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.errorStatus(
							PackagerPlugin.PLUGIN_ID, e.getMessage(), e);
				} catch (CommandMisconfiguredException e) {
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.errorStatus(
							PackagerPlugin.PLUGIN_ID, e.getMessage(), e);
				} catch (UploadFailedException e) {
					// Check if cert has expired, give some more 
					// meaningful error in that case
					if (e.isCertificateExpired()) {
						String msg = NLS
								.bind(FedoraPackagerText.UploadHandler_expiredCertificateError,
										getProjectRoot().getProductStrings()
												.getDistributionName());
						logger.logError(msg, e);
						return FedoraHandlerUtils.errorStatus(
								PackagerPlugin.PLUGIN_ID, msg, e);
					}
					// Check if cert has been revoked, give some more 
					// meaningful error in that case
					if (e.isCertificateRevoked()) {
						String msg = NLS
								.bind(FedoraPackagerText.UploadHandler_revokedCertificateError,
										getProjectRoot().getProductStrings()
												.getDistributionName());
						logger.logError(msg, e);
						return FedoraHandlerUtils.errorStatus(
								PackagerPlugin.PLUGIN_ID, msg, e);
					}
					// something else failed
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.errorStatus(
							PackagerPlugin.PLUGIN_ID, e.getMessage(), e);
				} catch (InvalidUploadFileException e) {
					logger.logDebug(e.getMessage(), e);
					FedoraHandlerUtils.showInformationDialog(shell,
							getProjectRoot().getProductStrings().getProductName(),
							e.getMessage());
					return Status.OK_STATUS;
				} catch (MalformedURLException e) {
					// Upload URL was invalid, something is wrong with
					// preferences.
					String message = NLS.bind(
							FedoraPackagerText.UploadHandler_invalidUrlError,
							e.getMessage());
					logger.logDebug(message, e);
					FedoraHandlerUtils.showInformationDialog(shell,
							getProjectRoot().getProductStrings().getProductName(), message);
					return Status.OK_STATUS;
				}

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				
				// result may be null if upload file was already in lookaside
				// cache.
				if (result != null && !result.wasSuccessful()) {
					// probably a 404 or some such
					String message = result.getErrorString();
					logger.logDebug(message);
					return FedoraHandlerUtils.errorStatus(
							PackagerPlugin.PLUGIN_ID, message);
				}

				IStatus res = Status.OK_STATUS;
				// Do VCS update
				res = projectBits.updateVCS(getProjectRoot(), monitor);
				if (res.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
				
				// Refresh project
				IProject project = getProjectRoot().getProject();
				if (project != null) {
					try {
						project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
				
				return res;
			}

		};
		job.setUser(true);
		job.schedule();
		return null; // must be null
	}

	/**
	 * Determines if {@code sources} file should be replaced or not.
	 * 
	 * @return {@code true} if and only if {@code sources} file should be
	 *         replaced with new content.
	 * @see NewSourcesHandler
	 */
	protected boolean shouldReplaceSources() {
		return false;
	}

	/**
	 * Sets the SSL policy for this handler.
	 */
	protected void setSSLPolicy(UploadSourceCommand uploadCmd, String uploadUrl) {
		// enable SLL authentication
		uploadCmd.setFedoraSSLEnabled(true);
	}
	
	@Override
	public String getPreference() {
		return PackagerPlugin
				.getStringPreference(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL);
	}
}
