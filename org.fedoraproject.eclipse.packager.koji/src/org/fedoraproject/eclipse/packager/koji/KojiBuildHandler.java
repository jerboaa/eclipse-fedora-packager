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
package org.fedoraproject.eclipse.packager.koji;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;

import org.apache.xmlrpc.XmlRpcException;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
import org.fedoraproject.eclipse.packager.IFpProjectBits;
import org.fedoraproject.eclipse.packager.handlers.CommonHandler;
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;

/**
 * Handler to perform a Koji build.
 * 
 */
public class KojiBuildHandler extends CommonHandler {
	@SuppressWarnings("unused")
	private String dist;
	protected IKojiHubClient koji;
	private Job job;

	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final IResource resource = FedoraHandlerUtils.getResource(e);
		final FedoraProjectRoot fedoraProjectRoot = FedoraHandlerUtils
				.getValidRoot(e);
		final IFpProjectBits projectBits = FedoraHandlerUtils
				.getVcsHandler(fedoraProjectRoot);
		job = new Job(Messages.kojiBuildHandler_jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.kojiBuildHandler_sendBuildToKoji,
						IProgressMonitor.UNKNOWN);
				dist = fedoraProjectRoot.getSpecFile().getParent().getName();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				IStatus status = Status.OK_STATUS;
				if (projectBits.needsTag()) {
					// Do VCS tagging
					promptForTag();
					status = projectBits.tagVcs(fedoraProjectRoot, monitor);
				}
				if (status.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					status = makeBuildJob(fedoraProjectRoot, monitor);
				}

				monitor.done();
				return status;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	private boolean promptForTag() {
		if (debug) {
			// don't worry about tagging for debug mode
			return false;
		}
		YesNoRunnable op = new YesNoRunnable(
				Messages.kojiBuildHandler_tagBeforeSendingBuild); //$NON-NLS-1$
		Display.getDefault().syncExec(op);
		return op.isOkPressed();
	}

	protected IStatus makeBuildJob(FedoraProjectRoot fedoraProjectRoot,
			IProgressMonitor monitor) {
		final IStatus result = newBuild(fedoraProjectRoot, monitor);
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
										Messages.kojiBuildHandler_kojiBuild,
										titleImage,
										result.getMessage(),
										MessageDialog.NONE,
										new String[] { IDialogConstants.OK_LABEL },
										0,
										koji);
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

	protected IStatus newBuild(FedoraProjectRoot fedoraProjectRoot,
			IProgressMonitor monitor) {
		IStatus status;
		IFpProjectBits projectBits = FedoraHandlerUtils
				.getVcsHandler(fedoraProjectRoot);
		try {
			// for testing use the stub instead
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (!debug) {
				monitor.subTask(Messages.kojiBuildHandler_connectKojiMsg);
				koji = new KojiHubClient();
			}

			String scmURL = projectBits.getScmUrlForKoji(fedoraProjectRoot);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// login via SSL if available, otherwise do plain login
			monitor.subTask(Messages.kojiBuildHandler_kojiLogin);
			String result = koji.login();

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// push build
			monitor.subTask(Messages.kojiBuildHandler_sendBuildCmd);
			result = koji.build(projectBits.getTarget(), scmURL, isScratch());

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// logout
			monitor.subTask(Messages.kojiBuildHandler_kojiLogout);
			koji.logout();
			status = new Status(IStatus.OK, KojiPlugin.PLUGIN_ID, result);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e);
		} catch (IOException e) {
			e.printStackTrace();
			status = FedoraHandlerUtils.handleError(e);
		}
		return status;
	}

	public IKojiHubClient getKoji() {
		return koji;
	}

	public void setKoji(IKojiHubClient koji) {
		this.koji = koji;
	}

	protected boolean isScratch() {
		return false;
	}
}
