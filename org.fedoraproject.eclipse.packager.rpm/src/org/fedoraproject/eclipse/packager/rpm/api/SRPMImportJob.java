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
package org.fedoraproject.eclipse.packager.rpm.api;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.SourcesFileUpdater;
import org.fedoraproject.eclipse.packager.api.UploadSourceCommand;
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
import org.fedoraproject.eclipse.packager.rpm.RPMPlugin;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.errors.SRPMImportCommandException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Job that has functionality similar to the Fedora Packager import command.
 * 
 */
public class SRPMImportJob extends Job {

	private String srpmPath;
	private IContainer fprContainer;
	private SRPMImportCommand srpmImport;
	private UploadSourceCommand upload;
	private final FedoraPackagerLogger logger = FedoraPackagerLogger
			.getInstance();
	private Shell shell;

	/**
	 * @param name
	 *            Name of the job.
	 * @param shell
	 *            Shell the job is run in.
	 * @param fpRoot
	 *            Root of the project.
	 * @param srpmPath
	 *            Path to the srpm.
	 */
	public SRPMImportJob(String name, Shell shell, IContainer fpRoot,
			String srpmPath) {
		super(name);
		this.shell = shell;
		this.srpmPath = srpmPath;
		fprContainer = fpRoot;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(RpmText.SRPMImportJob_InitialSetup,
				IProgressMonitor.UNKNOWN);
		IStatus res = null;
		try {
			Set<String> stageSet = new HashSet<String>();
			Set<String> uploadedFiles = new HashSet<String>();
			logger.logDebug(NLS.bind(FedoraPackagerText.callingCommand,
					SRPMImportCommand.class.getName()));
			srpmImport = new SRPMImportCommand(srpmPath,
					fprContainer.getProject());
			monitor.setTaskName(RpmText.SRPMImportJob_ExtractingSRPM);
			SRPMImportResult importResult;
			importResult = srpmImport.call(monitor);

			if (!importResult.wasSuccessful()) {
				return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
						RpmText.SRPMImportJob_ExtractFailed);
			}
			// Rename .spec file if it does not have the name we require for a
			// valid
			// project root.
			for (IResource resource : fprContainer.members()) {
				if (resource.getName().endsWith(".spec") && !resource.getName().equals(fprContainer.getProject().getName().concat(".spec"))) { //$NON-NLS-1$ //$NON-NLS-2$
					resource.move(new Path(fprContainer.getProject().getName()
							.concat(".spec")), true, new NullProgressMonitor()); //$NON-NLS-1$
				}
			}

			monitor.setTaskName(RpmText.SRPMImportJob_UploadingSources);
			// create sources file if one does not exist
			fprContainer
					.getFile(new Path("sources")).getLocation().toFile().createNewFile(); //$NON-NLS-1$
			fprContainer.getProject().refreshLocal(IResource.DEPTH_INFINITE,
					monitor);

			// Should now have a valid project root, so get it.
			IProjectRoot fpr = FedoraPackagerUtils.getProjectRoot(fprContainer);

			// Make sure if the imported SRPM makes sense
			if (!fpr.getSpecfileModel().getName()
					.equals(fprContainer.getProject().getName())) {
				String errorMsg = NLS.bind(
						RpmText.SRPMImportJob_PackageNameSpecNameMismatchError,
						fprContainer.getProject().getName(), fpr
								.getSpecfileModel().getName());
				logger.logError(errorMsg);
				return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
						errorMsg);
			}

			FedoraPackager fp = new FedoraPackager(fpr);
			// get upload source command
			upload = (UploadSourceCommand) fp
					.getCommandInstance(UploadSourceCommand.ID);

			for (String file : srpmImport.getUploadFiles()) {
				// This won't find the .spec file since it has been removed above.
				// We don't care, since it shouldn't get uploaded anyway.
				IResource candidate = fprContainer.findMember(new Path(file));
				if (candidate != null
						&& FedoraPackagerUtils.isValidUploadFile(candidate
								.getLocation().toFile())) {
					File newUploadFile = candidate.getLocation().toFile();
					SourcesFileUpdater sourcesUpdater = new SourcesFileUpdater(
							fpr, newUploadFile);
					// Note that ignore file may not exist, yet
					IFile gitIgnore = fpr.getIgnoreFile();
					VCSIgnoreFileUpdater vcsIgnoreFileUpdater = new VCSIgnoreFileUpdater(
							newUploadFile, gitIgnore);
					String uploadUrl = PackagerPlugin
							.getStringPreference(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_UPLOAD_URL);
					if (uploadUrl != null) {
						upload.setUploadURL(uploadUrl);
					}
					upload.setFileToUpload(newUploadFile);
					// enable SLL authentication
					upload.setFedoraSSLEnabled(true);
					upload.addCommandListener(sourcesUpdater);
					upload.addCommandListener(vcsIgnoreFileUpdater);
					logger.logDebug(NLS.bind(FedoraPackagerText.callingCommand,
							UploadSourceCommand.class.getName()));
					try {
						upload.call(new NullProgressMonitor());
					} catch (FileAvailableInLookasideCacheException e) {
						// ignore, srpms with multiple files may have unchanged
						// files
					}
					uploadedFiles.add(file);
				} else {
					stageSet.add(file);
				}
			}
			importResult.setUploaded(uploadedFiles.toArray(new String[0]));
			monitor.setTaskName(RpmText.SRPMImportJob_StagingChanges);
			res = Status.OK_STATUS;
			// Do VCS update
			res = FedoraPackagerUtils.getVcsHandler(fpr)
					.updateVCS(fpr, monitor);
			if (res.isOK()) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}

			// Refresh project
			IProject project = fpr.getProject();
			if (project != null) {
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}
			// stage changes
			stageSet.add(fpr.getSourcesFile().getName());
			stageSet.add(fpr.getIgnoreFile().getName());
			FedoraPackagerUtils.getVcsHandler(fpr).stageChanges(
					stageSet.toArray(new String[0]));
			importResult.setAddedToGit(stageSet.toArray(new String[0]));
		} catch (SRPMImportCommandException e) {
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (CoreException e) {
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell, this.getName(),
					e.getMessage());
			return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell, this.getName(),
					e.getMessage());
			return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (InvalidProjectRootException e) {
			// should not occur, but give reasonable error anyway
			return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (CommandListenerException e) {
			// sources file updating or vcs ignore file updating may
			// have caused an exception.
			if (e.getCause() instanceof VCSIgnoreFileUpdateException
					|| e.getCause() instanceof SourcesFileUpdateException) {
				String message = e.getCause().getMessage();
				logger.logError(message, e.getCause());
				return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
						message, e.getCause());
			}
			// Something else failed
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (CommandMisconfiguredException e) {
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (MalformedURLException e) {
			// Upload URL was invalid, something is wrong with
			// preferences.
			String message = NLS
					.bind(RpmText.SRPMImportJob_MalformedLookasideURL,
							e.getMessage());
			logger.logError(message, e);
			FedoraHandlerUtils.showInformationDialog(shell, this.getName(),
					message);
		} catch (IOException e) {
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (InvalidUploadFileException e) {
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (UploadFailedException e) {
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} finally {
			monitor.done();
		}
		return res;
	}
}
