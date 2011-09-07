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
package org.fedoraproject.eclipse.packager.rpm.internal.handlers;

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
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.FileDialogRunable;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.rpm.RPMPlugin;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.MockBuildCommand;
import org.fedoraproject.eclipse.packager.rpm.api.MockBuildJob;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler for building locally using mock.
 * 
 */
public class MockBuildHandler extends FedoraPackagerAbstractHandler {
	protected Shell shell;
	protected IProjectRoot fedoraProjectRoot;
	protected MockBuildCommand mockBuild;

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		shell = getShell(event);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		IResource eventResource = FedoraHandlerUtils.getResource(event);
		try {
			fedoraProjectRoot = FedoraPackagerUtils
					.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e) {
			logger.logError(FedoraPackagerText.invalidFedoraProjectRootError, e);
			FedoraHandlerUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
					FedoraPackagerText.invalidFedoraProjectRootError);
			return null;
		}
		IPath srpmPath = null;
		if (eventResource instanceof IFile
				&& eventResource.getName().endsWith(".src.rpm")) { //$NON-NLS-1$
			srpmPath = eventResource.getLocation();
		}
		if (srpmPath == null) {
			try {
				srpmPath = FedoraHandlerUtils.chooseRootFileOfType(shell,
						fedoraProjectRoot, ".src.rpm", //$NON-NLS-1$
						RpmText.MockBuildHandler_RootListMessage);
			} catch (OperationCanceledException e) {
				return null;
			} catch (CoreException e) {
				logger.logError(e.getMessage(), e);
				return FedoraHandlerUtils.errorStatus(RPMPlugin.PLUGIN_ID,
						e.getMessage(), e);
			}
		}
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
		Job job = new MockBuildJob(fedoraProjectRoot.getProductStrings()
				.getProductName(), shell, fedoraProjectRoot, srpmPath);
		job.setSystem(true); // Suppress UI. That's done in sub-jobs within.
		job.schedule();
		return null;
	}
}
