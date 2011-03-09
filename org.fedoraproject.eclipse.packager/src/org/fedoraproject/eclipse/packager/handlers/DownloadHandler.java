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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.NonTranslatableStrings;
import org.fedoraproject.eclipse.packager.api.ChecksumValidListener;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.DownloadFailedException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidCheckSumException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.api.errors.SourcesUpToDateException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler for downloading sources as listed in sources files.
 * 
 */
public class DownloadHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final FedoraProjectRoot fedoraProjectRoot;
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(e);
			fedoraProjectRoot = FedoraPackagerUtils
					.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e1) {
			// TODO Show proper message
			e1.printStackTrace();
			return null;
		}
		final FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
		final Shell shell = getShell(e);
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
					// FIXME set download URL from preferences.
					download.call(monitor);
				} catch (final SourcesUpToDateException e) {
					PlatformUI.getWorkbench().getDisplay()
							.asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(shell,
											NonTranslatableStrings
													.getProductName(), e
													.getMessage());
								}
							});
					return Status.OK_STATUS;
				} catch (DownloadFailedException e) {
					return FedoraHandlerUtils.handleError(e);
				} catch (CommandMisconfiguredException e) {
					// This shouldn't happen, but report error anyway
					return FedoraHandlerUtils.handleError(e);
				} catch (CommandListenerException e) {
					if (e.getCause() instanceof InvalidCheckSumException) {
						return FedoraHandlerUtils.handleError(e.getCause().getMessage(), false);
					}
					return FedoraHandlerUtils.handleError(e);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}
	
	/**
	 * @param event
	 * @return the shell
	 * @throws ExecutionException
	 */
	private Shell getShell(ExecutionEvent event) throws ExecutionException {
		return HandlerUtil.getActiveShellChecked(event);
	}
}
