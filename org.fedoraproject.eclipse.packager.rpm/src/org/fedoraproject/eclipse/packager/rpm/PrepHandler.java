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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.handlers.DownloadHandler;

public class PrepHandler extends RPMHandler {
	
	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final IResource resource = getResource(e);
		final FedoraProjectRoot fedoraProjectRoot = getValidRoot(resource);
		specfile = fedoraProjectRoot.getSpecFile();
		Job job = new Job("Fedora Packager") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.getString("PrepHandler.1"),
						IProgressMonitor.UNKNOWN);
				DownloadHandler dh = new DownloadHandler();
				IStatus result = null;
					// retrieve sources
					result = dh.doExecute(fedoraProjectRoot, monitor);
				if (result.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					ArrayList<String> flags = new ArrayList<String>();
					flags.add("--nodeps");
					flags.add("-bp");
					result = rpmBuild(flags, monitor); //$NON-NLS-1$
				}	
				
				return result;
				
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
		return Status.OK_STATUS; //TODO remove once every handler handles it's execute
	}

	@Override
	protected String getTaskName() {
		return Messages.getString("PrepHandler.1"); //$NON-NLS-1$
	}

}
