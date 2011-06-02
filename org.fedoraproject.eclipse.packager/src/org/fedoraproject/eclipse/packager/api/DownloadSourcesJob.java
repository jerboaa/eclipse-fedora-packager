package org.fedoraproject.eclipse.packager.api;

import java.net.MalformedURLException;

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
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.DownloadFailedException;
import org.fedoraproject.eclipse.packager.api.errors.InvalidCheckSumException;
import org.fedoraproject.eclipse.packager.api.errors.SourcesUpToDateException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;

/**
 * Job for downloading sources.
 */
public class DownloadSourcesJob extends Job {
	
	private DownloadSourceCommand download;
	private FedoraProjectRoot fedoraProjectRoot;
	private FedoraPackagerLogger logger;
	private Shell shell;
	private boolean suppressSourcesUpToDateInfo = false;
	
	/**
	 * @param jobName
	 * @param download
	 *            The download command to use
	 * @param fedoraProjectRoot
	 * @param shell
	 *            A valid shell.
	 */
	public DownloadSourcesJob(String jobName, DownloadSourceCommand download,
			FedoraProjectRoot fedoraProjectRoot, Shell shell) {
		super(jobName);
		this.download = download;
		this.fedoraProjectRoot = fedoraProjectRoot;
		this.logger = FedoraPackagerLogger.getInstance();
	}

	/**
	 * @param jobName
	 * @param download
	 *            The download command to use
	 * @param fedoraProjectRoot
	 * @param shell
	 *            A valid shell.
	 * @param suppressSourcesUpToDateInfo
	 *            Indicating if information message dialog reporting sources are
	 *            up-to-date should be suppressed.
	 */
	public DownloadSourcesJob(String jobName, DownloadSourceCommand download,
			FedoraProjectRoot fedoraProjectRoot, Shell shell,
			boolean suppressSourcesUpToDateInfo) {
		super(jobName);
		this.download = download;
		this.fedoraProjectRoot = fedoraProjectRoot;
		this.logger = FedoraPackagerLogger.getInstance();
		this.suppressSourcesUpToDateInfo = suppressSourcesUpToDateInfo;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(
				FedoraPackagerText.DownloadHandler_downloadSourceTask,
				fedoraProjectRoot.getSourcesFile().getMissingSources().size());
		ChecksumValidListener md5sumListener = new ChecksumValidListener(
				fedoraProjectRoot);
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
			if (!suppressSourcesUpToDateInfo) {
				FedoraHandlerUtils
						.showInformationDialog(shell,
								NonTranslatableStrings.getProductName(this.fedoraProjectRoot),
								e.getMessage());
			}
			return Status.OK_STATUS;
		} catch (DownloadFailedException e) {
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(PackagerPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (CommandMisconfiguredException e) {
			// This shouldn't happen, but report error anyway
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(PackagerPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (CommandListenerException e) {
			if (e.getCause() instanceof InvalidCheckSumException) {
				String message = e.getCause().getMessage();
				logger.logError(message, e.getCause());
				return FedoraHandlerUtils.errorStatus(PackagerPlugin.PLUGIN_ID,
						message, e.getCause());
			}
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(PackagerPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} catch (MalformedURLException e) {
			// setDownloadUrl failed
			logger.logError(e.getMessage(), e);
			return FedoraHandlerUtils.errorStatus(PackagerPlugin.PLUGIN_ID,
					e.getMessage(), e);
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

}
