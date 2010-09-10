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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.cvs.Messages;
import org.fedoraproject.eclipse.packager.handlers.CommonHandler;
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;

/**
 * Handler which performs VCS tagging.
 *
 */
public class TagHandler extends CommonHandler {

	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final FedoraProjectRoot fedoraProjectRoot = FedoraHandlerUtils.getValidRoot(e);
		Job job = new Job(Messages.tagHandler_jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.tagHandler_tagTaskName, 
						IProgressMonitor.UNKNOWN);
				// Do VCS tagging
				IFpProjectBits projectBits = FedoraHandlerUtils.getVcsHandler(fedoraProjectRoot);
				IStatus result = projectBits.tagVcs(fedoraProjectRoot, monitor);
				monitor.done();
				return result;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

}
