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
package org.fedoraproject.eclipse.packager.cvs;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.handlers.UploadHandler;
import org.fedoraproject.eclipse.packager.handlers.WGetHandler;

public class NewSourcesHandler extends WGetHandler {
	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor)
			throws ExecutionException {
		return Status.OK_STATUS;
	}

	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final IResource resource = getResource(e);
		final FedoraProjectRoot fedoraProjectRoot = getValidRoot(resource);
		final SourcesFile sourceFile = fedoraProjectRoot.getSourcesFile();
		specfile = fedoraProjectRoot.getSpecFile();
		job = new Job("Fedora Packager") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Examining resources", IProgressMonitor.UNKNOWN);
				// get the sources and .cvsignore files
				final File cvsignore = new File(fedoraProjectRoot.getContainer().getLocation().toString()
							+ Path.SEPARATOR + ".cvsignore");

				// don't add empty files
				final File toAdd = resource.getLocation().toFile();
				if (toAdd.length() == 0) {
					return handleOK(resource.getName() + " is empty", true);
				}

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				final String filename = resource.getName();
				// use our Fedora client certificate to start SSL connection
				IStatus result = performUpload(toAdd, filename, monitor, fedoraProjectRoot);

				if (result.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					monitor.subTask("Updating 'sources' and '.cvsignore'");
					result = updateFiles(sourceFile, cvsignore, toAdd, filename,
							monitor);
				}
				monitor.done();
				return result;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	@Override
	protected String getTaskName() {
		// TODO Auto-generated method stub
		return null;
	}
}
