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
package org.fedoraproject.eclipse.packager.rpm.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.BranchConfigInstance;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.DownloadSourcesJob;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.rpm.RPMPlugin;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand.BuildType;
import org.fedoraproject.eclipse.packager.rpm.api.errors.RpmBuildCommandException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler for building locally.
 * 
 */
public class LocalBuildHandler extends FedoraPackagerAbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Shell shell = getShell(event);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
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
		final RpmBuildCommand rpmBuild;
		final DownloadSourceCommand download;
		try {
			// need to get sources for an SRPM build
			download = (DownloadSourceCommand) fp
					.getCommandInstance(DownloadSourceCommand.ID);
			// get RPM build command in order to produce an SRPM
			rpmBuild = (RpmBuildCommand) fp
					.getCommandInstance(RpmBuildCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell, fedoraProjectRoot
					.getProductStrings().getProductName(), e.getMessage());
			return null;
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell, fedoraProjectRoot
					.getProductStrings().getProductName(), e.getMessage());
			return null;
		}
		Job job = new Job(fedoraProjectRoot.getProductStrings()
				.getProductName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final String downloadUrlPreference = PackagerPlugin
						.getStringPreference(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL);
				// Make sure we have sources locally
				Job downloadSourcesJob = new DownloadSourcesJob(
						RpmText.LocalBuildHandler_downloadSourcesForLocalBuild,
						download, fedoraProjectRoot, shell,
						downloadUrlPreference, true);
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
				// Do the local build
				Job rpmBuildjob = new Job(fedoraProjectRoot.getProductStrings()
						.getProductName()) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							monitor.beginTask(
									RpmText.LocalBuildHandler_buildForLocalArch,
									IProgressMonitor.UNKNOWN);
							IFpProjectBits projectBits = FedoraPackagerUtils
									.getVcsHandler(fedoraProjectRoot);
							BranchConfigInstance bci = projectBits
									.getBranchConfig();
							try {
								rpmBuild.buildType(BuildType.BINARY)
										.branchConfig(bci).call(monitor);
								fedoraProjectRoot.getProject().refreshLocal(
										IResource.DEPTH_INFINITE, monitor);
							} catch (CommandMisconfiguredException e) {
								// This shouldn't happen, but report error
								// anyway
								logger.logError(e.getMessage(), e);
								return FedoraHandlerUtils.errorStatus(
										RPMPlugin.PLUGIN_ID, e.getMessage(), e);
							} catch (CommandListenerException e) {
								// There are no command listeners registered, so
								// shouldn't
								// happen. Do something reasonable anyway.
								logger.logError(e.getMessage(), e);
								return FedoraHandlerUtils.errorStatus(
										RPMPlugin.PLUGIN_ID, e.getMessage(), e);
							} catch (RpmBuildCommandException e) {
								logger.logError(e.getMessage(), e.getCause());
								return FedoraHandlerUtils.errorStatus(
										RPMPlugin.PLUGIN_ID, e.getMessage(),
										e.getCause());
							} catch (IllegalArgumentException e) {
								// setting distDefines failed
								logger.logError(e.getMessage(), e);
								return FedoraHandlerUtils.errorStatus(
										RPMPlugin.PLUGIN_ID, e.getMessage(), e);
							} catch (CoreException e) {
								// should not occur
								logger.logError(e.getMessage(), e.getCause());
								return FedoraHandlerUtils.errorStatus(
										RPMPlugin.PLUGIN_ID, e.getMessage(),
										e.getCause());
							} catch (OperationCanceledException e) {
								FedoraHandlerUtils
										.showErrorDialog(
												shell,
												RpmText.LocalBuildHandler_buildCanceled,
												RpmText.LocalBuildHandler_buildCancelationResponse);
							}
						} finally {
							monitor.done();
						}
						return Status.OK_STATUS;
					}
				};
				rpmBuildjob.setUser(true);
				rpmBuildjob.schedule();
				try {
					// wait for job to finish
					rpmBuildjob.join();
				} catch (InterruptedException e1) {
					throw new OperationCanceledException();
				}
				return rpmBuildjob.getResult();
			}

		};
		// Suppress UI progress reporting. This is done by sub-jobs within.
		job.setSystem(true);
		job.schedule();
		return null;
	}

}
