/*******************************************************************************
 * Copyright (c) 2010-2011 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager.rpm.internal.handlers.local;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.FileDialogRunable;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.rpm.RPMPlugin;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.MockBuildJob;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler for building an SRPM inside mock for a local Fedora RPM project.
 * This is the modified version of org.fedoraproject.eclipse.packager.rpm.internal.handlers.MockBuildHandler.java
 * 
 * A few things (most importantly the project root and dispatching) are different.
 */
public class LocalMockBuildHandler extends LocalHandlerDispatcher {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Perhaps need to dispatch to non-local handler
		if(checkDispatch(event, new org.fedoraproject.eclipse.packager.rpm.internal.handlers.MockBuildHandler())) {
			// dispatched, so return
			return null;
		}
		final Shell shell = getShell(event);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		final IProjectRoot localFedoraProjectRoot;
		IResource eventResource = null;
		try {
			eventResource = FedoraHandlerUtils.getResource(event);
			localFedoraProjectRoot = FedoraPackagerUtils
					.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e) {
			logger.logError(FedoraPackagerText.invalidFedoraProjectRootError, e);
			FedoraHandlerUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
					FedoraPackagerText.invalidFedoraProjectRootError);
			return null;
		}
		// If SRPM already selected, don't prompt
		IPath srpmPath = null;
		if (eventResource instanceof IFile
				&& eventResource.getName().endsWith(".src.rpm")) { //$NON-NLS-1$
			srpmPath = eventResource.getLocation();
		}
		// Let user choose from the SRPMs available in the project root
		if (srpmPath == null) {
			try {
				srpmPath = FedoraHandlerUtils.chooseRootFileOfType(shell,
						localFedoraProjectRoot, ".src.rpm", //$NON-NLS-1$
						RpmText.MockBuildHandler_RootListMessage);
			} catch (OperationCanceledException e) {
				return null;
			} catch (CoreException e) {
				logger.logError(e.getMessage(), e);
				return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
						e.getMessage(), e);
			}
		}
		// As a last resort, let the user pick any SRPM on their system
		if (srpmPath == null) {
			FileDialogRunable fdr = new FileDialogRunable("*.src.rpm", //$NON-NLS-1$
					RpmText.MockBuildHandler_FileSystemDialogTitle);
			shell.getDisplay().syncExec(fdr);
			String srpm = fdr.getFile();
			if (srpm == null) {
				return Status.CANCEL_STATUS;
			}
			srpmPath = new Path(srpm);
		}
		Job job = new MockBuildJob(localFedoraProjectRoot.getProductStrings()
				.getProductName(), shell, localFedoraProjectRoot, srpmPath,
				FedoraPackagerUtils.getVcsHandler(localFedoraProjectRoot)
						.getBranchConfig());
		job.setSystem(true); // Suppress UI. That's done in sub-jobs within.
		job.schedule();
		return null;
	}

}
