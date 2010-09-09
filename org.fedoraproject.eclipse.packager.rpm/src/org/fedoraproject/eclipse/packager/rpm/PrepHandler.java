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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.handlers.DownloadHandler;
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;

/**
 * Handler for the fedpkg prep command.
 * 
 */
public class PrepHandler extends RPMHandler {

	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final FedoraProjectRoot fedoraProjectRoot = FedoraHandlerUtils
				.getValidRoot(e);
		specfile = fedoraProjectRoot.getSpecFile();
		Job job = new Job(Messages.prepHandler_jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.prepHandler_attemptApplyPatchMsg,
						IProgressMonitor.UNKNOWN);
				DownloadHandler dh = new DownloadHandler();
				IStatus result = null;
				dh.doExecute(fedoraProjectRoot, monitor);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				ArrayList<String> flags = new ArrayList<String>();
				flags.add("--nodeps"); //$NON-NLS-1$
				flags.add("-bp"); //$NON-NLS-1$
				result = rpmBuild(flags, monitor);

				return result;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

}
