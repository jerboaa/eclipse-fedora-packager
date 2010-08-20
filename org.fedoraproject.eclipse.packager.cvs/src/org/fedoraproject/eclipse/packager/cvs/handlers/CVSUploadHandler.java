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
package org.fedoraproject.eclipse.packager.cvs.handlers;

import java.io.File;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.cvs.Messages;
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;

/**
 * Handles upload commands (CSV implementation)
 * 
 * @author Red Hat Inc.
 *
 */
public class CVSUploadHandler extends CVSHandler {
	
	@Override
	/**
	 *  Performs upload of sources (independent of VCS used), updates "sources"
	 *  file and performs necessary CVS operations to bring branch in sync.
	 */
	public Object execute(final ExecutionEvent e) throws ExecutionException {

		final IResource resource = FedoraHandlerUtils.getResource(e);
		final FedoraProjectRoot fedoraProjectRoot = FedoraHandlerUtils.getValidRoot(resource);
		final SourcesFile sourceFile = fedoraProjectRoot.getSourcesFile();

		// do tasks as job
		Job job = new Job(getTaskName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask(Messages.getString("CVSUploadHandler.taskName"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

				// ensure file has changed if already listed in sources
				Map<String, String> sources = sourceFile.getSources();
				String filename = resource.getName();
				if (sources.containsKey(filename)
						&& SourcesFile
								.checkMD5(sources.get(filename), resource)) {
					// file already in sources
					return handleOK(Messages
							.getString("CVSUploadHandler.versionExists") //$NON-NLS-1$
							, true);
				}

				// Don't do anything if file is empty
				final File toAdd = resource.getLocation().toFile();
				if (toAdd.length() == 0) {
					return handleOK(NLS.bind(org.fedoraproject.eclipse.packager.Messages
							.getString("UploadHandler.0"),
							toAdd.getName()), true); //$NON-NLS-1$
				}

				// Do the file uploading
				IStatus result = performUpload(toAdd, filename, monitor,
						fedoraProjectRoot);

				if (result.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}

				// Update sources file
				result = updateSources(sourceFile, toAdd);
				if (!result.isOK()) {
					// fail updating sources file
				}

				// Handle CVS specific stuff; Update .cvsignore
				final File cvsignore = new File(fedoraProjectRoot
						.getContainer().getLocation().toString()
						+ IPath.SEPARATOR + ".cvsignore"); //$NON-NLS-1$
				result = updateIgnoreFile(cvsignore, toAdd);
				if (!result.isOK()) {
					// fail updating sources file
				}

				// Do CVS update
				result = updateCVS(fedoraProjectRoot, cvsignore, monitor);
				if (result.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
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
