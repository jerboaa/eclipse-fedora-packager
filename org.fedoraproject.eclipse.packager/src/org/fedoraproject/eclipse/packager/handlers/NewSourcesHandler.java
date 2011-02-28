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
package org.fedoraproject.eclipse.packager.handlers;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Uploads new sources. Does not check if sources changed.
 * 
 * @author Red Hat Inc.
 *
 */
public class NewSourcesHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {

		final FedoraProjectRoot fedoraProjectRoot;
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(e);
			fedoraProjectRoot = FedoraPackagerUtils.getValidRoot(eventResource);
		} catch (InvalidProjectRootException e1) {
			// TODO Handle appropriately
			e1.printStackTrace();
			return null;
		}
		final IResource resource = FedoraHandlerUtils.getResource(e);
		final SourcesFile sourceFile = fedoraProjectRoot.getSourcesFile();
		final IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(fedoraProjectRoot);
		// do tasks as job
		Job job = new Job(FedoraPackagerText.get().newSourcesHandler_jobName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask(FedoraPackagerText.get().newSourcesHandler_taskName, IProgressMonitor.UNKNOWN);

				// Don't do anything if file is empty
				final File toAdd = resource.getLocation().toFile();
				if (!FedoraPackagerUtils.isValidUploadFile(toAdd)) {
					return FedoraHandlerUtils.handleOK(
							MessageFormat.format(FedoraPackagerText.get().newSourcesHandler_invalidFile,
													toAdd.getName()), true);
				}

				// Do the file uploading
				String filename = resource.getName();
				// TODO do the upload.
				IStatus result = null;
				if (result.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				} else { // bail out on error
					return FedoraHandlerUtils.handleError(result.getMessage());
				}

				// Update sources file (replace existing sources, add true as
				// last parameter)
				// TODO: replace sources file
				if (!result.isOK()) {
					// fail updating sources file
					return FedoraHandlerUtils.handleError(FedoraPackagerText.get().newSourcesHandler_failUpdateSourceFile);
				}

				// Handle VCS specific stuff; Update .cvsignore/.gitignore
				// TODO: update ignore file!
				if (!result.isOK()) {
					// fail updating sources file
					return FedoraHandlerUtils.handleError(FedoraPackagerText.get().newSourcesHandler_failVCSUpdate);
				}

				// Do VCS update
				result = projectBits.updateVCS(fedoraProjectRoot, monitor);
				if (result.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				} else {
					// fail updating VCS
					return FedoraHandlerUtils.handleError(FedoraPackagerText.get().newSourcesHandler_failVCSUpdate);
				}
				
				// Refresh project
				IProject project = fedoraProjectRoot.getProject();
				if (project != null) {
					try {
						project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					} catch (CoreException e) {
						return FedoraHandlerUtils.handleError(e);
					}
				}
				return result;
			}

		};
		job.setUser(true);
		job.schedule();
		return null;
	}

}
