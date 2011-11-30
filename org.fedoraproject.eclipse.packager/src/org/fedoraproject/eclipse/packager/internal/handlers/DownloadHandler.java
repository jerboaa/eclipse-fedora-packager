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
package org.fedoraproject.eclipse.packager.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.DownloadSourcesJob;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.IPreferenceHandler;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler responsible for downloading sources as listed in sources files.
 */
public class DownloadHandler extends FedoraPackagerAbstractHandler implements IPreferenceHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Shell shell = getShell(event);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(event);
			setProjectRoot(FedoraPackagerUtils.getProjectRoot(eventResource));
		} catch (InvalidProjectRootException e) {
			logger.logError(FedoraPackagerText.invalidFedoraProjectRootError, e);
			FedoraHandlerUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
					FedoraPackagerText.invalidFedoraProjectRootError);
			return null;
		}
		FedoraPackager fp = new FedoraPackager(getProjectRoot());
		final DownloadSourceCommand download;
		try {
			// Get DownloadSourceCommand from Fedora packager registry
			download = (DownloadSourceCommand) fp
					.getCommandInstance(DownloadSourceCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					getProjectRoot().getProductStrings().getProductName(), e.getMessage());
			return null;
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					getProjectRoot().getProductStrings().getProductName(), e.getMessage());
			return null;
		}
		final String downloadUrlPreference = getPreference();
		Job downloadJob = new DownloadSourcesJob(
				getProjectRoot().getProductStrings().getProductName(), download,
				getProjectRoot(), shell, downloadUrlPreference);
		downloadJob.setUser(true);
		downloadJob.schedule();
		return null; // must be null
	}

	@Override
	public String getPreference() {
		return PackagerPlugin
				.getStringPreference(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL);
	}
}
