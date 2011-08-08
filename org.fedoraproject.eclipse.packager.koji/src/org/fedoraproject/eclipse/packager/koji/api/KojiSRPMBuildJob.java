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
package org.fedoraproject.eclipse.packager.koji.api;

import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.api.errors.TagSourcesException;
import org.fedoraproject.eclipse.packager.api.errors.UnpushedChangesException;
import org.fedoraproject.eclipse.packager.koji.KojiPlugin;
import org.fedoraproject.eclipse.packager.koji.KojiText;
import org.fedoraproject.eclipse.packager.koji.api.errors.BuildAlreadyExistsException;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientException;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientLoginException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
import org.fedoraproject.eclipse.packager.utils.RPMUtils;

/**
 * Job that uploads an SRPM to Koji and has Koji build an RPM from the SRPM.
 *
 */
public class KojiSRPMBuildJob extends KojiBuildJob {

	private Shell shell;
	private IProjectRoot fedoraProjectRoot;
	private IPath srpmPath;

	/**
	 * @param name The name of the job.
	 * @param shell The shell the job runs in.
	 * @param fedoraProjectRoot The root of the project containing the SRPM being used.
	 * @param srpmPath 
	 */
	public KojiSRPMBuildJob(String name, Shell shell, IProjectRoot fedoraProjectRoot, IPath srpmPath) {
		super(name, shell, fedoraProjectRoot, true);
		this.shell = shell;
		this.fedoraProjectRoot = fedoraProjectRoot;
		this.srpmPath = srpmPath;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(NLS.bind(
				KojiText.KojiBuildHandler_pushBuildToKoji,
				fedoraProjectRoot.getProductStrings().getBuildToolName()), 100);
		monitor.worked(5);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
		final IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(fedoraProjectRoot);
		final KojiBuildCommand kojiBuildCmd;
		try {
			// Get KojiBuildCommand from Fedora packager registry
			kojiBuildCmd = (KojiBuildCommand) fp
			.getCommandInstance(KojiBuildCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID, e.getMessage(), e);
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID, e.getMessage(), e);
		}
		
		// build upload path
		final String uploadPath = "cli-build/" + FedoraPackagerUtils.getUniqueIdentifier();  //$NON-NLS-1$
		Job uploadJob = new KojiUploadSRPMJob(
				fedoraProjectRoot.getProductStrings().getProductName(), shell, 
				fedoraProjectRoot, srpmPath.toOSString(), uploadPath);
		uploadJob.setUser(true);
		uploadJob.schedule();
		try {
			// wait for SRPM upload to finish
			uploadJob.join();
		} catch (InterruptedException e1) {
			throw new OperationCanceledException();
		}
		if (!uploadJob.getResult().isOK()) {
			// bail if something failed
			return uploadJob.getResult();
		}
		monitor.worked(5);
		IKojiHubClient kojiClient;
		try {
			kojiClient = getHubClient();
		} catch (MalformedURLException e) {
			logger.logError(NLS.bind(
					KojiText.KojiBuildHandler_invalidHubUrl,
					fedoraProjectRoot.getProductStrings().getBuildToolName()), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
					NLS.bind(KojiText.KojiBuildHandler_invalidHubUrl,
							fedoraProjectRoot.getProductStrings().getBuildToolName()),
							e);
		}
		kojiBuildCmd.setKojiClient(kojiClient);
		kojiBuildCmd.sourceLocation(uploadPath + "/" + srpmPath.lastSegment()); //$NON-NLS-1$
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
				.isScratchBuild(true);
		logger.logDebug(NLS.bind(FedoraPackagerText.callingCommand,
				KojiBuildCommand.class.getName()));
		try {
			// Call build command.
			// Make sure to set the buildResult variable, since it is used
			// by getBuildResult() which is in turn called from the handler
			buildResult = kojiBuildCmd.call(monitor);
		} catch (CommandMisconfiguredException e) {
			// This shouldn't happen, but report error anyway
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (BuildAlreadyExistsException e) {
			logger.logDebug(e.getMessage(), e);
			FedoraHandlerUtils.showInformationDialog(shell,
					fedoraProjectRoot.getProductStrings().getProductName(),
					e.getMessage());
			return Status.OK_STATUS;
		} catch (UnpushedChangesException e) {
			logger.logDebug(e.getMessage(), e);
			FedoraHandlerUtils.showInformationDialog(shell,
					fedoraProjectRoot.getProductStrings().getProductName(),
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
						fedoraProjectRoot.getProductStrings().getDistributionName());
				logger.logError(msg, e);
				return FedoraHandlerUtils.errorStatus(
						KojiPlugin.PLUGIN_ID, msg, e);
			}
			if (e.isCertificateExpired()) {
				String msg = NLS
				.bind(KojiText.KojiBuildHandler_certificateExpriredMsg,
						fedoraProjectRoot.getProductStrings().getDistributionName());
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
}
