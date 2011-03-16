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
package org.fedoraproject.eclipse.packager.internal.handlers;

import java.net.MalformedURLException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.NonTranslatableStrings;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.ChecksumValidListener;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.DownloadFailedException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidCheckSumException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.api.errors.SourcesUpToDateException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler responsible for downloading sources as listed in sources files.
 */
public class DownloadHandler extends FedoraPackagerAbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Shell shell = getShell(event);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		final FedoraProjectRoot fedoraProjectRoot;
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(event);
			fedoraProjectRoot = FedoraPackagerUtils
					.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e) {
			logger.logError(NLS.bind(
					FedoraPackagerText.invalidFedoraProjectRootError,
					NonTranslatableStrings.getDistributionName()), e);
			FedoraHandlerUtils.showError(shell, NonTranslatableStrings
					.getProductName(), NLS.bind(
					FedoraPackagerText.invalidFedoraProjectRootError,
					NonTranslatableStrings.getDistributionName()),
					PackagerPlugin.PLUGIN_ID, e);
			return null;
		}
		final FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
		Job job = new Job(NonTranslatableStrings.getProductName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(
						FedoraPackagerText.DownloadHandler_downloadSourceTask,
						fedoraProjectRoot.getSourcesFile().getMissingSources()
								.size());
				DownloadSourceCommand download = fp.downloadSources();
				ChecksumValidListener md5sumListener = new ChecksumValidListener(fedoraProjectRoot);
				download.addCommandListener(md5sumListener); // want md5sum checking
				try {
					String downloadUrl = PackagerPlugin
							.getStringPreference(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL);
					if (downloadUrl != null) {
						// Only set URL explicitly if set in preferences. Lookaside
						// cache falls back to the default URL if not set.
						download.setDownloadURL(downloadUrl);
					}
					logger.logInfo(NLS.bind(FedoraPackagerText.callingCommand,
							DownloadSourceCommand.class.getName()));
					download.call(monitor);
				} catch (final SourcesUpToDateException e) {
					logger.logInfo(e.getMessage(), e);
					return FedoraHandlerUtils.showInformation(shell,
							NonTranslatableStrings.getProductName(),
							e.getMessage());
				} catch (DownloadFailedException e) {
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.showError(shell,
							NonTranslatableStrings.getProductName(),
							e.getMessage(), PackagerPlugin.PLUGIN_ID, e);
				} catch (CommandMisconfiguredException e) {
					// This shouldn't happen, but report error anyway
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.showError(shell,
							NonTranslatableStrings.getProductName(),
							e.getMessage(), PackagerPlugin.PLUGIN_ID, e);
				} catch (CommandListenerException e) {
					if (e.getCause() instanceof InvalidCheckSumException) {
						String message = e.getCause().getMessage();
						logger.logError(message, e.getCause());
						return FedoraHandlerUtils
								.showError(
										shell,
										NonTranslatableStrings.getProductName(),
										message, PackagerPlugin.PLUGIN_ID,
										e.getCause());
					}
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.showError(shell,
							NonTranslatableStrings.getProductName(),
							e.getMessage(), PackagerPlugin.PLUGIN_ID, e);
				} catch (MalformedURLException e) {
					// setDownloadUrl failed
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.showError(shell,
							NonTranslatableStrings.getProductName(), e.getMessage(),
							PackagerPlugin.PLUGIN_ID, e);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return null; // must be null
	}
}
