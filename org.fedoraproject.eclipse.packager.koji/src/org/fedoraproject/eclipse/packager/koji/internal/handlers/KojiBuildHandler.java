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
package org.fedoraproject.eclipse.packager.koji.internal.handlers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.NonTranslatableStrings;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.RPMUtils;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
import org.fedoraproject.eclipse.packager.koji.KojiMessageDialog;
import org.fedoraproject.eclipse.packager.koji.KojiPlugin;
import org.fedoraproject.eclipse.packager.koji.KojiText;
import org.fedoraproject.eclipse.packager.koji.KojiUrlUtils;
import org.fedoraproject.eclipse.packager.koji.api.BuildResult;
import org.fedoraproject.eclipse.packager.koji.api.IKojiHubClient;
import org.fedoraproject.eclipse.packager.koji.api.KojiBuildCommand;
import org.fedoraproject.eclipse.packager.koji.api.KojiSSLHubClient;
import org.fedoraproject.eclipse.packager.koji.api.TagSourcesListener;
import org.fedoraproject.eclipse.packager.koji.api.UnpushedChangesListener;
import org.fedoraproject.eclipse.packager.koji.api.errors.BuildAlreadyExistsException;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientException;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientLoginException;
import org.fedoraproject.eclipse.packager.koji.api.errors.TagSourcesException;
import org.fedoraproject.eclipse.packager.koji.api.errors.UnpushedChangesException;

/**
 * Handler to kick off a remote Koji build.
 * 
 */
public class KojiBuildHandler extends FedoraPackagerAbstractHandler {
	
	/**
	 * Shell for message dialogs, etc.
	 */
	private Shell shell;
	private BuildResult buildResult;
	private URL kojiWebUrl;
	private IProjectRoot fedoraProjectRoot;
	
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
		FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
		final IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(fedoraProjectRoot);
		final KojiBuildCommand kojiBuildCmd;
		try {
			// Get KojiBuildCommand from Fedora packager registry
			kojiBuildCmd = (KojiBuildCommand) fp
					.getCommandInstance(KojiBuildCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					NonTranslatableStrings.getProductName(fedoraProjectRoot), e.getMessage());
			return null;
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					NonTranslatableStrings.getProductName(fedoraProjectRoot), e.getMessage());
			return null;
		}
		// Push the build
		Job job = new Job(NonTranslatableStrings.getProductName(fedoraProjectRoot)) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(NLS.bind(
						KojiText.KojiBuildHandler_pushBuildToKoji,
						NonTranslatableStrings
								.getBuildToolName(fedoraProjectRoot)), 100);
				monitor.worked(5);
				UnpushedChangesListener unpushedChangesListener = new UnpushedChangesListener(
						fedoraProjectRoot, monitor);
				// check for unpushed changes prior calling command
				kojiBuildCmd.addCommandListener(unpushedChangesListener);
				// tag sources if user wishes; TagSourcesListener takes care of this
				TagSourcesListener tagSources = new TagSourcesListener(fedoraProjectRoot, monitor, shell);
				kojiBuildCmd.addCommandListener(tagSources);
				IKojiHubClient kojiClient;
				try {
					kojiClient = getHubClient();
				} catch (MalformedURLException e) {
					logger.logError(NLS.bind(
							KojiText.KojiBuildHandler_invalidHubUrl,
							NonTranslatableStrings.getBuildToolName(fedoraProjectRoot)), e);
					return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
							NLS.bind(KojiText.KojiBuildHandler_invalidHubUrl,
									NonTranslatableStrings.getBuildToolName(fedoraProjectRoot)),
							e);
				}
				kojiBuildCmd.setKojiClient(kojiClient);
				kojiBuildCmd.scmUrl(projectBits
							.getScmUrlForKoji(fedoraProjectRoot));
				String nvr;
				try {
					nvr = RPMUtils.getNVR(fedoraProjectRoot);
				} catch (IOException e) {
					logger.logError(KojiText.KojiBuildHandler_errorGettingNVR,
							e);
					return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
							KojiText.KojiBuildHandler_errorGettingNVR, e);
				}
				kojiBuildCmd.buildTarget(projectBits.getTarget()).nvr(nvr)
						.isScratchBuild(isScratchBuild());
				logger.logInfo(NLS.bind(FedoraPackagerText.callingCommand,
						KojiBuildCommand.class.getName()));
				// Call build command
				try {
					buildResult = kojiBuildCmd.call(monitor);
				} catch (CommandMisconfiguredException e) {
					// This shouldn't happen, but report error anyway
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
							e.getMessage(), e);
				} catch (BuildAlreadyExistsException e) {
					logger.logInfo(e.getMessage(), e);
					FedoraHandlerUtils.showInformationDialog(shell,
							NonTranslatableStrings.getProductName(fedoraProjectRoot),
							e.getMessage());
					return Status.OK_STATUS;
				} catch (UnpushedChangesException e) {
					logger.logInfo(e.getMessage(), e);
					FedoraHandlerUtils.showInformationDialog(shell,
							NonTranslatableStrings.getProductName(fedoraProjectRoot),
							e.getMessage());
					return Status.OK_STATUS;
				} catch (TagSourcesException e) {
					// something failed while tagging sources
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
							e.getMessage(), e);
				} catch (CommandListenerException e) {
					// This shouldn't happen, but report error anyway
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
							e.getMessage(), e);
				} catch (KojiHubClientLoginException e) {
					e.printStackTrace();
					// Check if certs were missing
					if (e.isCertificateMissing()) {
						String msg = NLS
								.bind(KojiText.KojiBuildHandler_missingCertificatesMsg,
										NonTranslatableStrings
												.getDistributionName(fedoraProjectRoot));
						logger.logError(msg, e);
						return FedoraHandlerUtils.errorStatus(
								KojiPlugin.PLUGIN_ID, msg, e);
					}
					if (e.isCertificateExpired()) {
						String msg = NLS
						.bind(KojiText.KojiBuildHandler_certificateExpriredMsg,
								NonTranslatableStrings
										.getDistributionName(fedoraProjectRoot));
						logger.logError(msg, e);
						return FedoraHandlerUtils.errorStatus(
								KojiPlugin.PLUGIN_ID, msg, e);
					}
					// return some generic error
					logger.logError(e.getMessage(), e);
					return FedoraHandlerUtils.errorStatus(
							KojiPlugin.PLUGIN_ID, e.getMessage(), e);
				} catch (KojiHubClientException e) {
					// return some generic error
					String msg = NLS.bind(KojiText.KojiBuildHandler_unknownBuildError, e.getMessage());
					logger.logError(msg, e);
					return FedoraHandlerUtils.errorStatus(
							KojiPlugin.PLUGIN_ID, msg, e);
				}
				// success
				return Status.OK_STATUS;
			}
			
		};
		// Add listener to show response dialog
		job.addJobChangeListener(getJobChangeListener());
		job.setUser(true);
		job.schedule();
		return null; // must be null
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
				NonTranslatableStrings.getBuildToolName(fedoraProjectRoot)), titleImage,
				MessageDialog.NONE, new String[] { IDialogConstants.OK_LABEL },
				0, kojiWebUrl, taskId, NLS.bind(
						KojiText.KojiMessageDialog_buildResponseMsg,
						NonTranslatableStrings.getBuildToolName(fedoraProjectRoot)),
				msgContentImage,
				NonTranslatableStrings.getBuildToolName(fedoraProjectRoot));
		return msgDialog;
	}
	
	/**
	 * Since, this is the handler for a regular build always return false.
	 * 
	 * @return {@code false}
	 */
	protected boolean isScratchBuild() {
		return false;
	}
	
	/**
	 * Create a hub client based on set preferences 
	 * 
	 * @throws MalformedURLException If the koji hub URL preference was invalid.
	 * @return The koji client.
	 */
	protected IKojiHubClient getHubClient() throws MalformedURLException {
		String kojiHubUrl = PackagerPlugin.getStringPreference(FedoraPackagerPreferencesConstants.PREF_KOJI_HUB_URL);
		if (kojiHubUrl == null) {
			// Set to default
			kojiHubUrl = FedoraPackagerPreferencesConstants.DEFAULT_KOJI_HUB_URL;
		}
		return new KojiSSLHubClient(kojiHubUrl);
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
					NonTranslatableStrings.getBuildToolName(fedoraProjectRoot), webUrl), e);
			try {
				kojiWebUrl = new URL(FedoraPackagerPreferencesConstants.DEFAULT_KOJI_WEB_URL);
			} catch (MalformedURLException ignored) {};
		}
		IJobChangeListener listener = new JobChangeAdapter() {
			
			// We are only interested in the done event
			@Override
			public void done(IJobChangeEvent event) {
				final IStatus jobStatus = event.getResult();
				PlatformUI.getWorkbench().getDisplay().asyncExec(
						new Runnable() {
							@Override
							public void run() {
								// Only show response message dialog on success
								if (jobStatus.isOK() && buildResult != null && buildResult.wasSuccessful()) {
									FedoraPackagerLogger logger = FedoraPackagerLogger
											.getInstance();
									logger.logInfo(NLS
											.bind(KojiText.KojiMessageDialog_buildResponseMsg,
													NonTranslatableStrings
															.getBuildToolName(fedoraProjectRoot))
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
}
