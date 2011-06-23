package org.fedoraproject.eclipse.packager.rpm.api;

import java.io.FileNotFoundException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
import org.fedoraproject.eclipse.packager.FedoraPackagerText;
import org.fedoraproject.eclipse.packager.IProjectRoot;
import org.fedoraproject.eclipse.packager.PackagerPlugin;
import org.fedoraproject.eclipse.packager.api.DownloadSourceCommand;
import org.fedoraproject.eclipse.packager.api.DownloadSourcesJob;
import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
import org.fedoraproject.eclipse.packager.rpm.RPMPlugin;
import org.fedoraproject.eclipse.packager.rpm.RpmText;
import org.fedoraproject.eclipse.packager.rpm.api.errors.MockBuildCommandException;
import org.fedoraproject.eclipse.packager.rpm.api.errors.MockNotInstalledException;
import org.fedoraproject.eclipse.packager.rpm.api.errors.UserNotInMockGroupException;
import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;

/**
 * Job for doing a standard Mock build.
 *
 */
public class MockBuildJob extends Job {

	private Shell shell;
	private final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
	private IProjectRoot fpr;
	private MockBuildResult result;
	private RpmBuildCommand srpmBuild;
	private MockBuildCommand mockBuild;
	private DownloadSourceCommand download;
	/** 
	 * @param name
	 * @param shell 
	 * @param fpRoot 
	 */
	public MockBuildJob(String name, Shell shell, IProjectRoot fpRoot) {
		super(name);
		this.shell = shell;
		fpr = fpRoot;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		FedoraPackager fp = new FedoraPackager(fpr);
		try {
			// need to get sources for an SRPM build
			download = (DownloadSourceCommand) fp
				.getCommandInstance(DownloadSourceCommand.ID);
			// get RPM build command in order to produce an SRPM
			srpmBuild = (RpmBuildCommand) fp
					.getCommandInstance(RpmBuildCommand.ID);
			mockBuild = (MockBuildCommand) fp
				.getCommandInstance(MockBuildCommand.ID);
		} catch (FedoraPackagerCommandNotFoundException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					fpr.getProductStrings().getProductName(), e.getMessage());
			return FedoraHandlerUtils
			.errorStatus(
					RPMPlugin.PLUGIN_ID,
					e.getMessage(),
					e);
		} catch (FedoraPackagerCommandInitializationException e) {
			logger.logError(e.getMessage(), e);
			FedoraHandlerUtils.showErrorDialog(shell,
					fpr.getProductStrings().getProductName(), e.getMessage());
			return FedoraHandlerUtils
			.errorStatus(
					RPMPlugin.PLUGIN_ID,
					e.getMessage(),
					e);
		}
		logger.logInfo(NLS.bind(
				FedoraPackagerText.callingCommand,
				MockBuildCommand.class.getName()));
		//sources need to be downloaded
		final String downloadUrlPreference = PackagerPlugin
		.getStringPreference(FedoraPackagerPreferencesConstants.PREF_LOOKASIDE_DOWNLOAD_URL);
		Job downloadSourcesJob = new DownloadSourcesJob(RpmText.MockBuildHandler_downloadSourcesForMockBuild,
				download, fpr, shell, downloadUrlPreference, true);
		downloadSourcesJob.setUser(true);
		downloadSourcesJob.schedule();
		try {
			// wait for download job to finish
			downloadSourcesJob.join();
		} catch (InterruptedException e1) {
			throw new OperationCanceledException();
		}
		if (!downloadSourcesJob.getResult().isOK()) {
				// bail if something failed
			return downloadSourcesJob.getResult();
		}
			//srpms need to be built
			// Create a brand new SRPM
		SRPMBuildJob srpmBuildJob = new SRPMBuildJob(NLS.bind(
			RpmText.MockBuildHandler_creatingSRPMForMockBuild,
			fpr.getPackageName()), srpmBuild,
			fpr);
		srpmBuildJob.setUser(true);
		srpmBuildJob.schedule();
		try {
			// wait for SRPM build to finish
			srpmBuildJob.join();
		} catch (InterruptedException e1) {
			throw new OperationCanceledException();
		}
		if (!srpmBuildJob.getResult().isOK()) {
			// bail if something failed
			return srpmBuildJob.getResult();
		}
		final RpmBuildResult srpmBuildResult = srpmBuildJob.getSRPMBuildResult(); 
		try {
			mockBuild.pathToSRPM(srpmBuildResult
				.getAbsoluteSRPMFilePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// catch error when creating the SRPM failed.
			logger.logError(
					RpmText.MockBuildHandler_srpmBuildFailed,
					e);
			return FedoraHandlerUtils
					.errorStatus(
							RPMPlugin.PLUGIN_ID,
							RpmText.MockBuildHandler_srpmBuildFailed,
							e);
		}
		Job mockJob = new Job(fpr.getProductStrings().getProductName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask(
							RpmText.MockBuildHandler_testLocalBuildWithMock,
							IProgressMonitor.UNKNOWN);
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					try {
						result = mockBuild.call(monitor);
					} catch (CommandMisconfiguredException e) {
						// This shouldn't happen, but report error
						// anyway
						logger.logError(e.getMessage(), e);
						return FedoraHandlerUtils.errorStatus(
								RPMPlugin.PLUGIN_ID, e.getMessage(), e);
					} catch (UserNotInMockGroupException e) {
						// nothing critical, advise the user what to do.
						logger.logInfo(e.getMessage());
						FedoraHandlerUtils
						.showInformationDialog(shell,
								fpr.getProductStrings().getProductName(), e
								.getMessage());
						return Status.OK_STATUS;
					} catch (CommandListenerException e) {
						// There are no command listeners registered, so
						// shouldn't
						// happen. Do something reasonable anyway.
						logger.logError(e.getMessage(), e);
						return FedoraHandlerUtils.errorStatus(
								RPMPlugin.PLUGIN_ID, e.getMessage(), e);
					} catch (MockBuildCommandException e) {
						// Some unknown error occurred
						logger.logError(e.getMessage(), e.getCause());
						return FedoraHandlerUtils.errorStatus(
								RPMPlugin.PLUGIN_ID, e.getMessage(),
								e.getCause());
					} catch (MockNotInstalledException e) {
						// nothing critical, advise the user what to do.
						logger.logInfo(e.getMessage());
						FedoraHandlerUtils
						.showInformationDialog(shell,
								fpr.getProductStrings().getProductName(), e
								.getMessage());
						return Status.OK_STATUS;
					}
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		mockJob.addJobChangeListener(getMockJobFinishedJobListener());
		mockJob.setUser(true);
		mockJob.schedule();
		try {
			// wait for job to finish
			mockJob.join();
		} catch (InterruptedException e1) {
			throw new OperationCanceledException();
		}
		return mockJob.getResult();
	}
	/**
	 * 
	 * @return A job listener for the {@code done} event.
	 */
	protected IJobChangeListener getMockJobFinishedJobListener() {
		IJobChangeListener listener = new JobChangeAdapter() {

			// We are only interested in the done event
			@Override
			public void done(IJobChangeEvent event) {
				FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
				if (result.wasSuccessful()) {
					// TODO: Make this a link to the directory
					String msg = NLS.bind(
							RpmText.MockBuildHandler_mockSucceededMsg,
							result.getResultDirectoryPath());
					logger.logInfo(msg);
					FedoraHandlerUtils.showInformationDialog(
							shell,
							fpr.getProductStrings().getProductName(), msg);
				} else {
					String msg = NLS.bind(
							RpmText.MockBuildHandler_mockFailedMsg,
							result.getResultDirectoryPath());
					logger.logInfo(msg);
					FedoraHandlerUtils.showInformationDialog(
							shell,
							fpr.getProductStrings().getProductName(),
							msg);
				}
			}
		};
		return listener;
	}
}
