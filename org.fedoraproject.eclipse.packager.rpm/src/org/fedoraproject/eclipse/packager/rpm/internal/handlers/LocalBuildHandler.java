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

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.NonTranslatableStrings;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.DownloadFailedException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.api.errors.SourcesUpToDateException;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.utils.RPMUtils;

/**
 * Handler for building locally.
 *
 */
public class LocalBuildHandler extends RpmBuildHandler {

	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Shell shell = getShell(event);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		final FedoraProjectRoot fedoraProjectRoot;
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(event);
			fedoraProjectRoot = FedoraPackagerUtils.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e2) {
			// TODO Handle this appropriately
			e2.printStackTrace();
			return null;
		}
		FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
		final DownloadSourceCommand downloadCmd;
		try {
			// Get DownloadSourceCommand from Fedora packager registry
			downloadCmd = (DownloadSourceCommand) fp
					.getCommandInstance(DownloadSourceCommand.ID);
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
		specfile = fedoraProjectRoot.getSpecFile();
		Job job = new Job(RpmText.LocalBuildHandler_jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(RpmText.LocalBuildHandler_buildForLocalArch,
						IProgressMonitor.UNKNOWN);
				// First download sources				
				try {
					downloadCmd.call(monitor);
				} catch (SourcesUpToDateException e1) {
					// TODO handle appropriately
				} catch (DownloadFailedException e1) {
					// TODO handle appropriately
				} catch (CommandListenerException e1) {
					// TODO handle appropriately
				} catch (CommandMisconfiguredException e1) {
					// TODO handle appropriately
				}
				IStatus result;
				try {
					// search for noarch directive, otherwise use local arch
					final String arch = RPMUtils.rpmQuery(
							fedoraProjectRoot, "ARCH"); //$NON-NLS-1$

					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					// perform rpmbuild
					ArrayList<String> flags = new ArrayList<String>();
					flags.add("--target"); //$NON-NLS-1$
					flags.add(arch);
					flags.add("-ba"); //$NON-NLS-1$
					result = rpmBuild(fedoraProjectRoot, flags, monitor);

				} catch (IOException e) {
					e.printStackTrace();
					result = FedoraHandlerUtils.handleError(e);
				}
				monitor.done();
				return result;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

}
