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
package org.fedoraproject.eclipse.packager.rpm;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.DownloadFailedException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidCheckSumException;
import org.fedoraproject.eclipse.packager.api.errors.SourcesUpToDateException;
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;

/**
 * Handler for building locally.
 *
 */
public class LocalBuildHandler extends RPMHandler {

	
	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final FedoraProjectRoot fedoraProjectRoot = FedoraHandlerUtils.getValidRoot(e);
		final FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
		specfile = fedoraProjectRoot.getSpecFile();
		Job job = new Job(Messages.localBuildHandler_jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.localBuildHandler_buildForLocalArch,
						IProgressMonitor.UNKNOWN);
				// First download sources
				DownloadSourceCommand downloadCmd = fp.downloadSources();
				
				try {
					downloadCmd.call(monitor);
				} catch (SourcesUpToDateException e1) {
					// TODO handle appropriately
				} catch (DownloadFailedException e1) {
					// TODO handle appropriately
				} catch (InvalidCheckSumException e1) {
					// TODO handle appropriately
				} catch (CommandMisconfiguredException e1) {
					// TODO handle appropriately
				}
				IStatus result;
				try {
					// search for noarch directive, otherwise use local arch
					final String arch = FedoraHandlerUtils.rpmQuery(
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

				} catch (CoreException e) {
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
