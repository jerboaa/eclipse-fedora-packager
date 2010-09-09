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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.Messages;

/**
 * Class responsible for downloading source files
 * 
 * @author Red Hat inc.
 * 
 */
public class DownloadHandler extends WGetHandler {

	/**
	 * Retrieves sources. This method is useful because if you try to access
	 * ExecutionEvent from outside the even loop one will get
	 * InvalidThreadAccess error.
	 * 
	 * @param fedoraProjectRoot
	 * @param monitor
	 * @return The status.
	 */
	public IStatus doExecute(FedoraProjectRoot fedoraProjectRoot,
			IProgressMonitor monitor) {
		return retrieveSources(fedoraProjectRoot, monitor);
	}

	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final FedoraProjectRoot fedoraProjectRoot = FedoraHandlerUtils
				.getValidRoot(e);
		Job job = new Job(Messages.downloadHandler_jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.downloadHandler_downloadSourceTask,
						IProgressMonitor.UNKNOWN);
				IStatus status = retrieveSources(fedoraProjectRoot, monitor);
				monitor.done();
				return status;
			}
		};
		job.setUser(true);
		job.schedule();
		// Set job variable in order to be able to wait for it to finish.
		// See CommonHandler#waitForJob() and RPMHandler#makeSRPM() for an
		// example.
		setJob(job);
		return null;
	}

}
