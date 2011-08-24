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
package org.fedoraproject.eclipse.packager.rpm.internal.handlers.local;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
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
 * This is the modified version of org.fedoraproject.eclipse.packager.rpm.internal.handlers.SRPMBuildHandler.java
 * to make it work with Local Fedora Packager Project, since in the local version
 * downloading source from lookaside cache is not applicable
 */
public class SRPMBuildHandler extends LocalHandlerDispatcher {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Perhaps need to dispatch to non-local handler
		if (checkDispatch(event, new org.fedoraproject.eclipse.packager.rpm.internal.handlers.SRPMBuildHandler())) {
			// dispatched, so return
			return null;
		}
		final Shell shell = getShell(event);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		final IProjectRoot localFedoraProjectRoot;

		IResource eventResource = FedoraHandlerUtils.getResource(event);
		try {
			localFedoraProjectRoot = FedoraPackagerUtils.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e) {
			logger.logError(FedoraPackagerText.invalidLocalFedoraProjectRootError, e);
			FedoraHandlerUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
					FedoraPackagerText.invalidLocalFedoraProjectRootError);
			return null;
		}
		FedoraPackager fp = new FedoraPackager(localFedoraProjectRoot);
		final RpmBuildCommand srpmBuild;
		try {
			// get RPM build command in order to produce an SRPM
			srpmBuild = (RpmBuildCommand) fp
					.getCommandInstance(RpmBuildCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					localFedoraProjectRoot.getProductStrings().getProductName(), e.getMessage());
			return null;
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					localFedoraProjectRoot.getProductStrings().getProductName(), e.getMessage());
			return null;
		}

		// Need to nest jobs into this job for it to show up properly in the
		// UI
		Job job = new Job(localFedoraProjectRoot.getProductStrings().getProductName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// Kick off the SRPM job
				SRPMBuildJob srpmBuildJob = new SRPMBuildJob(
						RpmText.SRPMBuildHandler_buildingSRPM, srpmBuild,
						localFedoraProjectRoot);
				srpmBuildJob.setUser(true);
				srpmBuildJob.schedule();
				try {
					// wait for job to finish
					srpmBuildJob.join();
				} catch (InterruptedException e1) {
					throw new OperationCanceledException();
				}
				return srpmBuildJob.getResult();
			}

		};
		job.setSystem(true); // avoid UI for this job
		job.schedule();
		return null;
	}

}
