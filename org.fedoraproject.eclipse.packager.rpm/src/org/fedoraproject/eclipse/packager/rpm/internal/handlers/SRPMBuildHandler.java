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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler for the fedpkg srpm command.
 *
 */
public class SRPMBuildHandler extends RpmBuildHandler {

	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final FedoraProjectRoot fedoraProjectRoot;
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(e);
			fedoraProjectRoot = FedoraPackagerUtils
					.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e1) {
			// TODO Handle this appropriately
			e1.printStackTrace();
			return null;
		}
		specfile = fedoraProjectRoot.getSpecFile();
		Job job = new Job(RpmText.CreateSRPMHandler_jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(RpmText.CreateSRPMHandler_buildSrpm,
						IProgressMonitor.UNKNOWN);
				return makeSRPM(fedoraProjectRoot, monitor);
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

}
