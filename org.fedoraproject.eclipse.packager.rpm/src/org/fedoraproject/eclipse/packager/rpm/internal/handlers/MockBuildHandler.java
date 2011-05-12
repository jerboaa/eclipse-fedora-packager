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
package org.fedoraproject.eclipse.packager.rpm.internal.handlers;

import java.io.FileNotFoundException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.NonTranslatableStrings;
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
import org.fedoraproject.eclipse.packager.rpm.api.MockBuildCommand;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildResult;
import org.fedoraproject.eclipse.packager.rpm.api.SRPMBuildJob;
import org.fedoraproject.eclipse.packager.rpm.api.errors.MockBuildCommandException;
import org.fedoraproject.eclipse.packager.rpm.api.errors.MockNotInstalledException;
import org.fedoraproject.eclipse.packager.rpm.api.errors.UserNotInMockGroupException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler for building locally using mock.
 *
 */
public class MockBuildHandler extends FedoraPackagerAbstractHandler {
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Shell shell = getShell(event);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		final FedoraProjectRoot fedoraProjectRoot;
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(event);
			fedoraProjectRoot = FedoraPackagerUtils
					.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e) {
			logger.logError(NLS.bind(
					FedoraPackagerText.invalidFedoraProjectRootError,
					NonTranslatableStrings.getDistributionName()), e);
			FedoraHandlerUtils.showErrorDialog(shell, NonTranslatableStrings
					.getProductName(), NLS.bind(
					FedoraPackagerText.invalidFedoraProjectRootError,
					NonTranslatableStrings.getDistributionName()));
			return null;
		}
		FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
		final RpmBuildCommand srpmBuild;
		final MockBuildCommand mockBuild;
		final DownloadSourceCommand download;
		try {
			// need to get sources for an SRPM build
			download = (DownloadSourceCommand) fp
					.getCommandInstance(DownloadSourceCommand.ID);
			// get RPM build command in order to produce an SRPM
			srpmBuild = (RpmBuildCommand) fp
					.getCommandInstance(RpmBuildCommand.ID);
			// the mock build command we are going to use
			mockBuild = (MockBuildCommand) fp
					.getCommandInstance(MockBuildCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					NonTranslatableStrings.getProductName(), e.getMessage());
			return null;
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					NonTranslatableStrings.getProductName(), e.getMessage());
			return null;
		}
		Job job = new Job(NonTranslatableStrings.getProductName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// Make sure we have sources locally
				Job downloadSourcesJob = new DownloadSourcesJob(RpmText.MockBuildHandler_downloadSourcesForMockBuild,
						download, fedoraProjectRoot, shell, true);
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
				// Create a brand new SRPM
				SRPMBuildJob srpmBuildJob = new SRPMBuildJob(NLS.bind(
						RpmText.MockBuildHandler_creatingSRPMForMockBuild,
						fedoraProjectRoot.getPackageName()), srpmBuild,
						fedoraProjectRoot);
				srpmBuildJob.setUser(true);
				srpmBuildJob.schedule();
				try {
					// wait for SRPM build to finish
					srpmBuildJob.join();
				} catch (InterruptedException e1) {
					throw new OperationCanceledException();
				}
				if (!srpmBuildJob.getResult().isOK()) {
					// bail if something failed
					return srpmBuildJob.getResult();
				}
				
				final RpmBuildResult srpmBuildResult = srpmBuildJob.getSRPMBuildResult(); 
				// do the mock building
				Job mockBuildJob = new Job(NonTranslatableStrings.getProductName()) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							monitor.beginTask(
									RpmText.MockBuildHandler_testLocalBuildWithMock,
									IProgressMonitor.UNKNOWN);
							if (monitor.isCanceled()) {
								throw new OperationCanceledException();
							}

							// kick of the mock build
							try {
								mockBuild.pathToSRPM(srpmBuildResult
										.getAbsoluteSRPMFilePath());
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// catch error when creating the SRPM failed.
								logger.logError(
										RpmText.MockBuildHandler_srpmBuildFailed,
										e);
								return FedoraHandlerUtils
										.errorStatus(
												RPMPlugin.PLUGIN_ID,
												RpmText.MockBuildHandler_srpmBuildFailed,
												e);
							}
							logger.logInfo(NLS.bind(
									FedoraPackagerText.callingCommand,
									MockBuildCommand.class.getName()));
							try {
								mockBuild.call(monitor);
							} catch (CommandMisconfiguredException e) {
								// This shouldn't happen, but report error
								// anyway
								logger.logError(e.getMessage(), e);
								return FedoraHandlerUtils.errorStatus(
										RPMPlugin.PLUGIN_ID, e.getMessage(), e);
							} catch (UserNotInMockGroupException e) {
								// nothing critical, advise the user what to do.
								logger.logInfo(e.getMessage());
								FedoraHandlerUtils
										.showInformationDialog(shell,
												NonTranslatableStrings
														.getProductName(), e
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
								logger.logInfo(e.getMessage());
								FedoraHandlerUtils
										.showInformationDialog(shell,
												NonTranslatableStrings
														.getProductName(), e
														.getMessage());
								return Status.OK_STATUS;
							}
						} finally {
							monitor.done();
						}
						return Status.OK_STATUS;
					}
				};
				mockBuildJob.setUser(true);
				mockBuildJob.schedule();
				try {
					// wait for job to finish
					mockBuildJob.join();
				} catch (InterruptedException e1) {
					throw new OperationCanceledException();
				}
				return mockBuildJob.getResult();
			}
			
		};
		job.setSystem(true); // Suppress UI. That's done in sub-jobs within.
		job.schedule();
		return null;
	}

}
