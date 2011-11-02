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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.BranchConfigInstance;
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
	 * @param name
	 *            The name of the job.
	 * @param shell
	 *            The shell the job runs in.
	 * @param fedoraProjectRoot
	 *            The root of the project containing the SRPM being used.
	 * @param srpmPath
	 *            Path of the SRPM locally
	 */
	public KojiSRPMBuildJob(String name, Shell shell,
			IProjectRoot fedoraProjectRoot, IPath srpmPath) {
		super(name, shell, fedoraProjectRoot, true);
		this.shell = shell;
		this.fedoraProjectRoot = fedoraProjectRoot;
		this.srpmPath = srpmPath;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
		final IFpProjectBits projectBits = FedoraPackagerUtils
				.getVcsHandler(fedoraProjectRoot);
		BranchConfigInstance bci = projectBits.getBranchConfig();
		KojiBuildCommand kojiBuildCmd;
		KojiUploadSRPMCommand uploadSRPMCommand;
		subMonitor.setTaskName(KojiText.KojiSRPMBuildJob_ConfiguringClient);
		try {
			uploadSRPMCommand = (KojiUploadSRPMCommand) fp
					.getCommandInstance(KojiUploadSRPMCommand.ID);
			kojiBuildCmd = (KojiBuildCommand) fp
					.getCommandInstance(KojiBuildCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell, fedoraProjectRoot
					.getProductStrings().getProductName(), e.getMessage());
			return null;
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell, fedoraProjectRoot
					.getProductStrings().getProductName(), e.getMessage());
			return null;
		}
		IKojiHubClient kojiClient;
		try {
			kojiClient = getHubClient();
		} catch (MalformedURLException e) {
			logger.logError(NLS.bind(KojiText.KojiBuildHandler_invalidHubUrl,
					fedoraProjectRoot.getProductStrings().getBuildToolName()),
					e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID, NLS
					.bind(KojiText.KojiBuildHandler_invalidHubUrl,
							fedoraProjectRoot.getProductStrings()
									.getBuildToolName()), e);
		}
		subMonitor.worked(5);
		subMonitor.setTaskName(KojiText.KojiSRPMBuildJob_UploadingSRPM);
		final String uploadPath = "cli-build/" + FedoraPackagerUtils.getUniqueIdentifier(); //$NON-NLS-1$
		try {
			uploadSRPMCommand.setKojiClient(kojiClient)
					.setRemotePath(uploadPath).setSRPM(srpmPath.toOSString())
					.call(subMonitor.newChild(80));
		} catch (CommandMisconfiguredException e) {
			// This shouldn't happen, but report error anyway
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (KojiHubClientException e) {
			// return some generic error
			String msg = NLS.bind(KojiText.KojiBuildHandler_unknownBuildError,
					e.getMessage());
			logger.logError(msg, e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID, msg, e);
		} catch (KojiHubClientLoginException e) {
			e.printStackTrace();
			// Check if certs were missing
			if (e.isCertificateMissing()) {
				String msg = NLS.bind(
						KojiText.KojiBuildHandler_missingCertificatesMsg,
						fedoraProjectRoot.getProductStrings()
								.getDistributionName());
				logger.logError(msg, e);
				return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
						msg, e);
			}
			if (e.isCertificateExpired()) {
				String msg = NLS.bind(
						KojiText.KojiBuildHandler_certificateExpriredMsg,
						fedoraProjectRoot.getProductStrings()
								.getDistributionName());
				logger.logError(msg, e);
				return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
						msg, e);
			}
			if (e.isCertificateRevoked()) {
				String msg = NLS.bind(
						KojiText.KojiBuildHandler_certificateRevokedMsg,
						fedoraProjectRoot.getProductStrings()
								.getDistributionName());
				logger.logError(msg, e);
				return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
						msg, e);
			}
			// return some generic error
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (CommandListenerException e) {
			// This shouldn't happen, but report error anyway
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
					e.getMessage(), e);
		}
		subMonitor.worked(5);
		kojiBuildCmd.setKojiClient(kojiClient);
		kojiBuildCmd.sourceLocation(uploadPath + "/" + srpmPath.lastSegment()); //$NON-NLS-1$
		String nvr;
		try {
			nvr = RPMUtils.getNVR(fedoraProjectRoot, bci);
		} catch (IOException e) {
			logger.logError(KojiText.KojiBuildHandler_errorGettingNVR, e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
					KojiText.KojiBuildHandler_errorGettingNVR, e);
		}
		kojiBuildCmd.buildTarget(bci.getBuildTarget()).nvr(nvr)
				.isScratchBuild(true);
		logger.logDebug(NLS.bind(FedoraPackagerText.callingCommand,
				KojiBuildCommand.class.getName()));
		try {
			//Call build command
			buildResult = kojiBuildCmd.call(subMonitor.newChild(10));
		} catch (CommandMisconfiguredException e) {
			// This shouldn't happen, but report error anyway
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (BuildAlreadyExistsException e) {
			logger.logDebug(e.getMessage(), e);
			FedoraHandlerUtils.showInformationDialog(shell, fedoraProjectRoot
					.getProductStrings().getProductName(), e.getMessage());
			return Status.OK_STATUS;
		} catch (UnpushedChangesException e) {
			logger.logDebug(e.getMessage(), e);
			FedoraHandlerUtils.showInformationDialog(shell, fedoraProjectRoot
					.getProductStrings().getProductName(), e.getMessage());
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
				String msg = NLS.bind(
						KojiText.KojiBuildHandler_missingCertificatesMsg,
						fedoraProjectRoot.getProductStrings()
								.getDistributionName());
				logger.logError(msg, e);
				return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
						msg, e);
			}
			if (e.isCertificateExpired()) {
				String msg = NLS.bind(
						KojiText.KojiBuildHandler_certificateExpriredMsg,
						fedoraProjectRoot.getProductStrings()
								.getDistributionName());
				logger.logError(msg, e);
				return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
						msg, e);
			}
			// return some generic error
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (KojiHubClientException e) {
			// return some generic error
			String msg = NLS.bind(KojiText.KojiBuildHandler_unknownBuildError,
					e.getMessage());
			logger.logError(msg, e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID, msg, e);
		}
		// success
		return Status.OK_STATUS;
	}
}
