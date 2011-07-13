package org.fedoraproject.eclipse.packager.koji.api;

import java.net.MalformedURLException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.koji.KojiPlugin;
import org.fedoraproject.eclipse.packager.koji.KojiText;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientException;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientLoginException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;

/**
 * Command that uploads an SRPM to Koji.
 *
 */
public class KojiUploadSRPMJob extends Job {

	private Shell shell;
	private IProjectRoot fedoraProjectRoot;
	private String srpmPath;
	private KojiUploadSRPMCommand uploadSRPMCommand;
	private String uploadPath;
	
	/**
	 * @param name The name of the Job.
	 * @param shell The shell the Job runs in.
	 * @param fedoraProjectRoot The root of the project containing the SRPM.
	 * @param srpmPath The path to the SRPM.
	 * @param uploadPath The path to upload to on the Koji server.
	 */
	public KojiUploadSRPMJob(String name, Shell shell, IProjectRoot fedoraProjectRoot, String srpmPath, String uploadPath) {
		super(name);
		this.shell = shell;
		this.fedoraProjectRoot = fedoraProjectRoot;
		this.srpmPath = srpmPath;
		this.uploadPath = uploadPath;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
		FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
		try{
		uploadSRPMCommand = (KojiUploadSRPMCommand) fp
			.getCommandInstance(KojiUploadSRPMCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					fedoraProjectRoot.getProductStrings().getProductName(), e.getMessage());
			return null;
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					fedoraProjectRoot.getProductStrings().getProductName(), e.getMessage());
			return null;
		}

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
		try {
			uploadSRPMCommand.setKojiClient(kojiClient).setRemotePath(uploadPath).setSRPM(srpmPath).call(monitor);		
		} catch (CommandMisconfiguredException e) {
			// This shouldn't happen, but report error anyway
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (KojiHubClientException e) {
			// return some generic error
			String msg = NLS.bind(KojiText.KojiBuildHandler_unknownBuildError, e.getMessage());
			logger.logError(msg, e);
			return FedoraHandlerUtils.errorStatus(
					KojiPlugin.PLUGIN_ID, msg, e);
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
		} catch (CommandListenerException e) {
			// This shouldn't happen, but report error anyway
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
					e.getMessage(), e);
		}
		// success
		return Status.OK_STATUS;
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
}
