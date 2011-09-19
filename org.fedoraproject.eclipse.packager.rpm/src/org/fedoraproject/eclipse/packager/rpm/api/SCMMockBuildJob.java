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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.BranchConfigInstance;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.DownloadSourcesJob;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.rpm.RPMPlugin;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.SCMMockBuildCommand.RepoType;
import org.fedoraproject.eclipse.packager.rpm.api.errors.MockBuildCommandException;
import org.fedoraproject.eclipse.packager.rpm.api.errors.MockNotInstalledException;
import org.fedoraproject.eclipse.packager.rpm.api.errors.UserNotInMockGroupException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Job that configures and calls SCMMockBuildCommand.
 *
 */
public class SCMMockBuildJob extends AbstractMockJob {
	
	private boolean useRepoSource = false;
	private final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
	private SCMMockBuildCommand mockBuild;
	private DownloadSourceCommand download;
	private RepoType repo;
	
	/**
	 * Constructor for a job that defaults to using source downloaded separately
	 * @param name The name of the Job
	 * @param shell The shell the Job is in
	 * @param fpRoot The root of the project the Job is run in
	 * @param repoType The type of repo containing the specfile
	 * @param bci The configuration of the branch at time of build
	 */
	public SCMMockBuildJob(String name, Shell shell, IProjectRoot fpRoot, RepoType repoType, BranchConfigInstance bci){
		super(name, shell, fpRoot, bci);
		repo = repoType;
	}
	
	/**
	 * Constructor for forcing Mock to try to build from source in the repo, ignoring any specfiles.
	 * @param name The name of the Job
	 * @param shell The shell the Job is in
	 * @param fpRoot The root of the project the Job is run in
	 * @param repoType The type of repo containing the specfile
	 * @param localSource true to force the use of local source
	 * @param bci The configuration of the branch at time of build
	 */
	public SCMMockBuildJob(String name, Shell shell, IProjectRoot fpRoot, RepoType repoType, boolean localSource, BranchConfigInstance bci){
		super(name, shell, fpRoot, bci);
		repo = repoType;
		useRepoSource = localSource;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		FedoraPackager fp = new FedoraPackager(fpr);
		try {
			download = (DownloadSourceCommand) fp
				.getCommandInstance(DownloadSourceCommand.ID);
			mockBuild = (SCMMockBuildCommand) fp
				.getCommandInstance(SCMMockBuildCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					fpr.getProductStrings().getProductName(), e.getMessage());
			return FedoraHandlerUtils
			.errorStatus(
					RPMPlugin.PLUGIN_ID,
					e.getMessage(),
					e);
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					fpr.getProductStrings().getProductName(), e.getMessage());
			return FedoraHandlerUtils
			.errorStatus(
					RPMPlugin.PLUGIN_ID,
					e.getMessage(),
					e);
		}
		logger.logDebug(NLS.bind(
				FedoraPackagerText.callingCommand,
				SCMMockBuildCommand.class.getName()));
		//sources need to be downloaded
		if (!useRepoSource){
			final String downloadUrlPreference = PackagerPlugin
					.getStringPreference(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL);
			Job downloadSourcesJob = new DownloadSourcesJob(
					RpmText.MockBuildHandler_downloadSourcesForMockBuild,
					download, fpr, shell, downloadUrlPreference, true);
			downloadSourcesJob.setUser(true);
			downloadSourcesJob.schedule();
			try {
				// wait for download job to finish
				downloadSourcesJob.join();
			} catch (InterruptedException e1) {
				throw new OperationCanceledException();
			}
			if (!downloadSourcesJob.getResult().isOK()) {
				// bail if something failed
				return downloadSourcesJob.getResult();
			}
			mockBuild.useDownloadedSourceDirectory(download.getDownloadFolderPath());
			mockBuild.useSpec(fpr.getSpecFile().getName());
		}
		mockBuild.branchConfig(bci);
		//set repo type
		mockBuild.useRepoType(repo);
		if (repo == RepoType.GIT){
			mockBuild.useRepoPath(fpr.getContainer().getParent().getRawLocation().toString());
		} else {
			mockBuild.useRepoPath(fpr.getContainer().getParent().getParent().getRawLocation().toString());
		}
		mockBuild.usePackage(fpr.getPackageName());
		mockBuild.useBranch(FedoraPackagerUtils.getVcsHandler(fpr).getRawCurrentBranchName());
		
		
		Job mockJob = new Job(fpr.getProductStrings().getProductName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask(
							RpmText.MockBuildHandler_testLocalBuildWithMock,
							IProgressMonitor.UNKNOWN);
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					try {
						result = mockBuild.call(monitor);
						fpr.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
					} catch (CommandMisconfiguredException e) {
						// This shouldn't happen, but report error
						// anyway
						logger.logError(e.getMessage(), e);
						return FedoraHandlerUtils.errorStatus(
								RPMPlugin.PLUGIN_ID, e.getMessage(), e);
					} catch (UserNotInMockGroupException e) {
						// nothing critical, advise the user what to do.
						logger.logDebug(e.getMessage());
						FedoraHandlerUtils.showInformationDialog(shell, fpr
								.getProductStrings().getProductName(), e
								.getMessage());
						return Status.OK_STATUS;
					} catch (CommandListenerException e) {
						// There are no command listeners registered, so
						// shouldn't
						// happen. Do something reasonable anyway.
						logger.logError(e.getMessage(), e);
						return FedoraHandlerUtils.errorStatus(
								RPMPlugin.PLUGIN_ID, e.getMessage(), e);
					} catch (MockBuildCommandException e) {
						// Some unknown error occurred
						logger.logError(e.getMessage(), e.getCause());
						return FedoraHandlerUtils.errorStatus(
								RPMPlugin.PLUGIN_ID, e.getMessage(),
								e.getCause());
					} catch (MockNotInstalledException e) {
						// nothing critical, advise the user what to do.
						logger.logDebug(e.getMessage());
						FedoraHandlerUtils.showInformationDialog(shell, fpr
								.getProductStrings().getProductName(), e
								.getMessage());
						return Status.OK_STATUS;
					} catch (CoreException e) {
						logger.logError(e.getMessage(), e.getCause());
						return FedoraHandlerUtils.errorStatus(
								RPMPlugin.PLUGIN_ID, e.getMessage(),
								e.getCause());
					}
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		mockJob.addJobChangeListener(getMockJobFinishedJobListener());
		mockJob.setUser(true);
		mockJob.schedule();
		try {
			// wait for job to finish
			mockJob.join();
		} catch (InterruptedException e1) {
			throw new OperationCanceledException();
		}
		return mockJob.getResult();
	}
}
