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
package org.fedoraproject.eclipse.packager.koji.internal.handlers;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.koji.KojiMessageDialog;
import org.fedoraproject.eclipse.packager.koji.KojiPlugin;
import org.fedoraproject.eclipse.packager.koji.KojiText;
import org.fedoraproject.eclipse.packager.koji.KojiUrlUtils;
import org.fedoraproject.eclipse.packager.koji.api.BuildResult;
import org.fedoraproject.eclipse.packager.koji.api.KojiBuildJob;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;

/**
 * Handler to kick off a remote Koji build.
 * 
 */
public class KojiBuildHandler extends FedoraPackagerAbstractHandler {
	
	/**
	 * Shell for message dialogs, etc.
	 */
	protected Shell shell;
	protected URL kojiWebUrl;
	protected IProjectRoot fedoraProjectRoot;
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		this.shell = getShell(event);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		try {
			IResource eventResource = FedoraHandlerUtils.getResource(event);
			fedoraProjectRoot = FedoraPackagerUtils
					.getProjectRoot(eventResource);
		} catch (InvalidProjectRootException e) {
			logger.logError(FedoraPackagerText.invalidFedoraProjectRootError, e);
			FedoraHandlerUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
					FedoraPackagerText.invalidFedoraProjectRootError);
			return null;
		}
		Job job = new KojiBuildJob(fedoraProjectRoot.getProductStrings().getProductName(), 
				getShell(event), fedoraProjectRoot, isScratchBuild());
		job.addJobChangeListener(getJobChangeListener());
		job.setUser(true);
		job.schedule();
		return null; // must be null
	}
	
	protected boolean isScratchBuild() {
		return false;
	}
	
	/**
	 * Create a job listener for the event {@code done}.
	 *  
	 * @return The job change listener.
	 */
	protected IJobChangeListener getJobChangeListener() {
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		String webUrl = PackagerPlugin.getStringPreference(FedoraPackagerPreferencesConstants.PREF_KOJI_WEB_URL);
		if (webUrl == null) {
			// use default
			webUrl = FedoraPackagerPreferencesConstants.DEFAULT_KOJI_WEB_URL; 
		}
		try {
			kojiWebUrl = new URL(webUrl);
		} catch (MalformedURLException e) {
			// nothing critical, use default koji URL instead and log the bogus
			// Web url set in preferences.
			logger.logError(NLS.bind(
					KojiText.KojiBuildHandler_invalidKojiWebUrl,
					fedoraProjectRoot.getProductStrings().getBuildToolName(), webUrl), e);
			try {
				kojiWebUrl = new URL(FedoraPackagerPreferencesConstants.DEFAULT_KOJI_WEB_URL);
			} catch (MalformedURLException ignored) {};
		}
		IJobChangeListener listener = new JobChangeAdapter() {
			
			// We are only interested in the done event
			@Override
			public void done(IJobChangeEvent event) {
				// get the BuildResult from the underlying job
				KojiBuildJob job = (KojiBuildJob)event.getJob();
				final BuildResult buildResult = job.getBuildResult();
				final IStatus jobStatus = event.getResult();
				PlatformUI.getWorkbench().getDisplay().asyncExec(
						new Runnable() {
							@Override
							public void run() {
								// Only show response message dialog on success
								if (jobStatus.isOK() && buildResult != null && buildResult.wasSuccessful()) {
									FedoraPackagerLogger logger = FedoraPackagerLogger
											.getInstance();
									// unconditionally log so that users get a second chance to see the
									// koji-web URL
									logger.logInfo(NLS
											.bind(KojiText.KojiMessageDialog_buildResponseMsg,
													fedoraProjectRoot.getProductStrings().getBuildToolName())
											+ " " //$NON-NLS-1$
											+ KojiUrlUtils.constructTaskUrl(
													buildResult.getTaskId(),
													kojiWebUrl));
									// open dialog
									getKojiMsgDialog(buildResult.getTaskId(),
											kojiWebUrl).open();
								}
							}
						});
			}
		};
		return listener;
	}
	
	/**
	 * Create KojiMessageDialog based on taskId and kojiWebUrl.
	 * 
	 * @param taskId
	 *            The task ID to use for the message.
	 * @param kojiWebUrl
	 *            The url to Koji Web without any parameters.
	 * @return A new KojiMessageDialog for the given Web Url and task Id.
	 */
	public KojiMessageDialog getKojiMsgDialog(int taskId, URL kojiWebUrl) {
		ImageDescriptor descriptor = KojiPlugin
				.getImageDescriptor("icons/Artwork_DesignService_koji-icon-16.png"); //$NON-NLS-1$
		Image titleImage = descriptor.createImage();
		Image msgContentImage = KojiPlugin.getImageDescriptor("icons/koji.png") //$NON-NLS-1$
				.createImage();
		KojiMessageDialog msgDialog = new KojiMessageDialog(shell, NLS.bind(
				KojiText.KojiBuildHandler_kojiBuild,
				fedoraProjectRoot.getProductStrings().getBuildToolName()), titleImage,
				MessageDialog.NONE, new String[] { IDialogConstants.OK_LABEL },
				0, kojiWebUrl, taskId, NLS.bind(
						KojiText.KojiMessageDialog_buildResponseMsg,
						fedoraProjectRoot.getProductStrings().getBuildToolName()),
				msgContentImage,
				fedoraProjectRoot.getProductStrings().getBuildToolName());
		return msgDialog;
	}
}
