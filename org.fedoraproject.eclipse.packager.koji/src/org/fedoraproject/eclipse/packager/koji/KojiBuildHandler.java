package org.fedoraproject.eclipse.packager.koji;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.fedoraproject.eclipse.packager.CommonHandler;

@SuppressWarnings("restriction")
public class KojiBuildHandler extends CommonHandler {
	private String dist;
	private String scmURL;
	protected IKojiHubClient koji;

	@Override
	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor)
			throws ExecutionException {
		dist = specfile.getParent().getName();
		try {
			scmURL = getRepo();
		} catch (CVSException e) {
			e.printStackTrace();
			return handleError(e);
		}

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		IStatus status = Status.OK_STATUS;
		if (promptForTag()) {
			status = doTag(monitor);
		}
		if (status.isOK()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			try {
				status = makeBuildJob(scmURL, makeTagName(), monitor);
			} catch (CoreException e) {
				status = handleError(e);
			}
		}

		return status;
	}

	private boolean promptForTag() {
		if (debug) {
			// don't worry about tagging for debug mode
			return false;
		}
		YesNoRunnable op = new YesNoRunnable(Messages.getString("KojiBuildHandler.0")); //$NON-NLS-1$
		Display.getDefault().syncExec(op);
		return op.isOkPressed();
	}

	protected IStatus makeBuildJob(final String scmURL, final String tagName,
			IProgressMonitor monitor) {
		final IStatus result = newBuild(scmURL, tagName, monitor);
		if (result.isOK()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (!debug) {
				// if build is successfully sent, display the Kojiweb URL in a
				// dialog
				IJobChangeListener listener = new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								ImageDescriptor descriptor = KojiPlugin
										.getImageDescriptor("icons/Artwork_DesignService_koji-icon-16.png"); //$NON-NLS-1$
								Image titleImage = descriptor.createImage();
								KojiMessageDialog msgDialog = new KojiMessageDialog(
										shell,
										Messages.getString("KojiBuildHandler.2"), //$NON-NLS-1$
										titleImage,
										result.getMessage(),
										MessageDialog.NONE,
										new String[] { IDialogConstants.OK_LABEL },
										0);
								msgDialog.open();
							}
						});
					}
				};

				job.addJobChangeListener(listener);
			}
		}
		return result;
	}

	protected IStatus newBuild(String scmURL, String tagName,
			IProgressMonitor monitor) {
		IStatus status;
		try {
			// for testing use the stub instead
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (!debug) {
				monitor.subTask(Messages.getString("KojiBuildHandler.3")); //$NON-NLS-1$
				koji = new KojiHubClient();
			}

			scmURL += "#" + tagName; //$NON-NLS-1$
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// SSL login
			monitor.subTask(Messages.getString("KojiBuildHandler.5")); //$NON-NLS-1$
			String result = koji.sslLogin();

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// push build
			monitor.subTask(Messages.getString("KojiBuildHandler.6")); //$NON-NLS-1$
			result = koji.build(branches.get(dist).get("target"), scmURL, isScratch()); //$NON-NLS-1$

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// logout
			monitor.subTask(Messages.getString("KojiBuildHandler.8")); //$NON-NLS-1$
			koji.logout();
			status = new Status(IStatus.OK, KojiPlugin.PLUGIN_ID, result);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			status = handleError(e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			status = handleError(e);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			status = handleError(e);
		} catch (IOException e) {
			e.printStackTrace();
			status = handleError(e);
		}
		return status;
	}

	public IKojiHubClient getKoji() {
		return koji;
	}

	public void setKoji(IKojiHubClient koji) {
		this.koji = koji;
	}

	private String getRepo() throws CVSException {
		String ret = null;
		// get the project for this specfile
		IProject proj = specfile.getProject();

		if (CVSTeamProvider.isSharedWithCVS(proj)) {
			// get CVSProvider
			CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
					.getProvider(proj, CVSProviderPlugin.getTypeId());
			// get Repository Location
			ICVSRepositoryLocation location = provider.getRemoteLocation();

			// get CVSROOT
			CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();

			ICVSFolder folder = cvsRoot.getLocalRoot();
			FolderSyncInfo syncInfo = folder.getFolderSyncInfo();

			String module = syncInfo.getRepository();

			ret = "cvs://" + location.getHost() + location.getRootDirectory() //$NON-NLS-1$
					+ "?" + module + "/" + dist; //$NON-NLS-1$ //$NON-NLS-2$
		}

		return ret;
	}

	@Override
	protected String getTaskName() {
		return Messages.getString("KojiBuildHandler.12"); //$NON-NLS-1$
	}

	protected boolean isScratch() {
		return false;
	}
}
