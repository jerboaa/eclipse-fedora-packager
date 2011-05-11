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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.OperationCanceledException;
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
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand;
import org.fedoraproject.eclipse.packager.rpm.api.SRPMBuildJob;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler for the creating an SRPM
 *
 */
public class SRPMBuildHandler extends FedoraPackagerAbstractHandler {

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
		final DownloadSourceCommand download;
		try {
			// need to get sources for an SRPM build
			download = (DownloadSourceCommand) fp
					.getCommandInstance(DownloadSourceCommand.ID);
			// get RPM build command in order to produce an SRPM
			srpmBuild = (RpmBuildCommand) fp
					.getCommandInstance(RpmBuildCommand.ID);
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
			return null;
		}
		// Kick off the SRPM job
		SRPMBuildJob srpmBuildJob = new SRPMBuildJob(
				NonTranslatableStrings.getProductName(), srpmBuild,
				fedoraProjectRoot);
		srpmBuildJob.setUser(true);
		srpmBuildJob.schedule();
		return null;
	}

}
