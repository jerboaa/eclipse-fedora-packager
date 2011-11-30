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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.IPreferenceHandler;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.rpm.api.MockBuildCommand;
import org.fedoraproject.eclipse.packager.rpm.api.SCMMockBuildCommand.RepoType;
import org.fedoraproject.eclipse.packager.rpm.api.SCMMockBuildJob;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler for building locally using mock with SCM.
 * 
 */
public class SCMMockBuildHandler extends FedoraPackagerAbstractHandler implements IPreferenceHandler {

	protected Shell shell;
	protected MockBuildCommand mockBuild;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		shell = getShell(event);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(event);
			setProjectRoot(FedoraPackagerUtils
					.getProjectRoot(eventResource));
		} catch (InvalidProjectRootException e) {
			logger.logError(FedoraPackagerText.invalidFedoraProjectRootError, e);
			FedoraHandlerUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
					FedoraPackagerText.invalidFedoraProjectRootError);
			return null;
		}
		Job job = new SCMMockBuildJob(getProjectRoot().getProductStrings()
				.getProductName(), shell, getProjectRoot(), RepoType.GIT,
				FedoraPackagerUtils.getVcsHandler(getProjectRoot())
						.getBranchConfig(), getPreference());
		job.setSystem(true); // Suppress UI. That's done in sub-jobs within.
		job.schedule();
		return null;
	}

	@Override
	public String getPreference() {
		return PackagerPlugin
				.getStringPreference(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL);
	}

}
