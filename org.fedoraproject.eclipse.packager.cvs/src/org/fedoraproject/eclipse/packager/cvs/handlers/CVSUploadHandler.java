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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.cvs.Messages;

/**
 * Handles upload commands (CSV implementation)
 * 
 * @author Red Hat Inc.
 *
 */
public class CVSUploadHandler extends CVSHandler {
	
	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor)
			throws ExecutionException {
		// do nothing. Why?
		return Status.OK_STATUS;
	}
	
	@Override
	/**
	 *  Performs upload of sources (independent of VCS used), updates "sources"
	 *  file and performs necessary CVS operations to bring branch in sync.
	 */
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		
		final IResource resource = getResource(e);
		final FedoraProjectRoot fedoraProjectRoot = getValidRoot(resource);
		final SourcesFile sourceFile = fedoraProjectRoot.getSourcesFile();
		
		// ensure file has changed if already listed in sources
		Map<String, String> sources = getSourcesFile().getSources();
		String filename = resource.getName();
		if (sources.containsKey(filename)
				&& SourcesFile.checkMD5(sources.get(filename), resource)) {
			// file already in sources
			return handleOK(Messages.getString("CVSUploadHandler.versionExists") //$NON-NLS-1$
					, true);
		}
		
		// Do the file uploading
		IStatus status = (IStatus)super.execute(e);
		
		// Do rest of work if uploading was Ok.
		if (status.isOK()) {
			
			//Update sources file
			final File toAdd = resource.getLocation().toFile();
			status = updateSources(sourceFile, toAdd);
			if (!status.isOK()) {
				// fail updating sources file
			}

			// Handle CVS specific stuff; Update .cvsignore
			final File cvsignore = new File(fedoraProjectRoot
					.getContainer().getLocation().toString()
					+ IPath.SEPARATOR + ".cvsignore"); //$NON-NLS-1$
			status = updateCVSIgnore(cvsignore, toAdd);
			if (!status.isOK()) {
				// fail updating sources file
			}
			
			// Do CVS update
			job = new Job(org.fedoraproject.eclipse.packager.Messages.getString("FedoraPackager.jobName")) { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor
							.beginTask(
									Messages.getString("CVSUploadHandler.doCvsOps"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

					final IStatus result = updateCVS(sourceFile, cvsignore, monitor);
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
		}
		return null;
	}
	
	@Override
	protected String getTaskName() {
		return Messages.getString("CVSUploadHandler.taskName"); //$NON-NLS-1$
	}

}
