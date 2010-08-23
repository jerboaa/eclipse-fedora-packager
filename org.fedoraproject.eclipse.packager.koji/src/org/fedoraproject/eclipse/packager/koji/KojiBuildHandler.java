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
import org.eclipse.core.runtime.CoreException;
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
import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils.ProjectType;

public class KojiBuildHandler extends CommonHandler {
	private String dist;
	private String scmURL;
	protected IKojiHubClient koji;
	private Job job;
	
	@Override
	public Object execute(final ExecutionEvent e) throws ExecutionException {
		final IResource resource = FedoraHandlerUtils.getResource(e);
		final FedoraProjectRoot fedoraProjectRoot = FedoraHandlerUtils.getValidRoot(e);
		final ProjectType type = FedoraHandlerUtils.getProjectType(resource);
		job = new Job("Fedora Packager") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.getString("KojiBuildHandler.12"),
						IProgressMonitor.UNKNOWN);
				dist = fedoraProjectRoot.getSpecFile().getParent().getName();
				scmURL = getSCMURL(resource);
				System.out.println(scmURL);

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				IStatus status = Status.OK_STATUS;
				if (promptForTag(type)) {
					status = doTag(fedoraProjectRoot, monitor);
				}
				if (status.isOK()) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					try {
						status = makeBuildJob(scmURL, makeTagName(fedoraProjectRoot), monitor);
					} catch (CoreException e) {
						status = handleError(e);
					}
				}

				monitor.done();
				return status;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	private boolean promptForTag(ProjectType type) {
		if (debug || type.equals(ProjectType.GIT)) {
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

	/**
	 * Get the VCS specific URL for the given resource.
	 * 
	 * @param resource
	 * @return The requested URL.
	 */
	private String getSCMURL(IResource resource) {
		IFpProjectBits vcsHandler =  FedoraHandlerUtils.getVcsHandler(resource);
		return vcsHandler.getScmUrl();
	}

	protected boolean isScratch() {
		return false;
	}
}
